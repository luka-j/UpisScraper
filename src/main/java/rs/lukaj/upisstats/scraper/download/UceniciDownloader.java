package rs.lukaj.upisstats.scraper.download;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import rs.lukaj.upisstats.scraper.Main;
import rs.lukaj.upisstats.scraper.download.UceniciManager.UcData;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UceniciDownloader {
    private static UceniciDownloader instance;

    public static UceniciDownloader getInstance(int startingIndex, long time) {
        if(instance == null) instance = new UceniciDownloader(startingIndex, time);
        return instance;
    }

    private long spentTime;
    private long startTime = System.currentTimeMillis();


    public int getCurrentSmer() {
        return currentSmer.get();
    }
    
    public long getVreme() {
        return System.currentTimeMillis() - startTime + spentTime;
    }

    private final AtomicInteger currentSmer = new AtomicInteger(0); //nisam sasvim siguran zasto je ovo atomic

    /**
     * Preuzima podatke o ucenicima sa sajta
     */
    public void downloadStudentData(DownloadConfig config) {
        if(Main.DEBUG)System.out.println("starting program");
        Deque<UcData> uc;
        Smerovi smerovi = config.getSmerovi();
        smerovi.iterate(getCurrentSmer());
        double time, est;
        char oznaka;
        if(Main.DEBUG)System.out.println("starting iteration");
        UceniciManager ucenici = config.getUceniciManager();
        while (smerovi.hasNext()) {
            String smerSifra = smerovi.getNextSifra();
            if(Main.DEBUG)System.out.println("Uzimam sifre ucenika za " + smerSifra);
            uc = getSifreUcenika(smerSifra);
            if(Main.DEBUG)System.out.println("Uzeo sifre za " + smerSifra);
            ucenici.add(uc);
            currentSmer.incrementAndGet(); //possible source of bugs (race condition: non-atomic increment)
            System.out.print(String.format("%.2f%s", smerovi.getPercentageIterated(), "% - "));
            time = (System.currentTimeMillis() - startTime + spentTime)/1000;
            est = ((100/smerovi.getPercentageIterated()-1)*time) / 3600;
            if(time > 10800) {time = time/60; oznaka='m';}
            else if(time > 43200) {time = time/3600; oznaka='h';}
            else oznaka='s';
            System.out.print(String.format("%.2f%s", time, String.valueOf(oznaka)));
            System.out.println(String.format("%s%.2f%s", ". Preostalo još ", est, "h..."));
        }
        System.out.println("downloading last batch");
        ucenici.download();
        System.out.println("saving last batch");
        ucenici.new Saver().run(); //my fav piece of syntax
        System.out.println("all done");
    }

    private static final String UCENICI_URL
            = "http://195.222.98.59/srednja_skola_opsta.php?sort_type=nazivSkole,%20sifraukupno%20asc&prikazi_spisak=0&prikazi_details=1&podrada_id=0&okrug_id=0&id_profila=";
    private static final int UCENIKA_PO_STRANI = 25;
    private static final int UCENIK_IDOVA_PO_TR = 5;

    protected Deque<UcData> getSifreUcenika(String sifraProfila) {
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
                        if(Main.DEBUG)System.out.println("downloading doc " + i + " za " + sifraProfila);
                        doc = downloadDoc(generateUrl(sifraProfila, i), "", true);
                    } while(doc == null);
                    if(Main.DEBUG)System.out.println("starting download of ucenici");
                    for (int j = 2; j <= UCENIKA_PO_STRANI * UCENIK_IDOVA_PO_TR; j += UCENIK_IDOVA_PO_TR) {
                        trSifra = doc.select("#" + String.valueOf(j));
                        trUkBodova = doc.select("#" + String.valueOf(j + 3));
                        trOS = doc.select("#" + String.valueOf(j + 2));
                        osTokens = trOS.text().split(",");
                        
                        if(trSifra.text().isEmpty()) {end=true; break;}
                        data = new UcData(trSifra.text(), trUkBodova.text(), osTokens[osTokens.length - 1]);
                        sifre.add(data);
                        if(Main.DEBUG)System.out.print("added new ucenik: " + data);
                    }
                } catch (NullPointerException ex) {
                    System.err.println("NPE@ucenici: poslednji put");
                    DownloadLogger.getLogger(DownloadLogger.UCENICI).log(DownloadLogger.Level.WARNING, "NPE@getSifreUcenika");
                    break;
                }
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(UceniciDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sifre;
    }

    protected Document downloadDoc(String url, String requestBody, boolean post) throws IOException {
        try {
            Connection c =  Jsoup.connect(url);
            return post ? c.requestBody(requestBody).post() : c.get();
        } catch (SocketTimeoutException ex) {
            System.err.println("Socket timeout @ downloadDoc (UceniciDownloader): " + url);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex1) {
                Logger.getLogger(UceniciDownloader.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return null;
        }
    }

    private static String generateUrl(String sifra, int brojStrane) {
        return UCENICI_URL + sifra + "&broj_strane=" + brojStrane;
    }

    protected UceniciDownloader(int startingIndex, long time) {
        spentTime = time;
        currentSmer.set(startingIndex);
    }
}
