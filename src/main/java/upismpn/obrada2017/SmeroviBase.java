package upismpn.obrada2017;

import upismpn.download.Smer2017;
import upismpn.download.Smerovi2017;

import java.util.HashMap;
import java.util.Map;

public class SmeroviBase {
    private static Map<String, SmerWrapper> base = new HashMap<>();

    public static boolean isLoaded() {
        return !base.isEmpty();
    }

    public static void load() {
        Smerovi2017 smerovi = Smerovi2017.getInstance();
        smerovi.loadFromFile();
        smerovi.iterate(0);
        while(smerovi.hasNext()) {
            Smer2017 smer = (Smer2017) smerovi.getNext();
            base.put(smer.getSifra(), new SmerWrapper(smer));
        }
    }

    public static SmerWrapper get(String sifra) {
        if(base.containsKey(sifra)) return base.get(sifra);
        throw new IllegalArgumentException(sifra);
    }
}
