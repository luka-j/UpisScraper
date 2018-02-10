package rs.lukaj.upisstats.scraper.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Profiler {

    private static Map<String, Long> timers = new HashMap<>();

    public static void addTime(String key, long time) {
        long curr = timers.getOrDefault(key, (long) 0);
        timers.put(key, curr+time);
    }

    public static void print() {
        timers.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> {
            System.out.println(e.getKey() + ": " + ((double)e.getValue())*0.000000001 + "s");
        });
    }

    /*
     * Output on brojUcenika:
     *
     * SmeroviLoadFromFileDisk: 2.6471E-5s
     * new SmerW: 0.0046901930000000005s
     * new Osnovna2017: 0.0064356230000000006s
     * OsnovneBaseLoadDisk: 0.0073481020000000005s
     * UcenikWMapValuesToDouble: 0.009785631000000001s
     * UcenikWFindZelja: 0.015638088s
     * split: 0.024688296000000002s
     * OsnovneBaseLoadTotal: 0.031208673000000003s
     * loopSaveUcenik2017: 0.045013417s
     * UcenikWBodovi: 0.046724203000000006s
     * loopMakeUcenikOld: 0.049214374000000005s
     * UcenikWrapperOceneAverage: 0.067940498s
     * UcenikWBasics: 0.07639106600000001s
     * UceniciBaseLoadSkole: 0.097528377s
     * UcenikWrapperZavrsni: 0.10683941400000001s
     * loopMakeUcenik2017: 0.143346618s
     * clear&load smerovi: 0.143407651s
     * SmeroviLoadFromFileTotal: 0.15847760400000002s
     * new UcenikW.Zelja: 0.275242329s
     * UcenikWCleanOcene: 0.394302185s
     * UcenikWrapperOsnovna: 0.405161164s
     * UceniciBaseLoadDisk: 1.1053445610000001s
     * UcenikWrapperOcene: 1.140177358s
     * loopWrapUcenik2017: 1.2451902140000002s
     * new UcenikWrapper.SrednjaSkola: 2.119996735s
     * loopLoadUcenik2017: 2.672833708s
     * UcenikWrapperZelje: 2.7946135990000003s
     * UceniciBaseMainLoop: 4.150348883s
     * loadUceniciDiskOld: 4.436989968000001s
     * loopWrapUcenikOld: 4.986127837000001s
     * UceniciBaseLoadTotal: 5.268280349s
     * 2017 total: 5.385939747s
     * UcenikLoadFromStringOld: 6.628613753000001s
     * loopLoadUcenikOld: 7.136605973000001s
     * 2016 total: 7.1821736970000005s
     * 2015 total: 9.876206584s
     * loadEveryoneTotalOld: 16.691987039s
     */
}
