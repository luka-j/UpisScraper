package rs.lukaj.upisstats.scraper.obrada;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.Ucenik;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 * @author Luka
 */
public class UceniciGroupBuilder {
    private Predicate<UcenikWrapper> op;
    private static UceniciGroup everyone;

    public UceniciGroupBuilder(Predicate<UcenikWrapper> op) {
        if(op == null) {
            this.op = u -> true;
        } else {
            this.op = op;
        }
    }
    
    private static void loadEveryone() {
        Ucenik uc;
        everyone = new UceniciGroup();
        List<String> ucenici = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        for(String ucStr : ucenici) {
            uc = new Ucenik(ucStr.substring(0, 6));
            uc.loadFromString(ucStr.substring(7));
            everyone.add(new UcenikWrapper(uc));
        }
    }

    public static void clearCache() {
        everyone = null;
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
