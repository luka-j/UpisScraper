package rs.lukaj.upisstats.scraper.obrada2017;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.Ucenik2017;
import rs.lukaj.upisstats.scraper.download.UcenikUtils;
import rs.lukaj.upisstats.scraper.utils.StringTokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UcenikW {
    public final int sifra;
    public final OsnovnaW osnovna;
    public final SmerW smer;
    public final int krug;
    public final int blizanacSifra;
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
    public final boolean prioritet;
    private UcenikW blizanac;

    private final Map<Object, Object> props = new HashMap<>();
    public void addProperty(Object key, Object value) {
        if(props.containsKey(key)) System.out.println("Warning: overwriting prop " + key);
        props.put(key, value);
    }
    public Object getProperty(Object key) {
        return props.get(key);
    }

    public UcenikW(Ucenik2017 uc) {
        sifra = Integer.parseInt(uc.id);
        osnovna = OsnovneBase.get(Integer.parseInt(uc.getOsId()));
        smer = SmeroviBase.get(uc.getUpisana());

        if(uc.getKrug().equals("*")) krug=-1; //upisan po odluci OUKa
        else krug = Integer.parseInt(uc.getKrug());
        //Profiler.addTime("UcenikWBasics", end-start);

        if(uc.getBlizanac().isEmpty()) blizanacSifra =0;
        else {
            blizanacSifra = Integer.parseInt(uc.getBlizanac().split("\">")[1].split("<")[0]);
            //Profiler.addTime("split", end-start);
        }

        if(uc.getNajboljiBlizanacBodovi().isEmpty()) najboljiBlizanacBodovi = 0;
        else najboljiBlizanacBodovi = Double.parseDouble(uc.getNajboljiBlizanacBodovi());

        srpski = Double.parseDouble(uc.getSrpski());
        matematika = Double.parseDouble(uc.getMatematika());
        kombinovani = Double.parseDouble(uc.getKombinovani());
        bodovaZavrsni = srpski + matematika + kombinovani;
        bodovaAM = Double.parseDouble(uc.getBodovaAM());
        ukupnoBodova = Double.parseDouble(uc.getUkupnoBodova());
        //Profiler.addTime("UcenikWBodovi", end-start);

        maternji = uc.getMaternji();
        prviStrani = uc.getPrviStrani();
        drugiStrani = uc.getDrugiStrani();

        sestiRaz = cleanOcene(uc.getSestiRaz());
        sedmiRaz = cleanOcene(uc.getSedmiRaz());
        Map<String, String> osmi = uc.getOsmiRaz();
        osmiRaz = cleanOcene(osmi);
        vukovaDiploma = Integer.parseInt(osmi.get(UcenikUtils.PredmetiDefault.VUKOVA2017)) != 0;
        prioritet = uc.isPrioritet();

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

    //unreliable
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
        int zbir=0, broj=0;
        double prosek=0, bodovi=0;

        for(Map.Entry<String, String> en : raw.entrySet()) {
            String predmet = en.getKey(), ocena = en.getValue();
            if(predmet.startsWith("prosek")) prosek = Double.parseDouble(ocena); //this was IDE suggestion I swear
            else if(predmet.startsWith("bod")) bodovi = Double.parseDouble(ocena);
            else if(predmet.equals(UcenikUtils.PredmetiDefault.ZBIR2017)) zbir = Integer.parseInt(ocena);
            else if(predmet.equals(UcenikUtils.PredmetiDefault.BROJ2017)) broj = Integer.parseInt(ocena);
            else if(!predmet.equals(UcenikUtils.PredmetiDefault.VUKOVA2017)) ocene.put(predmet, Integer.parseInt(ocena));
        }
        //Profiler.addTime("UcenikWCleanOcene", end-start);
        return new Ocene(ocene, zbir, broj, prosek, bodovi);
    }
    private static Map<String, Double> mapValuesToDouble(Map<String, String> strings) {
        Map<String, Double> doubles = new HashMap<>();
        for(Map.Entry<String, String> e : strings.entrySet())
            doubles.put(e.getKey(), Double.parseDouble(e.getValue()));
        //Profiler.addTime("UcenikWMapValuesToDouble", end-start);
        return doubles;
    }
    private static int findZelja(List<Zelja> listaZelja, SmerW upisana) {
        for(int i=0; i<listaZelja.size(); i++) {
            if(listaZelja.get(i).smer.equals(upisana)) {
                //Profiler.addTime("UcenikWFindZelja", end-start);
                return i;
            }
        }
        throw new IndexOutOfBoundsException("Ne postoji želja");

    }

    protected void setBlizanac() {
        if(blizanacSifra == 0) return;
        blizanac = UceniciBase.get(blizanacSifra);
        if(blizanac != null)
            blizanac.blizanac = this;
    }

    public UcenikW getBlizanac() {
        setBlizanac();
        return blizanac;
    }

    public static class Zelja {
        public final SmerW smer;
        public final boolean uslov;
        public final double bodovaZaUpis;

        public Zelja(Ucenik2017.Zelja zelja) {
            this.smer = SmeroviBase.get(zelja.getSifraSmera());
            this.uslov = Integer.parseInt(zelja.getUslov()) != 0;
            if(uslov)
                this.bodovaZaUpis = Double.parseDouble(zelja.getBodovaZaUpis());
            else
                bodovaZaUpis = 0;
            //Profiler.addTime("new UcenikW.Zelja", end-start);
        }

        @Override
        public String toString() {
            return smer.toString() + "(" + uslov + ")";
        }
    }
    public static class Takmicenje {
        public static final int REPUBLICKO = 1;
        public static final int MEĐUNARODNO = 2;

        public final String predmet;
        public final int nivo, mesto;
        public final int bodova;

        public Takmicenje(String predmet, int bodova) {
            StringTokenizer tk = new StringTokenizer(predmet, '~');
            this.predmet = tk.nextToken();
            this.bodova = bodova;
            String nivoStr = tk.nextToken();
            if(nivoStr.startsWith("Republičko")) nivo = REPUBLICKO;
            else {
                System.out.println("Međunarodno: " + nivoStr);
                nivo = MEĐUNARODNO; //cini mi se da ovakvih nema u '17
            }
            String mestoStr = tk.nextToken();
            switch (mestoStr) {
                case "Prvo mesto": mesto = 1; break;
                case "Drugo mesto": mesto = 2; break;
                case "Treće mesto": mesto = 3; break;
                default: throw new IllegalArgumentException("nepostojeće mesto na takmičenju: " + mestoStr);
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UcenikW && ((UcenikW)obj).sifra == sifra;
    }

    @Override
    public int hashCode() {
        return sifra;
    }
}
