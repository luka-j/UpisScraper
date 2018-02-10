package rs.lukaj.upisstats.scraper.obrada2017;

import rs.lukaj.upisstats.scraper.download.Smer2017;

//this exists basically only to attempt to correct design fuckups of Smer2017
public class SmerW {

    public final String sifra, smer, skola, podrucje;
    public final String jezik, opstina, okrug;
    public final int kvota, kvotaUmanjenje;
    public final int trajanje; //trajanje smera, u godinama (3 ili 4)
    public final int upisano1k, upisano2k, kvota2k;
    public final double minBodova1k, minBodova2k;

    public SmerW(Smer2017 smer) {
        sifra = smer.getSifra();
        this.smer = smer.getPodrucje(); //it's not a mistake
        skola = smer.getIme();
        podrucje = smer.getPodrucje2017();
        jezik = smer.getJezik();
        opstina = smer.getOpstina();
        okrug = smer.getOkrug();
        kvota = Integer.parseInt(smer.getKvota());
        kvotaUmanjenje = Integer.parseInt(smer.getKvotaUmanjenje());
        upisano1k = Integer.parseInt(smer.getUpisano1K());
        upisano2k = Integer.parseInt(smer.getUpisano2K());
        kvota2k = Integer.parseInt(smer.getKvota2K());
        minBodova1k = Double.parseDouble(smer.getMinBodova1K());
        minBodova2k = Double.parseDouble(smer.getMinBodova2K());

        if(smer.getTrajanje().equals("2")) trajanje=4;
        else trajanje=3;
        //Profiler.addTime("new SmerW", end-start);
    }

    public boolean trebaPrijemni() {
        return minBodova1k > 100;
    }

    @Override
    public boolean equals(Object obj) {
        return sifra.equals(((SmerW)obj).sifra);
    }

    @Override
    public int hashCode() {
        return sifra.hashCode();
    }

    @Override
    public String toString() {
        return skola + ": " + smer + ", " + opstina;
    }
}
