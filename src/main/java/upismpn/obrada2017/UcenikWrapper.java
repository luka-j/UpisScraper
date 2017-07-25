package upismpn.obrada2017;

import upismpn.download.DownloadController;
import upismpn.download.Ucenik2017;
import upismpn.download.UcenikUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UcenikWrapper {
    public final int sifra;
    public final OsnovnaWrapper osnovna;
    public final SmerWrapper smer;
    public final int krug;
    public final int blizanac;
    public final double najboljiBlizanacBodovi;

    public final double srpski, matematika, kombinovani, bodovaZavrsni;
    public final double bodovaAM, ukupnoBodova;
    public final String maternji, prviStrani, drugiStrani;

    public final Ocene sestiRaz, sedmiRaz, osmiRaz;
    public final Map<String, Double> prijemni;
    public final Takmicenje takmicenje;
    public final double bodovaTakmicenja;
    public final List<Zelja> listaZelja1, listaZelja2;
    public final int upisanaZelja;

    public final double prosekUkupno;
    public final double bodoviOcene;
    public final boolean vukovaDiploma;

    public UcenikWrapper(Ucenik2017 uc) {
        sifra = Integer.parseInt(uc.id);
        osnovna = OsnovneBase.get(Integer.parseInt(uc.getOsId()));
        smer = SmeroviBase.get(uc.getUpisana());

        if(uc.getKrug() == null || uc.getKrug().equals("null")) krug = getKrug(uc);
        else if(uc.getKrug().equals("*")) krug=-1; //upisan po odluci OUKa
        else krug = Integer.parseInt(uc.getKrug());

        if(uc.getBlizanac().isEmpty()) blizanac=0;
        else blizanac = Integer.parseInt(uc.getBlizanac().split("\">")[1].split("<")[0]);

        if(uc.getNajboljiBlizanacBodovi().isEmpty()) najboljiBlizanacBodovi = 0;
        else najboljiBlizanacBodovi = Double.parseDouble(uc.getNajboljiBlizanacBodovi());

        srpski = Double.parseDouble(uc.getSrpski());
        matematika = Double.parseDouble(uc.getMatematika());
        kombinovani = Double.parseDouble(uc.getKombinovani());
        bodovaZavrsni = srpski + matematika + kombinovani;
        bodovaAM = Double.parseDouble(uc.getBodovaAM());
        ukupnoBodova = Double.parseDouble(uc.getUkupnoBodova());

        maternji = uc.getMaternji();
        prviStrani = uc.getPrviStrani();
        drugiStrani = uc.getDrugiStrani();

        sestiRaz = cleanOcene(uc.getSestiRaz());
        sedmiRaz = cleanOcene(uc.getSestiRaz());
        Map<String, String> osmi = uc.getOsmiRaz();
        osmiRaz = cleanOcene(osmi);
        vukovaDiploma = Integer.parseInt(osmi.get(UcenikUtils.PredmetiDefault.VUKOVA2017)) != 0;

        if(uc.getTakmicenja().isEmpty()) takmicenje = null;
        else {
            Map.Entry<String, String> tak = uc.getTakmicenja().entrySet().iterator().next();
            takmicenje = new Takmicenje(tak.getKey(), Integer.parseInt(tak.getValue()));
        }
        prijemni = mapValuesToDouble(uc.getPrijemni());
        bodovaTakmicenja = takmicenje == null ? 0 : takmicenje.bodova;

        listaZelja1 = uc.getListaZelja1().stream().map(Zelja::new).collect(Collectors.toList());
        listaZelja2 = uc.getListaZelja2().stream().map(Zelja::new).collect(Collectors.toList());

        prosekUkupno = (sestiRaz.prosekOcena + sedmiRaz.prosekOcena + osmiRaz.prosekOcena)/3;
        bodoviOcene = sestiRaz.bodovi + sedmiRaz.bodovi + osmiRaz.bodovi;

        if(krug == -1) upisanaZelja = -1;
        else if(krug == 1) upisanaZelja = findZelja(listaZelja1, smer);
        else if(krug == 2) upisanaZelja = findZelja(listaZelja2, smer);
        else throw new IllegalArgumentException("Krug nije 1 ili 2: " + krug);
    }

    private static int getKrug(Ucenik2017 uc) {
        int krug;
        if(uc.getListaZelja1().isEmpty() && uc.getListaZelja2().isEmpty()) krug = -1;
        else if(!uc.getListaZelja1().isEmpty() && uc.getListaZelja2().isEmpty()) krug = 1;
        else if(!uc.getListaZelja2().isEmpty()) krug = 2;
        else throw new RuntimeException("wtf: " + uc.id);
        uc.setDetails(uc.getUkupnoBodova(), krug>0 ? String.valueOf(krug) : "*");
        uc.saveToFile(DownloadController.DATA_FOLDER, true);
        return krug;
    }

    private static Ocene cleanOcene(Map<String, String> raw) {
        Map<String, Integer> ocene = new HashMap<>();
        final int[] zbir = new int[1];
        final int[] broj = new int[1];
        final double[] prosek = new double[1];
        final double[] bodovi = new double[1];

        raw.forEach((predmet, ocena) -> {
            if(predmet.startsWith("prosek")) prosek[0] = Double.parseDouble(ocena); //this was IDE suggestion I swear
            else if(predmet.startsWith("bod")) bodovi[0] = Double.parseDouble(ocena);
            else if(predmet.equals(UcenikUtils.PredmetiDefault.ZBIR2017)) zbir[0] = Integer.parseInt(ocena);
            else if(predmet.equals(UcenikUtils.PredmetiDefault.BROJ2017)) broj[0] = Integer.parseInt(ocena);
            else if(!predmet.equals(UcenikUtils.PredmetiDefault.VUKOVA2017)) ocene.put(predmet, Integer.parseInt(ocena));
        });
        return new Ocene(ocene, zbir[0], broj[0], prosek[0], bodovi[0]);
    }
    private static Map<String, Double> mapValuesToDouble(Map<String, String> strings) {
        Map<String, Double> doubles = new HashMap<>();
        for(Map.Entry<String, String> e : strings.entrySet())
            doubles.put(e.getKey(), Double.parseDouble(e.getValue()));
        return doubles;
    }
    private static int findZelja(List<Zelja> listaZelja, SmerWrapper upisana) {
        for(int i=0; i<listaZelja.size(); i++) {
            if(listaZelja.get(i).smer.equals(upisana))
                return i;
        }
        throw new IndexOutOfBoundsException("Ne postoji želja");
    }



    public static class Zelja {
        public final SmerWrapper smer;
        public final boolean uslov;

        public Zelja(Ucenik2017.Zelja zelja) {
            this.smer = SmeroviBase.get(zelja.getSifraSmera());
            this.uslov = Integer.parseInt(zelja.getUslov()) != 0;
        }
    }
    public static class Takmicenje {
        public static final int REPUBLICKO = 1;
        public static final int MEĐUNARODNO = 2;

        public final String predmet;
        public final int nivo, mesto;
        public final int bodova;

        public Takmicenje(String predmet, int bodova) {
            String[] info = predmet.split("~");
            this.predmet = info[0];
            this.bodova = bodova;
            if(info[1].split(",")[0].toLowerCase().startsWith("republičko")) nivo = REPUBLICKO;
            else nivo = MEĐUNARODNO; //cini mi se da ovakvih nema u '17
            switch (info[2].toLowerCase()) {
                case "prvo mesto": mesto = 1; break;
                case "drugo mesto": mesto = 2; break;
                case "treće mesto": mesto = 3; break;
                default: throw new IllegalArgumentException("nepostojeće mesto na takmičenju: " + info[2]);
            }
        }
    }
    public static class Ocene {
        public final Map<String, Integer> ocene;
        public final int zbirOcena, brojOcena;
        public final double prosekOcena, bodovi;

        public Ocene(Map<String, Integer> ocene, int zbirOcena, int brojOcena, Double prosekOcena, Double bodovi) {
            this.ocene = ocene;
            this.zbirOcena = zbirOcena;
            this.brojOcena = brojOcena;
            this.prosekOcena = prosekOcena;
            this.bodovi = bodovi;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(sifra); //todo
    }
}
