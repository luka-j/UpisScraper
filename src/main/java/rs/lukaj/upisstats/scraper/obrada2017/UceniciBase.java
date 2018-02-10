package rs.lukaj.upisstats.scraper.obrada2017;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.Ucenik2017;
import rs.lukaj.upisstats.scraper.obrada.FileMerger;
import rs.lukaj.upisstats.scraper.utils.Profiler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class UceniciBase {
    private static HashMap<Integer, UcenikW> base = new HashMap<>();

    public static void load() {
        long start = System.nanoTime();
        if(!SmeroviBase.isLoaded()) SmeroviBase.load();
        if(!OsnovneBase.isLoaded()) OsnovneBase.load();
        long end = System.nanoTime();
        Profiler.addTime("UceniciBaseLoadSkole", end-start);
        if(!base.isEmpty()) {
            System.err.println("Possible bug: attempting to load non-empty UceniciBase. Ignoring request");
            return;
        }

        start = System.nanoTime();
        List<String> ucenici = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        long endDisk = System.nanoTime();
        long startLoop = System.nanoTime();
        for(String ucStr : ucenici) {
            long t1 = System.nanoTime();
            Ucenik2017 uc = new Ucenik2017(ucStr.substring(0, 6));
            long t2 = System.nanoTime();
            uc.loadFromString(ucStr.substring(7));
            long t3 = System.nanoTime();
            UcenikW uw = new UcenikW(uc);
            long t4 = System.nanoTime();
            base.put(uw.sifra, uw);
            long t5 = System.nanoTime();
            Profiler.addTime("loopMakeUcenik2017", t2-t1);
            Profiler.addTime("loopLoadUcenik2017", t3-t2);
            Profiler.addTime("loopWrapUcenik2017", t4-t3);
            Profiler.addTime("loopSaveUcenik2017", t5-t4);
        }
        long endLoop = System.nanoTime();
        for(UcenikW uc : base.values()) uc.setBlizanac();
        end = System.nanoTime();
        Profiler.addTime("UceniciBaseLoadDisk", endDisk-start);
        Profiler.addTime("UceniciBaseLoadTotal", end-start);
        Profiler.addTime("UceniciBaseMainLoop", endLoop - startLoop);
    }

    public static Stream<UcenikW> svi() {
        if(base.isEmpty()) load();
        return base.values().stream();
    }

    public static void clear() {
        base.clear();
    }

    public static UcenikW get(int sifra) {
        return base.get(sifra);
    }
}
