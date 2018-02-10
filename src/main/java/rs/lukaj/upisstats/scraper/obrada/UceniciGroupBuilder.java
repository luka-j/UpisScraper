package rs.lukaj.upisstats.scraper.obrada;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.Ucenik;
import rs.lukaj.upisstats.scraper.utils.Profiler;

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
        long startTotal = System.nanoTime();
        Ucenik uc;
        everyone = new UceniciGroup();
        long start = System.nanoTime();
        List<String> ucenici = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        long end = System.nanoTime();
        Profiler.addTime("loadUceniciDiskOld", end-start);
        for(String ucStr : ucenici) {
            long t1 = System.nanoTime();
            uc = new Ucenik(ucStr.substring(0, 6));
            long t2 = System.nanoTime();
            uc.loadFromString(ucStr.substring(7));
            long t3 = System.nanoTime();
            everyone.add(new UcenikWrapper(uc));
            long t4 = System.nanoTime();
            Profiler.addTime("loopMakeUcenikOld", t2-t1);
            Profiler.addTime("loopLoadUcenikOld", t3-t2);
            Profiler.addTime("loopWrapUcenikOld", t4-t3);
        }
        long endTotal = System.nanoTime();
        Profiler.addTime("loadEveryoneTotalOld", endTotal-startTotal);
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
