package rs.lukaj.upisstats.scraper.obrada2017;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.Ucenik2017;
import rs.lukaj.upisstats.scraper.obrada.FileMerger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class UceniciBase {
    private static HashMap<Integer, UcenikW> base = new HashMap<>();

    public static void load() {
        if(!SmeroviBase.isLoaded()) SmeroviBase.load();
        if(!OsnovneBase.isLoaded()) OsnovneBase.load();
        //Profiler.addTime("UceniciBaseLoadSkole", end-start);
        if(!base.isEmpty()) {
            System.err.println("Possible bug: attempting to load non-empty UceniciBase. Ignoring request");
            return;
        }

        List<String> ucenici = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        for(String ucStr : ucenici) {
            Ucenik2017 uc = new Ucenik2017(ucStr.substring(0, 6));
            uc.loadFromString(ucStr.substring(7));
            UcenikW uw = new UcenikW(uc);
            base.put(uw.sifra, uw);
            //Profiler.addTime("loopMakeUcenik2017", t2-t1);
            //Profiler.addTime("loopLoadUcenik2017", t3-t2);
            //Profiler.addTime("loopWrapUcenik2017", t4-t3);
            //Profiler.addTime("loopSaveUcenik2017", t5-t4);
        }
        for(UcenikW uc : base.values()) uc.setBlizanac();
        //Profiler.addTime("UceniciBaseLoadDisk", endDisk-start);
        //Profiler.addTime("UceniciBaseLoadTotal", end-start);
        //Profiler.addTime("UceniciBaseMainLoop", endLoop - startLoop);
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
