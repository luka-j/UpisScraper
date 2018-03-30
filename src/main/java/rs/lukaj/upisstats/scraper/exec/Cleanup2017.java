package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.download.*;
import rs.lukaj.upisstats.scraper.obrada.FileMerger;
import rs.lukaj.upisstats.scraper.obrada2017.SmerW;
import rs.lukaj.upisstats.scraper.obrada2017.SmeroviBase;
import rs.lukaj.upisstats.scraper.obrada2017.UceniciBase;
import rs.lukaj.upisstats.scraper.obrada2017.UcenikW;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Cleanup2017 {
    public static void test() {
        UceniciBase.load();
        System.out.print("Međunarodna: ");
        System.out.println(UceniciBase.svi().filter(uc -> uc.takmicenje != null && uc.takmicenje.nivo == 2).findAny());
        System.out.print("0 bodova takmičenja: ");
        System.out.println(UceniciBase.svi().filter(uc -> uc.takmicenje != null && uc.bodovaTakmicenja ==0).count());
        System.out.print("krugovi: ");
        System.out.print(UceniciBase.svi().filter(uc -> uc.krug == -1).count());
        System.out.print(" / ");
        System.out.print(UceniciBase.svi().filter(uc -> uc.krug == 1).count());
        System.out.print(" / ");
        System.out.print(UceniciBase.svi().filter(uc -> uc.krug == 2).count());
        //UceniciBase.svi().filter(uc -> uc.sedmiRaz.brojOcena < 8).collect(Collectors.toList()).forEach(uc -> System.out.println(uc.sifra + " " + uc.sedmiRaz.brojOcena + " " + uc.sedmiRaz.prosekOcena));
    }

    public static void reloadSmerovi17() {
        Smerovi2017 smerovi = Smerovi2017.getInstance();
        smerovi.loadFromFile();
        smerovi.iterate(0);
        while(smerovi.hasNext()) {
            Smer2017 smer = (Smer2017) smerovi.getNext();
            smer.loadFromJson();
        }
        smerovi.save();
    }

    public static void sviPredmeti17() {
        UceniciBase.load();
        Set<String> predmeti = new HashSet<>();
        UceniciBase.svi().map(uc -> uc.osmiRaz.ocene.keySet()).forEach(predmeti::addAll);
        System.out.println(predmeti);
    }

    public static void reloadUcenici17() {
        List<String> strings = FileMerger.readFromOne(new File(DownloadController.DATA_FOLDER, FileMerger.FILENAME));
        Set<Ucenik2017> ucenici = new HashSet<>();
        for(String ucStr : strings) {
            String[] ucData = ucStr.split("\\n", 2);
            Ucenik2017 uc = new Ucenik2017(ucData[0]);
            ucenici.add(uc);
            try {
                uc.loadFromJson();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ucenici.forEach(uc -> uc.saveToFile(DownloadController.DATA_FOLDER, true));
        FileMerger.mergeToOne(DownloadController.DATA_FOLDER);
    }

    public static void verify17() {
        UceniciBase.load();
        System.out.println("Loaded");

        Map<SmerW, Map<Integer, TreeSet<UcenikW>>> upisani = new HashMap<>(); //smer -> krug -> br ucenika
        UceniciBase.svi().forEach(uc -> {
            if(!upisani.containsKey(uc.smer)) upisani.put(uc.smer, new HashMap<>());
            Map<Integer, TreeSet<UcenikW>> upisaniSmer = upisani.get(uc.smer);
            if(!upisaniSmer.containsKey(uc.krug)) upisaniSmer.put(uc.krug, new TreeSet<>(Comparator.comparingInt(o -> o.sifra)));
            upisaniSmer.get(uc.krug).add(uc);
        });

        System.out.println("Done counting");

        TreeSet<UcenikW> empty = new TreeSet<>();
        Collection<SmerW> sviSmerovi = SmeroviBase.getAll();
        int wrong = 0;
        for(SmerW smer : sviSmerovi) {
            if(upisani.get(smer) == null && smer.upisano1k == 0 && smer.upisano2k == 0) continue; //prazni smerovi: postoje

            if(smer.upisano1k != upisani.get(smer).getOrDefault(1, empty).size()) {
                wrong++;
                System.out.println("Greska: " + smer + "(" + smer.sifra + "), 2k: ocekivano " + smer.upisano1k +", upisano " + upisani.get(smer).get(1).size());
            }
            if(smer.upisano2k != upisani.get(smer).getOrDefault(2, empty).size()) {
                wrong++;
                System.out.println("Greska: " + smer + ", 2k: ocekivano " + smer.upisano2k +", upisano " + upisani.get(smer).get(2).size());
            }
        }
        System.out.println("\nUkupna greska: " + wrong);
    }

    public static void correct17() {
        Smerovi2017.getInstance().load();
        SmeroviBase.load();
        String[] faileds = new String[] {"BGNB GB 4R03S", "PMCU SB 4O13S", "JBNS SF 4O01S", "JALE GA 4R04S", "NINI SI 4O13S", "NINI SE 4L01S", "BGVR GA 4R04S", "JNVR GA 4R01S", "JNVR GA 4R04S", "BGNB GA 4R04S", "BGCU GA 4R04S", "JBNS SF 4O13S", "JBNS GC 4R04S"};
        StudentDownloader2017 downloader = StudentDownloader2017.getInstance(0, 0);
        for(String failed : faileds) {
            System.out.println(failed);
            Deque<UceniciManager.UcData> data = downloader.getSifreUcenika(failed);
            for(UceniciManager.UcData d : data) {
                Ucenik2017 uc = new Ucenik2017(d.sifra);
                if(!uc.exists()) {
                    System.out.println("Found missing! " + uc.id);
                    try {
                        uc.setDetails(d.ukBodova, d.mestoOS).loadFromNet().saveToFile(DownloadController.DATA_FOLDER);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Done");
    }

    public static void manjeod2() {
        DownloadController.DATA_FOLDER = DownloadController.generateDataFolder("17");
        List<UcenikW> svi = UceniciBase.svi().collect(Collectors.toList());
        for(UcenikW uc : svi) {
            for(Map.Entry<String, Integer> oc : uc.sestiRaz.ocene.entrySet())
                if(oc.getValue() == 1)
                    System.out.println("Ucenik " + uc.sifra + ", predmet " + oc.getKey() + ":" + oc.getValue());
            for(Map.Entry<String, Integer> oc : uc.sedmiRaz.ocene.entrySet())
                if(oc.getValue() == 1)
                    System.out.println("Ucenik " + uc.sifra + ", predmet " + oc.getKey() + ":" + oc.getValue());
            for(Map.Entry<String, Integer> oc : uc.osmiRaz.ocene.entrySet())
                if(oc.getValue() == 1)
                    System.out.println("Ucenik " + uc.sifra + ", predmet " + oc.getKey() + ":" + oc.getValue());
        }
    }
}
