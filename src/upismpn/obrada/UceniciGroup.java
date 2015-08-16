package upismpn.obrada;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import upismpn.download.UceniciManager;
import upismpn.download.Ucenik;
import upismpn.download.Ucenik.Skola;
import upismpn.download.UcenikUtils;
import upismpn.obrada.UceniciGroup.UcenikWrapper;

/**
 *
 * @author Luka
 */
public class UceniciGroup extends HashSet<UcenikWrapper> {

    UceniciGroup(Set<UcenikWrapper> group) {
        addAll(group);
    }

    UceniciGroup(String id) {
        add(id);
    }

    UceniciGroup(UcenikWrapper uc) {
        this.add(uc);
    }

    UceniciGroup() {
        super();
    }

    public void add(String id) {
        this.add(new UcenikWrapper(id));
    }

    public void unija(UceniciGroup ug) {
        this.addAll(ug);
    }

    public void presek(UceniciGroup ug) {
        this.forEach((uc) -> {
            if (!ug.contains(uc)) {
                this.remove(uc);
            }
        });
    }

    UcenikWrapper reduce(BinaryOperator<UcenikWrapper> f) {
        return this.stream().reduce(f).orElse(null);
    }

    UceniciGroup filter(Predicate<UcenikWrapper> p) {
        return new UceniciGroup(this.stream().filter(p).collect(Collectors.toSet()));
    }

    public double getProsekOcena() {
        return this.stream().mapToDouble((uc) -> {
            return uc.prosekUkupno;
        }).average().getAsDouble();
    }

    public double getProsekIzSkole() {
        return this.stream().mapToDouble((uc) -> {
            return uc.bodoviIzSkole;
        }).average().getAsDouble();
    }

    public double getProsekNaZavrsnom() {
        return this.stream().mapToDouble(uc -> {
            return uc.bodoviSaZavrsnog;
        }).average().getAsDouble();
    }

    public double getProsekIzMatematike() {
        return this.stream().mapToDouble(uc -> {
            return uc.matematika;
        }).average().getAsDouble();
    }

    public double getProsekIzSrpskog() {
        return this.stream().mapToDouble(uc -> {
            return uc.srpski;
        }).average().getAsDouble();
    }

    public double getProsekNaKombinovanom() {
        return this.stream().mapToDouble(uc -> {
            return uc.kombinovani;
        }).average().getAsDouble();
    }

    public UceniciGroup filterOdlicne(String predmet) {
        return new UceniciGroup(this.stream().filter((UcenikWrapper uc) -> {
            return uc.getProsekIz(UcenikUtils.PredmetiDefault.MATEMATIKA) > 4.5;
        }).collect(Collectors.toSet()));
    }

    public UceniciGroup filterOdlicneMatematika() {
        return filterOdlicne(UcenikUtils.PredmetiDefault.MATEMATIKA);
    }

    public UceniciGroup filterOdlicneSrpski() {
        return filterOdlicne(UcenikUtils.PredmetiDefault.SRPSKI);
    }

    public UceniciGroup filterOdlicneKombinovani() {
        return new UceniciGroup(this.stream().filter((UcenikWrapper uc) -> {
            return (uc.getProsekIz(UcenikUtils.PredmetiDefault.FIZIKA)
                    + uc.getProsekIz(UcenikUtils.PredmetiDefault.HEMIJA)
                    + uc.getProsekIz(UcenikUtils.PredmetiDefault.BIOLOGIJA)
                    + uc.getProsekIz(UcenikUtils.PredmetiDefault.ISTORIJA)
                    + uc.getProsekIz(UcenikUtils.PredmetiDefault.GEOGRAFIJA)) / 5 > 4.5;
        }).collect(Collectors.toSet()));
    }

    static class UcenikWrapper {

        static class Takmicenje {

