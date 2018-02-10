package rs.lukaj.upisstats.scraper.obrada;

import rs.lukaj.upisstats.scraper.download.Ucenik;
import rs.lukaj.upisstats.scraper.download.UcenikUtils;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Predstavlja grupu UcenikW-a (HashSet) sa nekim korisnim metodama
 * @author Luka
 */
public class UceniciGroup extends HashSet<UcenikWrapper> {

    public static UceniciGroup svi() {
        UceniciGroupBuilder.clearCache();
        Ucenik.Skola.clearCache();
        SmeroviBase.load();
        //Profiler.addTime("clear&load smerovi", end-start);
        return new UceniciGroupBuilder(null).getGroup();
    }

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
        return this.stream().mapToDouble((uc) -> uc.prosekUkupno).average().getAsDouble();
    }

    public double getProsekIzSkole() {
        return this.stream().mapToDouble((uc) -> uc.bodoviIzSkole).average().getAsDouble();
    }

    public double getProsekNaZavrsnom() {
        return this.stream().mapToDouble(uc -> uc.bodoviSaZavrsnog).average().getAsDouble();
    }

    public double getProsekIzMatematike() {
        return this.stream().mapToDouble(uc -> uc.matematika).average().getAsDouble();
    }

    public double getProsekIzSrpskog() {
        return this.stream().mapToDouble(uc -> uc.srpski).average().getAsDouble();
    }

    public double getProsekNaKombinovanom() {
        return this.stream().mapToDouble(uc -> uc.kombinovani).average().getAsDouble();
    }

    public UceniciGroup filterOdlicne(String predmet) {
        return new UceniciGroup(this.stream().filter((UcenikWrapper uc) -> uc.getProsekIz(predmet) > 4.5).collect(Collectors.toSet()));
    }

    public UceniciGroup filterOdlicneMatematika() {
        return filterOdlicne(UcenikUtils.PredmetiDefault.MATEMATIKA);
    }

    public UceniciGroup filterOdlicneSrpski() {
        return filterOdlicne(UcenikUtils.PredmetiDefault.SRPSKI);
    }

    public UceniciGroup filterOdlicneKombinovani() {
        return new UceniciGroup(this.stream()
                                    .filter((UcenikWrapper uc) -> (uc.getProsekIz(UcenikUtils.PredmetiDefault.FIZIKA)
                + uc.getProsekIz(UcenikUtils.PredmetiDefault.HEMIJA)
                + uc.getProsekIz(UcenikUtils.PredmetiDefault.BIOLOGIJA)
                + uc.getProsekIz(UcenikUtils.PredmetiDefault.ISTORIJA)
                + uc.getProsekIz(UcenikUtils.PredmetiDefault.GEOGRAFIJA)) / 5 > 4.5).collect(Collectors.toSet()));
    }

    public List<UcenikWrapper> sortBy(Comparator<UcenikWrapper> c) {
        List<UcenikWrapper> list = new LinkedList<>(this);
        list.sort(c);
        return list;
    }

}
