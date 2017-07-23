package upismpn.obrada2017;

import upismpn.download.DownloadController;
import upismpn.download.Ucenik2017;
import upismpn.obrada.FileMerger;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class UceniciBase {
    private static Set<UcenikWrapper> base = new HashSet<>();

    public static void load() {
        if(!SmeroviBase.isLoaded()) SmeroviBase.load();
        if(!OsnovneBase.isLoaded()) OsnovneBase.load();

        List<String> ucenici = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        for(String ucStr : ucenici) {
            String[] ucData = ucStr.split("\\n", 2);
            Ucenik2017 uc = new Ucenik2017(ucData[0]);
            uc.loadFromString(ucData[1]);
            base.add(new UcenikWrapper(uc));
        }
    }

    public static Stream<UcenikWrapper> svi() {
        return base.stream();
    }
}
