package upismpn.obrada;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import upismpn.download.UceniciManager;
import upismpn.download.Ucenik;
import upismpn.obrada.UceniciGroup.UcenikWrapper;

/**
 *
 * @author Luka
 */
public class UceniciGroupBuilder {
    Predicate<UcenikWrapper> op = null;
    static UceniciGroup everyone;
    
    private static void init() {
        Ucenik uc;
        String[] ucData;
        everyone = new UceniciGroup();
        List<String> ucenici = FileMerger.readFromOne(new File(UceniciManager.DATA_FOLDER, FileMerger.FILENAME));
        for(String ucStr : ucenici) {
            ucData = ucStr.split("\\n", 2);
            uc = new Ucenik(ucData[0]);
            uc.loadFromString(ucData[1]);
            everyone.add(new UcenikWrapper(uc));
        }
    }
    
    void addOp(Predicate<UcenikWrapper> o) {
        if(op == null) op = o;
        else op.and(o);
    }
    
    public UceniciGroup getGroup() {
        if(everyone == null) init();
        UceniciGroup ret = new UceniciGroup(everyone).filter(op);
        clearOps();
        return ret;
    }
    
    public void clearOps() {
        op = null;
    }
    
    Map<UcenikWrapper.OsnovnaSkola, UceniciGroup> getByOS() {
        Map<UcenikWrapper.OsnovnaSkola, UceniciGroup> map = new HashMap<>();
        if(everyone == null) init();
        everyone.forEach((uc) -> {
            if(map.get(uc.osInfo) == null) {
                map.put(uc.osInfo, new UceniciGroup(uc));
            } else {
                map.get(uc.osInfo).add(uc);
            }
        });
        return map;
    }
}
