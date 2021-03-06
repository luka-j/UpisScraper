package rs.lukaj.upisstats.scraper.download;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luka
 */
public class Smerovi {
    private static final String SVI_SMEROVI_URL
            = "http://195.222.98.59/srednja_skola_opsta.php?sort_type=nazivSkole,%20sifraukupno%20asc&prikazi_spisak=1&prikazi_details=1&podrada_id=0&opstina_id=0&okrug_id=0&id_profila=0&broj_strane=";
    private static final int START_FROM = 1;
    private static final int END_AT = 95; //moze i da se odredi dok ucitava, ali ovako je lakse //95 default
    private static final int SMEROVA_PO_STRANI = 25; //takodje
    private static final int SMER_IDOVA_PO_TR = 8;

    static final String SAVEFILE_NAME = "smerovi";
    static final File SMEROVI_FOLDER = new File(DownloadController.DATA_FOLDER, "smeroviData");
    static {
        if(!SMEROVI_FOLDER.isDirectory()) SMEROVI_FOLDER.mkdirs();
    }

    private static Smerovi instance;
    public static Smerovi getInstance() {
        if(instance == null) instance = new Smerovi();
        return instance;
    }

    private final LinkedHashMap<String, Smer> base = new LinkedHashMap<>(2_400);

    /**
     * Ucitava podatke o smerovima, ako postoje iz fajla, ako ne s neta
     */
    public void load() {
        File f = new File(DownloadController.DATA_FOLDER, SAVEFILE_NAME);
        DownloadLogger logger = DownloadLogger.getLogger(DownloadLogger.SMEROVI);
        if(f.exists() && f.length() > 0) {
            logger.log(DownloadLogger.Level.NORMAL, "Smer base exists; loading from file");
            loadFromFile();
        } else {
            logger.log(DownloadLogger.Level.NORMAL, "Smer base doesn't exist; starting download from net");
            loadFromNet();
        }
    }
    
    public void loadFromNet() {
        try {
            Document doc;
            Elements trSifra, trPodrucje, trKvota;
            for (int i = START_FROM; i <= END_AT; i++) {
                doc = Jsoup.connect(SVI_SMEROVI_URL + i).post();
                try {
                    for (int j = 2; j <= SMEROVA_PO_STRANI * SMER_IDOVA_PO_TR; j += SMER_IDOVA_PO_TR) {
                        trSifra = doc.select("#" + j);
                        trPodrucje = doc.select("#" + (j + 3));
                        trKvota = doc.select("#" + (j + 4));
                        addToBase(new Smer(trSifra.text(), trPodrucje.text(), trKvota.text()));
                    }
                } catch (NullPointerException ex) {
                    DownloadLogger.getLogger(DownloadLogger.SMEROVI).log(DownloadLogger.Level.DEBUG, "NPE@smerovi: poslednji put");
                    Logger.getLogger(UceniciDownloader.class.getName()).log(Level.FINE, "NPE@smerovi: poslednji put");
                }
                System.out.println(((double) i / END_AT) * 100 + "%...");
            }
        } catch (IOException ex) {
            Logger.getLogger(UceniciDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void addToBase(Smer s) {
        DownloadLogger.getLogger(DownloadLogger.SMEROVI).log(DownloadLogger.Level.VERBOSE, "Add " + s.getSifra() + " to base");
        base.put(s.getSifra(), s);
    }
    protected Smer createSmer(String compactString) {
        return new Smer(compactString);
    }
    
    public void loadFromFile() {
        File f = new File(DownloadController.DATA_FOLDER, SAVEFILE_NAME);
        try {
            String text = Files.readString(f.toPath());
            String[] smerovi = text.split("\\n");
            for(String smer : smerovi) {
                addToBase(createSmer(smer));
            }
        } catch (IOException ex) {
            Logger.getLogger(Ucenik.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Smer get(String sifra) {
        return base.get(sifra);
    }

    /**
     * Cuva podatke o smerovima u fajl
     */
    public void save() {
        StringBuilder out = new StringBuilder();
        base.values().forEach((Smer s) -> out.append(s.toCompactString()));
        File f = new File(DownloadController.DATA_FOLDER, SAVEFILE_NAME);
        try (Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            bw.write(out.toString());
        } catch (IOException ex) {
            Logger.getLogger(Smerovi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //todo make this implement Iterable

    private Iterator<Map.Entry<String, Smer>> baseIterator;
    private int count=0;
    public void iterate(int pos) {
        baseIterator = base.entrySet().iterator();
        for(int i=0; i<pos; i++) baseIterator.next();
        count = pos;
    }

    public boolean hasNext() {
        return baseIterator.hasNext();
    }

    public String getNextSifra() {
        count++;
        return baseIterator.next().getKey();
    }
    
    public Smer getNext() {
        count++;
        return baseIterator.next().getValue();
    }
    
    public int getCurrentIndex() {return count-1;}
    
    public double getPercentageIterated() {
        return ((double)(count+1)/(base.size()+1)) * 100;
    }

    protected Smerovi() {
    }
}
