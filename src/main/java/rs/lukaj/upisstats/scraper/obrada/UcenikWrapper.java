package rs.lukaj.upisstats.scraper.obrada;

import rs.lukaj.upisstats.scraper.download.DownloadController;
import rs.lukaj.upisstats.scraper.download.Ucenik;
import rs.lukaj.upisstats.scraper.download.UcenikUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Wrapper za ucenika koji je pogodan za izracunavanja (castovano ono sto moze, preracunate neke osobine i sl).
 * Sva polja su public i final
 * Created by Luka on 1/9/2016.
 */
public class UcenikWrapper {

    public static class Takmicenje {

        static final int REPUBLIČKO  = 1;
        static final int MEĐUNARODNO = 2;
        public final String predmet;
        public final int    mesto, rang;

        Takmicenje(String predmet, int bodova) {
            this.predmet = predmet.toLowerCase().trim();
            switch (bodova) {
                case 20:
                    mesto = 1;
                    rang = Takmicenje.MEĐUNARODNO;
                    break;
                case 18:
                    mesto = 2;
                    rang = Takmicenje.MEĐUNARODNO;
                    break;
                case 14:
                    mesto = 3;
                    rang = Takmicenje.MEĐUNARODNO;
                    break;
                case 8:
                    mesto = 1;
                    rang = Takmicenje.REPUBLIČKO;
                    break;
                case 6:
                    mesto = 2;
                    rang = Takmicenje.REPUBLIČKO;
                    break;
                case 4:
                    mesto = 3;
                    rang = Takmicenje.REPUBLIČKO;
                    break;
                default: mesto = rang = 0;
            }
        }

        @Override
        public String toString() {
            return predmet + ": " + mesto + " na " + (rang == REPUBLIČKO ? "republičkom" : "međunarodnom") + " takmičenju";
        }
    }

    public static class SrednjaSkola {

        public final String sifra, ime, mesto, okrug, smer, podrucje;
        public final int kvota;

        SrednjaSkola(Ucenik.Skola s) {
            sifra = s.sifra.toLowerCase().trim();
            ime = s.ime.toLowerCase().trim();
            mesto = s.mesto.toLowerCase().trim();
            smer = s.smer.toLowerCase().trim();
            podrucje = SmeroviBase.getPodrucje(s.sifra).toLowerCase().trim();
            okrug = SmeroviBase.getOkrug(s.sifra);
            kvota = SmeroviBase.getKvota(s.sifra);
        }

        @Override
        public boolean equals(Object e) {
            if (e == null || !(e instanceof SrednjaSkola)) {
                return false;
            } else {
                return ((SrednjaSkola) e).sifra.equals(this.sifra);
            }
        }

        @Override
        public int hashCode() {
            return sifra.hashCode();
        }

        @Override
        public String toString() {
            return ime + ", " + mesto + ", " + okrug + " okrug. Smer: " + smer + ", područje rada " + podrucje + ". Kvota: " + kvota;
        }
    }

    public static class OsnovnaSkola {

        public final String ime, mesto, okrug;

        OsnovnaSkola(String name, String mesto, String okrug) {
            this.ime = name;
            this.mesto = mesto;
            this.okrug = okrug;
        }

        @Override
        public boolean equals(Object e) {
            if (!(e instanceof OsnovnaSkola)) {
                return false;
            }
            OsnovnaSkola os = (OsnovnaSkola) e;
            return this.ime.equalsIgnoreCase(os.ime)
                    && this.mesto.equalsIgnoreCase(os.mesto)
                    && this.okrug.equalsIgnoreCase(os.okrug);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + Objects.hashCode(this.ime);
            hash = 83 * hash + Objects.hashCode(this.mesto);
            hash = 83 * hash + Objects.hashCode(this.okrug);
            return hash;
        }

        @Override
        public String toString() {
            return ime + ", " + mesto + ", " + okrug + " okrug";
        }
    }

    public final int id;

    public final OsnovnaSkola osInfo;

    public final Map<String, Integer> sestiRaz;
    public final double               prosekSesti;
    public final Map<String, Integer> sedmiRaz;
    public final double               prosekSedmi;
    public final Map<String, Integer> osmiRaz;
    public final double               prosekOsmi;
    public final double               prosekUkupno;
    public final double               bodoviIzSkole;

    public final Map<Takmicenje, Integer> takmicenja;

    public final double matematika;
    public final double srpski;
    public final double kombinovani;
    public final double bodoviSaZavrsnog;

    public final double ukupnoBodova;
    public final double bodoviSaPrijemnog;

    public final List<SrednjaSkola> listaZelja;
    public final int                brojZelja;
    public final SrednjaSkola       upisanaSkola;
    public final int                upisanaZelja;
    public final int                krug;

    public UcenikWrapper(String id) {
        this(new Ucenik(id).loadFromFile(DownloadController.DATA_FOLDER));
    }

