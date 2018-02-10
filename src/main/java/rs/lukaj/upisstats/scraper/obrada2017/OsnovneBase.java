package rs.lukaj.upisstats.scraper.obrada2017;

import rs.lukaj.upisstats.scraper.download.Osnovna2017;
import rs.lukaj.upisstats.scraper.download.OsnovneDownloader2017;
import rs.lukaj.upisstats.scraper.obrada.FileMerger;
import rs.lukaj.upisstats.scraper.utils.Profiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OsnovneBase {

    private static Map<Integer, OsnovnaW> base = new HashMap<>();

    public static boolean isLoaded() {
        return !base.isEmpty();
    }

    public static void load() {
        long start = System.nanoTime();
        List<String> osnovne = FileMerger.readFromOne(OsnovneDownloader2017.DATAFILE);
        long endDisk = System.nanoTime();
        base = osnovne.stream().map(Osnovna2017::new)
                .map(OsnovnaW::new)
                .collect(Collectors.toMap(os -> os.id, os -> os));
        long endTotal = System.nanoTime();
        Profiler.addTime("OsnovneBaseLoadDisk", endDisk-start);
        Profiler.addTime("OsnovneBaseLoadTotal", endTotal-start);
    }

    public static OsnovnaW get(int id) {
        if(!base.containsKey(id)) throw new IllegalArgumentException(String.valueOf(id));
        return base.get(id);
    }

    public static Collection<OsnovnaW> getAll() {
        return base.values();
    }
}
