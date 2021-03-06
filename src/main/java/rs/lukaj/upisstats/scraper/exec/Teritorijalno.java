package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.obrada.SmeroviBase;
import rs.lukaj.upisstats.scraper.obrada.Spreadsheets;
import rs.lukaj.upisstats.scraper.obrada.UceniciGroup;
import rs.lukaj.upisstats.scraper.obrada.UceniciGroupBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Luka on 1/9/2016.
 */
public class Teritorijalno {

    public static class StringGroup {
        private double       val;
        private String       str;
        private UceniciGroup group;

        private StringGroup(String str, UceniciGroup group, double val) {
            this.str = str;
            this.group = group;
            this.val = val;
        }

        public String getStr() {
            return str;
        }
        public UceniciGroup getGroup() {
            return group;
        }
        public double getVal() {
            return val;
        }

        private static Comparator<StringGroup> poZavrsnom = (o1, o2) -> {
            if(o1.group.getProsekNaZavrsnom() > o2.group.getProsekNaZavrsnom()) return -1;
            if(o1.group.getProsekNaZavrsnom() < o2.group.getProsekNaZavrsnom()) return 1;
            return 0;
        };
        private static Comparator<StringGroup> poOcenama  = (o1, o2) -> {
            if(o1.group.getProsekOcena() > o2.group.getProsekOcena()) return -1;
            if(o1.group.getProsekOcena() < o2.group.getProsekOcena()) return 1;
            return 0;
        };
    }
    public static void gradoviNaZavrsnom() throws IOException {
        SmeroviBase.load();
        UceniciGroupBuilder       builder = new UceniciGroupBuilder(null);
        Map<String, UceniciGroup> ucenici = builder.getByCity();
        List<StringGroup> list = ucenici.entrySet().stream().map((Map.Entry<String, UceniciGroup> en) ->
                new StringGroup(en.getKey(), en.getValue(),
                        en.getValue().getProsekNaZavrsnom())).sorted(StringGroup.poZavrsnom).collect(Collectors.toList());
        Spreadsheets.writeStringGroup(new File(DownloadController.DATA_FOLDER, "zavrsni_mesta_new.xlsx"), list, "Broj bodova");
    }

    public static void gradoviOcene() throws IOException {
        SmeroviBase.load();
        UceniciGroupBuilder builder = new UceniciGroupBuilder(null);
        Map<String, UceniciGroup> ucenici = builder.getByCity();
        List<StringGroup> list = ucenici.entrySet().stream().map((Map.Entry<String, UceniciGroup> en) ->
                new StringGroup(en.getKey(), en.getValue(), en.getValue().getProsekOcena()))
                .sorted(StringGroup.poOcenama)
                .collect(Collectors.toList());
        Spreadsheets.writeStringGroup(new File(DownloadController.DATA_FOLDER, "ocene_mesta_new.xlsx"), list, "Prosecna ocena");
    }
    public static void okruziNaZavrsnom() throws IOException {
        SmeroviBase.load();
        UceniciGroupBuilder builder = new UceniciGroupBuilder(null);
        Map<String, UceniciGroup> ucenici = builder.getByRegion();
        List<StringGroup> list = ucenici.entrySet().stream()
                .map((Map.Entry<String, UceniciGroup> en) ->
                        new StringGroup(en.getKey(), en.getValue(), en.getValue().getProsekNaZavrsnom()))
                .sorted(StringGroup.poZavrsnom)
                .collect(Collectors.toList());
        Spreadsheets.writeStringGroup(new File(DownloadController.DATA_FOLDER, "zavrsni_okruzi_new.xlsx"), list, "Broj bodova");
    }

    public static void okruziOcene() throws IOException {
        SmeroviBase.load();
        UceniciGroupBuilder builder = new UceniciGroupBuilder(null);
        Map<String, UceniciGroup> ucenici = builder.getByRegion();
        List<StringGroup> list = ucenici.entrySet().stream()
                .map((Map.Entry<String, UceniciGroup> en) ->
                        new StringGroup(en.getKey(), en.getValue(), en.getValue().getProsekOcena()))
                .sorted(StringGroup.poOcenama)
                .collect(Collectors.toList());
        Spreadsheets.writeStringGroup(new File(DownloadController.DATA_FOLDER, "ocene_okruzi_new.xlsx"), list, "Prosecna ocena");
    }
}
