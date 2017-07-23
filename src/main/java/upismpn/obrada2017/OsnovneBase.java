package upismpn.obrada2017;

import upismpn.download.Osnovna2017;
import upismpn.download.OsnovneDownloader2017;
import upismpn.obrada.FileMerger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OsnovneBase {

    private static Map<Integer, OsnovnaWrapper> base = new HashMap<>();

    public static boolean isLoaded() {
        return !base.isEmpty();
    }

    public static void load() {
        List<String> osnovne = FileMerger.readFromOne(OsnovneDownloader2017.DATAFILE);
        base = osnovne.stream().map(Osnovna2017::new)
                .map(OsnovnaWrapper::new)
                .collect(Collectors.toMap(os -> os.id, os -> os));
    }

    public static OsnovnaWrapper get(int id) {
        if(!base.containsKey(id)) throw new IllegalArgumentException(String.valueOf(id));
        return base.get(id);
    }
}
