package upismpn.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static StringBuilder mapToStringBuilder(Map<String, String> m) {
        StringBuilder sb = new StringBuilder();
        m.entrySet().stream().forEach((Map.Entry<String, String> e) -> {
            sb.append(e.getKey()).append(":").append(e.getValue()).append("\\");
        });
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

    static StringBuilder listToStringBuilder(List<Ucenik.Skola> l) {
        StringBuilder sb = new StringBuilder();
        l.stream().forEach((Ucenik.Skola s) -> {
            sb.append(s.toString()).append("\\");
        });
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
        }
        private static Map<String, String> inverse;
        
        static Map<String, String> compress(Map<String, String> m) {
            Set<Map.Entry<String, String>> s = m.entrySet();
            Map<String, String> compressed = new HashMap<>();
            s.forEach((Map.Entry<String, String> t) -> {
                compressed.put(nameToSifra.get(t.getKey()) == null ? t.getKey() : nameToSifra.get(t.getKey()), t.getValue());
            });
            return compressed;
        }
        
        static Map<String, String> decompress(Map<String, String> m) {
            inverse = nameToSifra.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            
            Set<Map.Entry<String, String>> s = m.entrySet();
            Map<String, String> decompressed = new HashMap<>();
            s.forEach((Map.Entry<String, String> t) -> {
                decompressed.put(inverse.get(t.getKey()) == null ? t.getKey() : inverse.get(t.getKey()), t.getValue());
            });
            return decompressed;
        }
    }
    
}
