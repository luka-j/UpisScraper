package upismpn.download;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import upismpn.download.UceniciManager.UcData;
import upismpn.UpisMpn;
import static upismpn.UpisMpn.DEBUG;

public class Sifre {
    
    static long spentTime;
    static long startTime = System.currentTimeMillis();

    public static void setStart(int start) {
        if(DEBUG)System.out.println("set progress: " + start);
        START_FROM = start;
    }

    public static int getStart() {
        return START_FROM;
    }
    
    public static long getVreme() {
        return System.currentTimeMillis() - startTime + spentTime;
    }
    public static void setVreme(long vreme) {
        spentTime = vreme;
    }

    private static volatile int START_FROM = 0;

    public static void go() {
        if(DEBUG)System.out.println("starting program");
        Deque<UcData> uc;
        Smerovi.iterate(START_FROM);
        double time, est; char oznaka='s';
        if(DEBUG)System.out.println("starting iteration");
        while (Smerovi.hasNext()) {
            String smerSifra = Smerovi.getNextSifra();
            if(DEBUG)System.out.println("Uzimam sifre ucenika za " + smerSifra);
            uc = getSifreUcenika(smerSifra);
            if(DEBUG)System.out.println("Uzeo sifre za " + smerSifra);
            UceniciManager.add(uc);
            START_FROM++;
            System.out.print(String.format("%.2f%s", Smerovi.getPercentageIterated(), "% - "));
            time = (System.currentTimeMillis() - startTime + spentTime)/1000;
            est = ((100/Smerovi.getPercentageIterated()-1)*time) / 3600;
            if(time > 10800) {time = time/60; oznaka='m';}
            if(time > 43200) {time = time/3600; oznaka='h';}
            System.out.print(String.format("%.2f%s", time, String.valueOf(oznaka)));
            System.out.println(String.format("%s%.2f%s", ". Preostalo još ", est, "h..."));
        }
        System.out.println("downloading last batch");
        UceniciManager.download();
        System.out.println("saving last batch");
        new UceniciManager.Saver().run();
        System.out.println("all done");
    }

    private static final String UCENICI_URL
            = "http://195.222.98.59/srednja_skola_opsta.php?sort_type=nazivSkole,%20sifraukupno%20asc&prikazi_spisak=0&prikazi_details=1&podrada_id=0&okrug_id=0&id_profila=";
    private static final int UCENIKA_PO_STRANI = 25;
    private static final int UCENIK_IDOVA_PO_TR = 5;

    private static Deque<UcData> getSifreUcenika(String sifraProfila) {
        Deque<UcData> sifre = new ArrayDeque<>();
        boolean end = false;
        try {
            Document doc;
            Elements trSifra, trUkBodova, trOS;
            String[] osTokens;
            UcData data;
            int i = 1;
            while (!end) {
                try {
                    do {
                        if(DEBUG)System.out.println("downloading doc " + i + " za " + sifraProfila);
                    doc = downloadDoc(sifraProfila, i);
                    } while(doc == null);
                    if(DEBUG)System.out.println("starting download of ucenici");
                    for (int j = 2; j <= UCENIKA_PO_STRANI * UCENIK_IDOVA_PO_TR; j += UCENIK_IDOVA_PO_TR) {
                        trSifra = doc.select("#" + String.valueOf(j));
                        trUkBodova = doc.select("#" + String.valueOf(j + 3));
                        trOS = doc.select("#" + String.valueOf(j + 2));
                        osTokens = trOS.text().split(",");
                        
                        if(trSifra.text().isEmpty()) {end=true; break;}
                        data = new UcData(trSifra.text(), trUkBodova.text(), osTokens[osTokens.length - 1]);
                        sifre.add(data);
                        if(DEBUG)System.out.print("added new ucenik: " + data);
                    }
                } catch (NullPointerException ex) {
                    Logger.getLogger(Sifre.class.getName()).log(Level.WARNING, "NPE@ucenici: poslednji put");
                    break;
                }
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(UpisMpn.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sifre;
    }

    private static Document downloadDoc(String sifraProfila, int i) throws IOException {
        try {
            return Jsoup.connect(UCENICI_URL + sifraProfila + "&broj_strane=" + i).post();
        } catch (SocketTimeoutException ex) {
            System.err.println("Socket timeout @ downloadDoc (Sifre)");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex1) {
                Logger.getLogger(Sifre.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return null;
        }
    }

    private Sifre() {
    }
}
