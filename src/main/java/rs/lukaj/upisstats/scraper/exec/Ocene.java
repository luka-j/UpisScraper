package rs.lukaj.upisstats.scraper.exec;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import rs.lukaj.upisstats.scraper.obrada.UceniciGroup;
import rs.lukaj.upisstats.scraper.obrada.UceniciGroupBuilder;
import rs.lukaj.upisstats.scraper.obrada.UcenikWrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Luka on 1/9/2016.
 */
public class Ocene {
    public static void koeficijentBlejacnosti() { //Balsina ideja
        Map<UcenikWrapper.OsnovnaSkola, UceniciGroup> skole = new UceniciGroupBuilder(null).getByOS(); //ucenici po osnovnim skolama
        System.out.println("dataset: " + skole.size()); //ispisuje broj osnovnih skola
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE, k; //deklaracije
        UcenikWrapper.OsnovnaSkola maxOS = null, minOS = null;
        for (Map.Entry<UcenikWrapper.OsnovnaSkola, UceniciGroup> e : skole.entrySet()) { //za svaku grupu (svaku osnovnu sk)
            if (e.getValue().size() > 25) { //ako je vise od 25 ucenika upisalo srednju, da preskocimo one seoske i sl.
                k = e.getValue().getProsekIzSkole() / e.getValue().getProsekNaZavrsnom(); //izracunavam koeficijent
                if (k > max) { //ako je veci od najveceg
                    max = k; //pamtim koeficijent
                    maxOS = e.getKey(); //i skolu
                }
                if (k < min) { //slicno kao ovo gore
                    min = k;
                    minOS = e.getKey();
                }
            }
        }
        System.out.println("Najveci koeficijent blejacnosti ima skola "
                                   + maxOS.ime + " iz " + maxOS.mesto + " (" + maxOS.okrug + " okrug)"
                                   + "i on iznosi " + max); //ispis rezultata
        System.out.println("Najmanje koeficijent blejacnosti ima skola "
                                   + minOS.ime + " iz " + minOS.mesto + " (" + minOS.okrug + " okrug)"
                                   + "i on iznosi " + min);
    }

    private static class OSKriterijum {

        UcenikWrapper.OsnovnaSkola os;
        double                     srpski, matematika, kombinovani;

        public OSKriterijum(UcenikWrapper.OsnovnaSkola os, double srpski, double matematika, double kombinovani) {
            this.os = os;
            this.srpski = srpski;
            this.matematika = matematika;
            this.kombinovani = kombinovani;
        }
    }


    public static void za5() {
        if(Config.DEBUG) System.out.println("Building groups");
        long time = System.currentTimeMillis();
        Map<UcenikWrapper.OsnovnaSkola, UceniciGroup> skole  = new UceniciGroupBuilder(null).getByOS();
        long end = System.currentTimeMillis();
        if(Config.DEBUG) System.out.println("Done Building. Time: " + (end-time));
        double                                        maxMat = Double.MIN_VALUE, maxSrp = Double.MIN_VALUE, maxKom = Double.MIN_VALUE;
        double                                        minMat = Double.MAX_VALUE, minSrp = Double.MAX_VALUE, minKom = Double.MIN_VALUE;
        UcenikWrapper.OsnovnaSkola                    maxMos = null, minMos = null, maxSos = null, minSos = null, maxKos = null, minKos = null;
        System.out.println("dataset: " + skole.size());
        List<OSKriterijum> sk = new LinkedList<>();
        skole.entrySet().stream().forEach((e) -> {
            if (e.getValue().size() > 30) {
                double srpski = e.getValue().filterOdlicneSrpski().size() == 0
                        ? -1 : e.getValue().filterOdlicneSrpski().getProsekIzSrpskog() * 10;
                double matematika = e.getValue().filterOdlicneMatematika().size() == 0
                        ? -1 : e.getValue().filterOdlicneMatematika().getProsekIzMatematike() * 10;
                double kombinovani = e.getValue().filterOdlicneKombinovani().size() == 0
                        ? -1 : e.getValue().filterOdlicneKombinovani().getProsekNaKombinovanom() * 10;
                sk.add(new OSKriterijum(e.getKey(), srpski, matematika, kombinovani));
            }
        });
        for (OSKriterijum e : sk) {
            if (e.matematika > maxMat) {
                maxMat = e.matematika;
                maxMos = e.os;
            }
            if (e.srpski > maxSrp) {
                maxSrp = e.srpski;
                maxSos = e.os;
            }
            if (e.kombinovani > maxKom) {
                maxKom = e.kombinovani;
                maxKos = e.os;
            }
            if (e.matematika < maxMat && e.matematika > 0) {
                minMat = e.matematika;
                minMos = e.os;
            }
            if (e.srpski < maxSrp && e.srpski > 0) {
                minSrp = e.srpski;
                minSos = e.os;
            }
            if (e.kombinovani < maxKom && e.kombinovani > 0) {
                minKom = e.kombinovani;
                minKos = e.os;
            }
        }
        if(Config.DEBUG)System.out.println("Found max in " + (end-time));
        System.out.println("Najvise za 5 iz srpskog treba u skoli " + maxSos.ime
                                   + " iz " + maxSos.mesto + " (" + maxSos.okrug + " okrug) i to "
                                   + maxSrp + "%");
        System.out.println("Najmanje za 5 iz srpskog treba u skoli " + minSos.ime
                                   + " iz " + minSos.mesto + " (" + minSos.okrug + " okrug) i to "
                                   + minSrp + "%");
        System.out.println("Najvise za 5 iz matematike treba u skoli " + maxMos.ime
                                   + " iz " + maxMos.mesto + " (" + maxMos.okrug + " okrug) i to "
                                   + maxMat + "%");
        System.out.println("Najmanje za 5 iz matematike treba u skoli " + minMos.ime
                                   + " iz " + minMos.mesto + " (" + minMos.okrug + " okrug) i to "
                                   + minMat + "%");
        System.out.println("Najvise za 5 iz predmeta na kombinovanom ispitu treba u skoli "
                                   + maxKos.ime + " iz " + maxKos.mesto + " (" + maxKos.okrug + " okrug) i to "
                                   + maxKom + "%");
        System.out.println("Najmanje za 5 iz predmeta na kombinovanom ispitu treba u skoli "
                                   + minKos.ime + " iz " + minKos.mesto + " (" + minKos.okrug + " okrug) i to "
                                   + minKom + "%");
    }

    public static void najgoriVukovac() {
        UceniciGroupBuilder builder = new UceniciGroupBuilder((UcenikWrapper uc) -> uc.bodoviIzSkole == 70);
        UceniciGroup group = builder.getGroup();
        List<UcenikWrapper> sorted = group.sortBy((UcenikWrapper uc1, UcenikWrapper uc2) -> {
            if (uc1.bodoviSaZavrsnog > uc2.bodoviSaZavrsnog)
                return 1;
            else if (uc1.bodoviSaZavrsnog < uc2.bodoviSaZavrsnog)
                return -1;
            return 0;
        });
        int i=0;
        do {
            System.out.println(sorted.get(i) + "\n\n ---NEXT--- \n\n");
            i++;
        } while(sorted.get(i).bodoviSaZavrsnog == sorted.get(i-1).bodoviSaZavrsnog);
    }
}
