package upismpn.obrada;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import upismpn.download.Smer;
import upismpn.download.Smerovi;

/**
 *
 * @author Luka
 */
public class SmeroviBase {
    static Map<String, SmerData> base;
    
    public static void load() {
        base = new HashMap<>();
        Smerovi.loadFromFile();
        Smerovi.iterate(0);
        Smer s;
        while(Smerovi.hasNext()) {
            s = Smerovi.getNext();
            base.put(s.getSifra().toUpperCase(), new SmerData(s));
        }
    }
    
    public static String getPodrucje(String sifra) {
        if(!base.containsKey(sifra.toUpperCase())) {
            System.out.println(sifra);
            System.exit(2);
        }
        return base.get(sifra.toUpperCase()).podrucje;
    }
    
    public static int getKvota(String sifra) {
        return base.get(sifra.toUpperCase()).kvota;
    }
 
    public static String getOkrug(String sifra) {
        return base.get(sifra.toUpperCase()).okrug;
    }
    
    private static class SmerData {
        String sifra;
        String podrucje;
        int kvota;
        String okrug;
        final static Map<String, String> okruzi;
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
}
