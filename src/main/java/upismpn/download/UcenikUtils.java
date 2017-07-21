package upismpn.download;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Luka
 */
public class UcenikUtils {

    static List<Ucenik.Skola> stringToList(String[] zelje) {
        List<Ucenik.Skola> l = new ArrayList<>();
        for (String zelja : zelje) {
            if (!zelja.isEmpty()) {
                l.add(new Ucenik.Skola(zelja));
            }
        }
        return l;
    }
    static List<Ucenik2017.Zelja> stringToListZelja(String[] zelje) {
        List<Ucenik2017.Zelja> l = new ArrayList<>();
        for (String zelja : zelje) {
            if (!zelja.isEmpty()) {
                l.add(new Ucenik2017.Zelja(zelja));
            }
        }
        return l;
    }

    public static StringBuilder mapToStringBuilder(Map<String, String> m) {
        StringBuilder sb = new StringBuilder();
        m.forEach((key, value) -> sb.append(key).append(":").append(value).append("\\"));
        sb.append("\n");
        return sb;
    }

    public static Map<String, String> stringArrayToMap(String[] str) {
        Map<String, String> m = new HashMap<>();
        if (str == null || (str.length == 1 && str[0].isEmpty()) || str.length == 0) {
            return m;
        }
        String[] pair;
        for (String p : str) {
            pair = p.split(":");
            try {
                m.put(pair[0], pair[1]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.err.println("invalid: " + p);
            }
        }
        return m;
    }

    static <T> StringBuilder listToStringBuilder(List<T> l) {
        StringBuilder sb = new StringBuilder();
        l.forEach((T s) -> sb.append(s.toString()).append("\\"));
        sb.append("\n");
        return sb;
    }

    public static boolean isAllCaps(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isUpperCase(text.charAt(i)) && text.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }
    
    public static class PredmetiDefault {
        
        public static final String MATEMATIKA = "Matematika";
        public static final String SRPSKI = "Srpski jezik";
        public static final String ENGLESKI = "Engleski jezik";
        public static final String LIKOVNO = "Likovno vaspitanje";
        public static final String MUZICKO = "Muzičko vaspitanje";
        public static final String ISTORIJA = "Istorija";
        public static final String GEOGRAFIJA = "Geografija";
        public static final String FIZIKA = "Fizika";
        public static final String BIOLOGIJA = "Biologija";
        public static final String HEMIJA = "Hemija";
        public static final String TEHNICKO = "Tehničko obrazovanje";
        public static final String FIZICKO = "Fizičko vaspitanje";
        public static final String SPORT = "Izabrani sport";
        public static final String VLADANJE = "Vladanje";

        public static final String MATERNJI = "maternjiJezik";
        public static final String DRUGI_MATERNJI = "drugiMaternjiJezik";
        public static final String PRVI_STRANI = "prviStrani";
        public static final String LIKOVNO2017 = "likovno";
        public static final String MUZICKO2017 = "muzicko";
        public static final String ISTORIJA2017 = "istorija";
        public static final String GEOGRAFIJA2017 = "geografija";
        public static final String FIZIKA2017 = "fizika";
        public static final String MATEMATIKA2017 = "matematika";
        public static final String BIOLOGIJA2017 = "biologija";
        public static final String HEMIJA2017 = "hemija";
        public static final String TEHNICKO2017 = "tehnicko";
        public static final String FIZICKO2017 = "fizicko";
        public static final String DRUGI_STRANI = "drugiStrani";
        public static final String SPORT2017 = "izborniSport";
        public static final String SPORT2017_LOWER = "izbornisport"; //yeah that exists
        public static final String VLADANJE2017 = "vladanje";

        public static final String ZBIR2017 = "ZbirOcena";
        public static final String BROJ2017 = "BrojOcena";
        public static final String PROSEK6_2017 = "prosek6";
        public static final String PROSEK7_2017 = "prosek7";
        public static final String PROSEK8_2017 = "prosek8";
        public static final String BOD6_2017 = "bod6";
        public static final String BOD7_2017 = "bod7";
        public static final String BOD8_2017 = "bod8";
        public static final String VUKOVA2017 = "vukovaDiploma";
        
        private static final Map<String, String> nameToSifra;
        static {
            nameToSifra = new HashMap<>();
            nameToSifra.put(SRPSKI, "0");
            nameToSifra.put(ENGLESKI, "1");
            nameToSifra.put(LIKOVNO, "2");
            nameToSifra.put(MUZICKO, "3");
            nameToSifra.put(ISTORIJA, "4");
            nameToSifra.put(GEOGRAFIJA, "5");
            nameToSifra.put(FIZIKA, "6");
            nameToSifra.put(MATEMATIKA, "7");
            nameToSifra.put(BIOLOGIJA, "8");
            nameToSifra.put(HEMIJA, "9");
            nameToSifra.put(TEHNICKO, "A");
            nameToSifra.put(FIZICKO, "B");
            nameToSifra.put("Nemački jezik", "C");
            nameToSifra.put(SPORT, "D");
            nameToSifra.put(VLADANJE, "E");
            nameToSifra.put("Ruski jezik", "F");
            nameToSifra.put("Francuski jezik", "G");
            nameToSifra.put("Italijanski jezik", "H");
            nameToSifra.put("Španski jezik", "I");

            nameToSifra.put(MATERNJI, "a");
            nameToSifra.put(DRUGI_MATERNJI, "b");
            nameToSifra.put(PRVI_STRANI, "c");
            nameToSifra.put(LIKOVNO2017, "d");
            nameToSifra.put(MUZICKO2017, "e");
            nameToSifra.put(ISTORIJA2017, "f");
            nameToSifra.put(GEOGRAFIJA2017, "g");
            nameToSifra.put(FIZIKA2017, "h");
            nameToSifra.put(MATEMATIKA2017, "i");
            nameToSifra.put(BIOLOGIJA2017, "j");
            nameToSifra.put(HEMIJA2017, "k");
            nameToSifra.put(TEHNICKO2017, "l");
            nameToSifra.put(FIZICKO2017, "m");
            nameToSifra.put(DRUGI_STRANI, "n");
            nameToSifra.put(SPORT2017, "O");
            nameToSifra.put(SPORT2017_LOWER, "o");
            nameToSifra.put(VLADANJE2017, "p");

            nameToSifra.put(PROSEK6_2017, "!");
            nameToSifra.put(PROSEK7_2017, "@");
            nameToSifra.put(PROSEK8_2017, "#");
            nameToSifra.put(ZBIR2017, "$");
            nameToSifra.put(BROJ2017, "%");
            nameToSifra.put(BOD6_2017, "^");
            nameToSifra.put(BOD7_2017, "&");
            nameToSifra.put(BOD8_2017, "*");
            nameToSifra.put(VUKOVA2017, "-");
        }
        private static Map<String, String> inverse;
        
        static Map<String, String> compress(Map<String, String> m) {
            Set<Map.Entry<String, String>> s = m.entrySet();
            Map<String, String> compressed = new HashMap<>();
            s.forEach((Map.Entry<String, String> t) ->
                    compressed.put(nameToSifra.get(t.getKey()) == null ? t.getKey() : nameToSifra.get(t.getKey()), t.getValue()));
            return compressed;
        }
        
        static Map<String, String> decompress(Map<String, String> m) {
            if(inverse == null)
                inverse = nameToSifra.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            
            Set<Map.Entry<String, String>> s = m.entrySet();
            Map<String, String> decompressed = new HashMap<>();
            s.forEach((Map.Entry<String, String> t) ->
                    decompressed.put(inverse.get(t.getKey()) == null ? t.getKey() : inverse.get(t.getKey()), t.getValue()));
            return decompressed;
        }
    }
    
}
