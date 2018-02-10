package rs.lukaj.upisstats.scraper.download.misc;

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

    //this is actually adapted from server, not the other way around
    public Osnovna(String compactString) {
        String[] parts = compactString.split("\\n");
        String[] info = parts[0].split("\\\\", -1);
        String[] ocene = parts[1].split("\\\\");
        id = Integer.parseInt(info[0]);
        ime = info[1];
        mesto = info[2];
        okrug = info[3];
        brojUcenika = info[4].isEmpty() ? "0" : info[4];
        adresa = info[5];
        telefon = info[6];
        mat6 = (ocene[0]);
        mat7 = ocene[1];
        mat8 = (ocene[2]);
        srp6 = (ocene[3]);
        srp7 = (ocene[4]);
        srp8 = (ocene[5]);
    }

    public String toCompactString() {
        return id + "\\" +
                ime + "\\" + mesto + "\\" + okrug + "\\" + brojUcenika + "\\" + adresa + "\\" + telefon + "\n" +
                mat6 + "\\" + mat7 + "\\" + mat8 + "\\" + srp6 + "\\" + srp7 + "\\" + srp8;
    }

    public String getIme() {
        return ime;
    }

    public String getOkrug() {
        return okrug;
    }

    public int getId() {
        return id;
    }

    public String getMesto() {
        return mesto;
    }

    public String getAdresa() {
        return adresa;
    }

    public String getTelefon() {
        return telefon;
    }

    public String getMat6() {
        return mat6;
    }

    public String getMat7() {
        return mat7;
    }

    public String getMat8() {
        return mat8;
    }

    public String getSrp6() {
        return srp6;
    }

    public String getSrp7() {
        return srp7;
    }

    public String getSrp8() {
        return srp8;
    }

    public String getBrojUcenika() {
        return brojUcenika;
    }
}
