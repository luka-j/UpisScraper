package upismpn.obrada2017;

import upismpn.download.DownloadController;
import upismpn.download.Ucenik2017;
import upismpn.obrada.FileMerger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class UceniciBase {
    private static HashMap<Integer, UcenikWrapper> base = new HashMap<>();

    public static void load() {
        if(!SmeroviBase.isLoaded()) SmeroviBase.load();
        if(!OsnovneBase.isLoaded()) OsnovneBase.load();

        List<String> ucenici = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        for(String ucStr : ucenici) {
            String[] ucData = ucStr.split("\\n", 2);
            Ucenik2017 uc = new Ucenik2017(ucData[0]);
            uc.loadFromString(ucData[1]);
            UcenikWrapper uw = new UcenikWrapper(uc);
            base.put(uw.sifra, uw);
        }
        for(UcenikWrapper uc : base.values()) uc.setBlizanac();
    }

    public static Stream<UcenikWrapper> svi() {
        return base.values().stream();
    }

    public static UcenikWrapper get(int sifra) {
        return base.get(sifra);
    }
}