    public UcenikWrapper(Ucenik uc) {
        this.id = Integer.parseInt(uc.id);

        String osnovnaSkola = uc.getOsnovnaSkola().toLowerCase().trim();
        String mestoOS      = uc.getMestoOS().toLowerCase().trim();
        String okrugOS      = uc.getOkrugOS().toLowerCase().trim();
        osInfo = new OsnovnaSkola(osnovnaSkola, mestoOS, okrugOS);

        sestiRaz = new HashMap<>();
        for (Map.Entry<String, String> e : uc.getSestiRaz().entrySet()) {
            sestiRaz.put(e.getKey(), Integer.parseInt(e.getValue()));
        }
        sedmiRaz = new HashMap<>();
        for (Map.Entry<String, String> e : uc.getSedmiRaz().entrySet()) {
            sedmiRaz.put(e.getKey(), Integer.parseInt(e.getValue()));
        }
        osmiRaz = new HashMap<>();
        for (Map.Entry<String, String> e : uc.getOsmiRaz().entrySet()) {
            osmiRaz.put(e.getKey(), Integer.parseInt(e.getValue()));
        }

        prosekSesti = sestiRaz.values().stream().mapToInt((Integer i) -> i).average().getAsDouble();
        prosekSedmi = sedmiRaz.values().stream().mapToInt((Integer i) -> i).average().getAsDouble();
        prosekOsmi = osmiRaz.values().stream().mapToInt((Integer i) -> i).average().getAsDouble();
        prosekUkupno = (prosekSesti + prosekSedmi + prosekOsmi) / 3;
        bodoviIzSkole = prosekSesti * 4 + prosekSedmi * 5 + prosekOsmi * 5;

        takmicenja = new HashMap<>();
        for (Map.Entry<String, String> e : uc.getTakmicenja().entrySet()) {
            takmicenja.put(new Takmicenje(e.getKey(), Integer.parseInt(e.getValue())),
                           Integer.parseInt(e.getValue()));
        }

        matematika = uc.getMatematika().equals("null") ? 0 : Double.parseDouble(uc.getMatematika());
        srpski = uc.getSrpski().equals("null") ? 0 : Double.parseDouble(uc.getSrpski());
        kombinovani = uc.getKombinovani().equals("null") ? 0 : Double.parseDouble(uc.getKombinovani());

        bodoviSaZavrsnog = matematika + srpski + kombinovani;
        if(uc.getUkupnoBodova().equals("*")) {
            ukupnoBodova = bodoviIzSkole + bodoviSaZavrsnog;
        } else {
            ukupnoBodova = Double.parseDouble(uc.getUkupnoBodova());
        }
        bodoviSaPrijemnog = (float) (ukupnoBodova - (bodoviIzSkole + bodoviSaZavrsnog));

        listaZelja = uc.getListaZelja().stream().map(SrednjaSkola::new).collect(Collectors.toList());
        brojZelja = listaZelja.size();
        upisanaSkola = new SrednjaSkola(uc.getUpisanaSkola());
        upisanaZelja = Integer.parseInt(uc.getUpisanaZelja());
        krug = Integer.parseInt(uc.getKrug());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UcenikWrapper && ((UcenikWrapper) o).id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public double getProsekIz(String predmet) {
        if (predmet.equals(UcenikUtils.PredmetiDefault.HEMIJA)) {
            return (double) sedmiRaz.get(predmet)
                    + osmiRaz.get(predmet) / 2;
        }
        return ((double) sestiRaz.get(predmet)
                + sedmiRaz.get(predmet)
                + osmiRaz.get(predmet)) / 3;
    }

    @Override
    public String toString() {
        return "Šifra: " + id + "\n" +
                "Osnovna škola: " + osInfo.toString() + "\n---\n" +
                "Šesti razred: " + sestiRaz.toString() + "\n" +
                "Prosek " + prosekSesti + "\n" +
                "Sedmi razred: " + sedmiRaz.toString() + "\n" +
                "Prosek " + prosekSedmi + "\n" +
                "Osmi razred: " + osmiRaz.toString() + "\n" +
                "Prosek " + prosekOsmi + "\n" +
                "Prosek ukupno: " + prosekUkupno + "\n---\n" +
                "Bodova iz škole " + bodoviIzSkole + "\n" +
                "Matematika završni: " + matematika + "\n" +
                "Srpski završni: " + srpski + "\n" +
                "Kombinovani završni: " + kombinovani + "\n" +
                "Ukupno završni: " + bodoviSaZavrsnog + "\n" +
                "Prijemni: " + bodoviSaPrijemnog + "\n" +
                "Takmičenja" + takmicenja.toString() + "\n" +
                "Ukupno bodova: " + ukupnoBodova + "\n---\n" +
                "Upisana škola: " + upisanaSkola.toString() + "\n" +
                "Broj iskazanih želja: " + brojZelja + "\n" +
                "Lista želja: " + listaZelja.toString() + "\n" +
                "Upisana želja: " + upisanaZelja + "\n" +
                "Upisan u " + krug + ". krugu";
    }
}