            static final int REPUBLIČKO = 1;
            static final int MEĐUNARODNO = 2;
            String predmet;
            int mesto, rang;

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
                }

            }
        }

        static class SrednjaSkola {

            final String sifra, ime, mesto, okrug, smer, podrucje;
            final int kvota;

            SrednjaSkola(Skola s) {
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
        }

        static class OsnovnaSkola {

            final String ime, mesto, okrug;

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
        }

        final int id;

        final OsnovnaSkola osInfo;

        final Map<String, Integer> sestiRaz;
        final double prosekSesti;
        final Map<String, Integer> sedmiRaz;
        final double prosekSedmi;
        final Map<String, Integer> osmiRaz;
        final double prosekOsmi;
        final double prosekUkupno;
        final double bodoviIzSkole;

        final Map<Takmicenje, Integer> takmicenja;

        final float matematika;
        final float srpski;
        final float kombinovani;
        final float bodoviSaZavrsnog;

        final float ukupnoBodova;
        final float bodoviSaPrijemnog;

        final List<SrednjaSkola> listaZelja;
        final int brojZelja;
        final SrednjaSkola upisanaSkola;
        final int upisanaZelja;
        final int krug;

        UcenikWrapper(String id) {
            this(new Ucenik(id).loadFromFile(UceniciManager.DATA_FOLDER));
        }

        UcenikWrapper(Ucenik uc) {
            this.id = Integer.parseInt(uc.id);

            String osnovnaSkola = uc.getOsnovnaSkola().toLowerCase().trim();
            String mestoOS = uc.getMestoOS().toLowerCase().trim();
            String okrugOS = uc.getOkrugOS().toLowerCase().trim();
            osInfo = new OsnovnaSkola(osnovnaSkola, mestoOS, okrugOS);

            sestiRaz = new HashMap<>();
            for (Entry<String, String> e : uc.getSestiRaz().entrySet()) {
                sestiRaz.put(e.getKey(), Integer.parseInt(e.getValue()));
            }
            sedmiRaz = new HashMap<>();
            for (Entry<String, String> e : uc.getSedmiRaz().entrySet()) {
                sedmiRaz.put(e.getKey(), Integer.parseInt(e.getValue()));
            }
            osmiRaz = new HashMap<>();
            for (Entry<String, String> e : uc.getOsmiRaz().entrySet()) {
                osmiRaz.put(e.getKey(), Integer.parseInt(e.getValue()));
            }

            prosekSesti = sestiRaz.values().stream().mapToInt((Integer i) -> {
                return i;
            }).average().getAsDouble();
            prosekSedmi = sedmiRaz.values().stream().mapToInt((Integer i) -> {
                return i;
            }).average().getAsDouble();
            prosekOsmi = osmiRaz.values().stream().mapToInt((Integer i) -> {
                return i;
            }).average().getAsDouble();
            prosekUkupno = (prosekSesti + prosekSedmi + prosekOsmi) / 3;
            bodoviIzSkole = prosekSesti * 4 + prosekSedmi * 5 + prosekOsmi * 5;

            takmicenja = new HashMap<>();
            for (Entry<String, String> e : uc.getTakmicenja().entrySet()) {
                takmicenja.put(new Takmicenje(e.getKey(), Integer.parseInt(e.getValue())), Integer.parseInt(e.getValue()));
            }

            matematika = Float.parseFloat(uc.getMatematika());
            srpski = Float.parseFloat(uc.getSrpski());
            kombinovani = Float.parseFloat(uc.getKombinovani());

            bodoviSaZavrsnog = matematika + srpski + kombinovani;
            ukupnoBodova = Float.parseFloat(uc.getUkupnoBodova());
            bodoviSaPrijemnog = (float) (ukupnoBodova - (bodoviIzSkole + bodoviSaZavrsnog));

            listaZelja = uc.getListaZelja().stream().map((Skola s) -> {
                return new SrednjaSkola(s);
            }).collect(Collectors.toList());
            brojZelja = listaZelja.size();
            upisanaSkola = new SrednjaSkola(uc.getUpisanaSkola());
            upisanaZelja = Integer.parseInt(uc.getUpisanaZelja());
            krug = Integer.parseInt(uc.getKrug());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof UcenikWrapper)) {
                return false;
            }
            return ((UcenikWrapper) o).id == this.id;
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
    }
}
