package upismpn.download.misc;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by luka on 2.4.17..
 */
public class Osnovna {
    private final int id;
    private final String ime, mesto, okrug, adresa, telefon;
    private final String mat6, mat7, mat8, srp6, srp7, srp8;
    private final String brojUcenika;

    public Osnovna(int id, Document doc) {
        this.id = id;
        Elements osnovniPodaci = doc.select(".osnovna_podaci");
        ime = osnovniPodaci.get(0).text();
        mesto = osnovniPodaci.get(1).text();
        okrug = osnovniPodaci.get(2).text();
        adresa = osnovniPodaci.get(3).text();
        telefon = osnovniPodaci.get(4).text();
        brojUcenika = doc.select(".stat_br_ucenika_black").text();
        Elements ocene = doc.select(".stat_pocena_skola_naglaseno");
        mat6 = ocene.get(0).text();
        mat7 = ocene.get(1).text();
        mat8 = ocene.get(2).text();
        //retarted inconsistency no1000: prosek iz matematike je klase .stat_pocena_skola_naglaseno2
        srp6 = ocene.get(3).text();
        srp7 = ocene.get(4).text();
        srp8 = ocene.get(5).text();
    }

    public String toCompactString() {
        return id + "\\" +
                ime + "\\" + mesto + "\\" + okrug + "\\" + brojUcenika + "\\" + adresa + "\\" + telefon + "\n" +
                mat6 + "\\" + mat7 + "\\" + mat8 + "\\" + srp6 + "\\" + srp7 + "\\" + srp8;
    }
}
