package upismpn.obrada;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import upismpn.download.UceniciManager;
import upismpn.download.Ucenik;

/**
 *
 * @author Luka
 */
public class UceniciGroupBuilder {
    private Predicate<UcenikWrapper> op = null;
    private static UceniciGroup everyone;

    public UceniciGroupBuilder(Predicate<UcenikWrapper> op) {
        if(op == null) {
            this.op = ucenikWrapper -> true;
        } else {
            this.op = op;
        }
    }
    
    private static void loadEveryone() {
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
        op.and(o);
    }
    
    public UceniciGroup getGroup() {
        if(everyone == null) loadEveryone();
        return new UceniciGroup(everyone).filter(op);
    }

    //grouping utilities

    public Map<UcenikWrapper.OsnovnaSkola, UceniciGroup> getByOS() {
        Map<UcenikWrapper.OsnovnaSkola, UceniciGroup> map = new HashMap<>();
        if(everyone == null) loadEveryone();
        everyone.forEach((uc) -> {
            if(map.get(uc.osInfo) == null) {
                map.put(uc.osInfo, new UceniciGroup(uc));
            } else {
                map.get(uc.osInfo).add(uc);
            }
        });
        return map;
    }

    public Map<String, UceniciGroup> getByCity() {
        Map<String, UceniciGroup> map = new HashMap<>();
        if(everyone == null) loadEveryone();
        everyone.forEach((uc) -> {
            if(map.get(uc.osInfo.mesto) == null) {
                map.put(uc.osInfo.mesto, new UceniciGroup(uc));
            } else {
                map.get(uc.osInfo.mesto).add(uc);
            }
        });
        return map;
    }
    public Map<String, UceniciGroup> getByRegion() {
        Map<String, UceniciGroup> map = new HashMap<>();
        if(everyone == null) loadEveryone();
        everyone.forEach((uc) -> {
            if(map.get(uc.osInfo.okrug) == null) {
                map.put(uc.osInfo.okrug, new UceniciGroup(uc));
            } else {
                map.get(uc.osInfo.okrug).add(uc);
            }
        });
        return map;
    }
}
