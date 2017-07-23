package upismpn.obrada2017;

import upismpn.download.Osnovna2017;

public class OsnovnaWrapper {
    public final int id;
    public final String naziv;
    public final String opstina;
    public final String okrug;
    public final String sediste;

    public final double bodova6, bodova7, bodova8;
    public final double matematika, srpski, kombinovani;
    public final int brojUcenika, ucenikaZavrsilo, vukovaca, nagradjenih;

    public final double ukupnoBodova, bodovaZavrsni, bodovaOcene, prosecnaOcena;
    public final double prosek6, prosek7, prosek8;
    public final int nijeZavrsilo;

    public OsnovnaWrapper(Osnovna2017 osnovna) {
        id = osnovna.getId();
        naziv = osnovna.getNaziv();
        opstina = osnovna.getOpstina();
        okrug = osnovna.getOkrug();
        sediste = osnovna.getSediste();
        bodova6 = Double.parseDouble(osnovna.getBodova6());
        bodova7 = Double.parseDouble(osnovna.getBodova6());
        bodova8 = Double.parseDouble(osnovna.getBodova6());
        matematika = Double.parseDouble(osnovna.getMatematika());
        srpski = Double.parseDouble(osnovna.getSrpski());
        kombinovani = Double.parseDouble(osnovna.getKombinovani());
        brojUcenika = Integer.parseInt(osnovna.getBrojUcenika());
        ucenikaZavrsilo = Integer.parseInt(osnovna.getUcenikaZavrsilo());
        vukovaca = Integer.parseInt(osnovna.getVukovaca());
        nagradjenih = Integer.parseInt(osnovna.getNagradjenih());

        bodovaOcene = bodova6 + bodova7 + bodova8;
        bodovaZavrsni = matematika + srpski + kombinovani;
        ukupnoBodova = bodovaOcene + bodovaZavrsni;
        prosek6 = bodova6 * (5/20);
        prosek7 = bodova7 * (5/25);
        prosek8 = bodova8 * (5/25);
        prosecnaOcena = (prosek6 + prosek7 + prosek8) / 3;
        nijeZavrsilo = brojUcenika - ucenikaZavrsilo;
    }
}
