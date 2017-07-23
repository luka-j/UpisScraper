package upismpn.obrada2017;

import upismpn.download.Smer2017;

//this exists basically only to attempt to correct design fuckups of Smer2017
public class SmerWrapper {

    public final String sifra, smer, skola, podrucje;
    public final String jezik, opstina, okrug;
    public final int kvota, kvotaUmanjenje;
    public final int trajanje;

    public SmerWrapper(Smer2017 smer) {
        sifra = smer.getSifra();
        this.smer = smer.getPodrucje(); //it's not a mistake
        skola = smer.getIme();
        podrucje = smer.getPodrucje2017();
        jezik = smer.getJezik();
        opstina = smer.getOpstina();
        okrug = smer.getOkrug();
        kvota = Integer.parseInt(smer.getKvota());
        kvotaUmanjenje = Integer.parseInt(smer.getKvotaUmanjenje());
        if(smer.getTrajanje().equals("2")) trajanje=4;
        else trajanje=3;
    }

    @Override
    public boolean equals(Object obj) {
        return sifra.equals(((SmerWrapper)obj).sifra);
    }

    @Override
    public int hashCode() {
        return sifra.hashCode();
    }
}
