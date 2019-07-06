package rs.lukaj.upisstats.scraper.obrada2017;

import rs.lukaj.upisstats.scraper.download.Smer2017;
import rs.lukaj.upisstats.scraper.download.Smerovi2017;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SmeroviBase {
    private static Map<String, SmerW> base = new HashMap<>();

    public static boolean isLoaded() {
        return !base.isEmpty();
    }

    public static void load() {
        Smerovi2017 smerovi = Smerovi2017.getInstance();
        smerovi.loadFromFile();
        smerovi.iterate(0);
        while(smerovi.hasNext()) {
            Smer2017 smer = (Smer2017) smerovi.getNext();
            base.put(smer.getSifra(), new SmerW(smer));
        }
    }

    public static SmerW get(String sifra) {
        if(base.containsKey(sifra)) return base.get(sifra);
        throw new IllegalArgumentException(sifra);
    }

    public static Collection<SmerW> getAll() {
        return base.values();
    }

    public static void clear() {
        base.clear();
    }
}
