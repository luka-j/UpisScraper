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
        if(!base.isEmpty()) {
            System.err.println("Possible bug: attempting to load non-empty UceniciBase. Ignoring request");
            return;
        }

        List<String> ucenici = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        for(String ucStr : ucenici) {
            int sifraEndPosition = ucStr.indexOf('\n');
            Ucenik2017 uc = new Ucenik2017(ucStr.substring(0, sifraEndPosition));
            uc.loadFromString(ucStr.substring(sifraEndPosition+1));
            UcenikW uw = new UcenikW(uc);
            base.put(uw.sifra, uw);
        }
        for(UcenikW uc : base.values()) uc.setBlizanac();
    }

    public static Stream<UcenikW> svi() {
        if(base.isEmpty()) load();
        return base.values().stream();
    }

    public static void clear() {
        base.clear();
        SmeroviBase.clear();
        OsnovneBase.clear();
    }

    public static UcenikW get(int sifra) {
        return base.get(sifra);
    }
}
