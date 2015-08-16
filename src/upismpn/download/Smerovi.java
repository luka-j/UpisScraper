package upismpn.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Luka
 */
public class Smerovi {

    private static final List<Smer> base = new ArrayList<>(2_363);
    private static int it;

    private static final String SVI_SMEROVI_URL
            = "http://195.222.98.59/srednja_skola_opsta.php?sort_type=nazivSkole,%20sifraukupno%20asc&prikazi_spisak=1&prikazi_details=1&podrada_id=0&opstina_id=0&okrug_id=0&id_profila=0&broj_strane=";
    private static final int START_FROM = 1;
    private static final int END_AT = 95; //moze i da se odredi dok ucitava, ali ovako je lakse //95 default
    private static final int SMEROVA_PO_STRANI = 25; //takodje
    private static final int SMER_IDOVA_PO_TR = 8;
    private static final String SAVEFILE_NAME = "smerovi";

    public static void load() {
        File f = new File(UceniciManager.DATA_FOLDER, SAVEFILE_NAME);
        if(f.exists())
            loadFromFile();
        else
            loadFromNet();
    }
    
    public static void loadFromNet() {
        try {
            Document doc;
            Elements trSifra, trPodrucje, trKvota;
            for (int i = START_FROM; i <= END_AT; i++) {
                doc = Jsoup.connect(SVI_SMEROVI_URL + String.valueOf(i)).post();
                try {
                    for (int j = 2; j <= SMEROVA_PO_STRANI * SMER_IDOVA_PO_TR; j += SMER_IDOVA_PO_TR) {
                        trSifra = doc.select("#" + String.valueOf(j));
                        trPodrucje = doc.select("#" + String.valueOf(j + 3));
                        trKvota = doc.select("#" + String.valueOf(j + 4));
                        base.add(new Smer(trSifra.text(), trPodrucje.text(), trKvota.text()));
                    }
                } catch (NullPointerException ex) {
                    Logger.getLogger(Sifre.class.getName()).log(Level.FINE, "NPE@smerovi: poslednji put");
                }
                System.out.println(String.valueOf(((double)i/END_AT) * 100) + "%...");
            }
        } catch (IOException ex) {
            Logger.getLogger(Sifre.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void loadFromFile() {
        File f = new File(UceniciManager.DATA_FOLDER, SAVEFILE_NAME);
        try {
            String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            String[] smerovi = text.split("\\n");
            for(String smer : smerovi) {
                base.add(new Smer(smer));
            }
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void save() {
        StringBuilder out = new StringBuilder();
        base.forEach((Smer s) -> out.append(s.toCompactString()));
        File f = new File(UceniciManager.DATA_FOLDER, SAVEFILE_NAME);
        try (Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            f.delete();
            f.createNewFile();
            bw.write(out.toString());
        } catch (IOException ex) {
            Logger.getLogger(Smerovi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void iterate(int i) {
        it = i;
    }

    public static boolean hasNext() {
        return it < base.size();
    }

    public static String getNextSifra() {
        it++;
        return base.get(it-1).getSifra();
    }
    
    public static Smer getNext() {
        it++;
        return base.get(it-1);
    }
    
    public static int getCurrentIndex() {return it-1;}
    
    public static double getPercentageIterated() {
        return ((double)(it+1)/(base.size()+1)) * 100;
    }

    private Smerovi() {
    }
}
