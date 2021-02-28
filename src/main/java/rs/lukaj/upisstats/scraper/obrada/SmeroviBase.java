package rs.lukaj.upisstats.scraper.obrada;

import rs.lukaj.upisstats.scraper.download.Smer;
import rs.lukaj.upisstats.scraper.download.Smerovi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Luka
 */
public class SmeroviBase {
    private static Map<String, SmerData> extraBase;
    
    public static void load() {
        extraBase = new HashMap<>();
        srednjeSkole.clear();
        Smerovi smerovi = Smerovi.getInstance();
        smerovi.loadFromFile();
        smerovi.iterate(0);
        Smer s;
        while(smerovi.hasNext()) {
            s = smerovi.getNext();
            extraBase.put(s.getSifra(), new SmerData(s));
        }
    }
    
    public static String getPodrucje(String sifra) {
        if(!extraBase.containsKey(sifra)) {
            System.out.println(sifra);
            throw new IllegalArgumentException("Invalid sifra: " + sifra);
        }
        return extraBase.get(sifra).podrucje;
    }
    
    public static int getKvota(String sifra) {
        return extraBase.get(sifra).kvota;
    }
 
    public static String getOkrug(String sifra) {
        return extraBase.get(sifra).okrug;
    }

    private static class SmerData {
        final static Map<String, String> okruzi;

        String sifra;
        String podrucje;
        int kvota;
        String okrug;
        static {
            okruzi = new HashMap<>();
            okruzi.put("BO", "borski");
            okruzi.put("BR", "braničevski");
            okruzi.put("BG", "grad beograd");
            okruzi.put("JA", "jablanički");
            okruzi.put("JB", "južnobački");
            okruzi.put("JN", "južnobanatski");
            okruzi.put("KO", "kolubarski");
            okruzi.put("KS", "kosovski");
            okruzi.put("KM", "kosovsko mitrovački");
            okruzi.put("KP", "kosovsko pomoravski");
            okruzi.put("MA", "mačvanski");
            okruzi.put("MO", "moravički");
            okruzi.put("NI", "nišavski");
            okruzi.put("PC", "pčinjski");
            okruzi.put("PE", "pećki");
            okruzi.put("PI", "pirotski");
            okruzi.put("PO", "podunavski");
            okruzi.put("PM", "pomoravski");
            okruzi.put("PR", "prizrenski");
            okruzi.put("RA", "rasinski");
            okruzi.put("RS", "raški");
            okruzi.put("SB", "severnobački");
            okruzi.put("SN", "severnobanatski");
            okruzi.put("SR", "srednjebanatski");
            okruzi.put("SM", "sremski");
            okruzi.put("SU", "šumadijski");
            okruzi.put("TO", "toplički");
            okruzi.put("ZA", "zaječarski");
            okruzi.put("ZB", "zapadnobački");
            okruzi.put("ZL", "zlatiborski");
        }
        
        SmerData(Smer s) {
            sifra = s.getSifra().toUpperCase();
            podrucje = s.getPodrucje().toLowerCase();
            kvota = Integer.parseInt(s.getKvota());
            String okrugTag = sifra.substring(0, 2);
            okrug = okruzi.get(okrugTag);
        }
        
        @Override
        public boolean equals(Object s) {
            if(s == null) return false;
            if(s instanceof SmerData)
                return sifra.equals(((SmerData)s).sifra);
            if(s instanceof Smer)
                return sifra.equals(((Smer)s).getSifra());
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 73 * hash + Objects.hashCode(this.sifra);
            return hash;
        }
    }


    private static final Map<String, UcenikWrapper.SrednjaSkola> srednjeSkole = new HashMap<>();

    static boolean skolaExists(String sifra) {
        return srednjeSkole.containsKey(sifra);
    }
    static UcenikWrapper.SrednjaSkola getSkola(String sifra) {
        return srednjeSkole.get(sifra);
    }
    static void putSkola(UcenikWrapper.SrednjaSkola skola) {
        srednjeSkole.put(skola.sifra, skola);
    }
}
