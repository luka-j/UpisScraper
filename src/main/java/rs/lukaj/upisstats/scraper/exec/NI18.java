package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.obrada2017.SmeroviBase;
import rs.lukaj.upisstats.scraper.obrada2017.UceniciBase;
import rs.lukaj.upisstats.scraper.obrada2017.UcenikW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NI18 {
    private static double maxDif = -1;

    private static List<Prijemni> prijemni = new ArrayList<>();
    private static class Prijemni {
        private int brZelje;
        private double bodovi;

        private Prijemni(int brZelje, double bodovi) {
            this.brZelje = brZelje;
            this.bodovi = bodovi;
        }
    }
    public static void testNI() {
        UceniciBase.svi().filter(uc -> uc.krug == 1).forEach(uc -> {
            int sum=uc.sestiRaz.ocene.values().stream().reduce(0, (a,b)->a+b);
            double avg6 = (double)sum/uc.sestiRaz.brojOcena;
            sum=uc.sedmiRaz.ocene.values().stream().reduce(0, (a,b)->a+b);
            double avg7 = (double)sum/uc.sedmiRaz.brojOcena;
            sum=uc.osmiRaz.ocene.values().stream().reduce(0, (a,b)->a+b);
            double avg8 = (double)sum/uc.osmiRaz.brojOcena;
            double avgTotal = (avg6+avg7+avg8)*4;
            double roundDiff = avgTotal - uc.bodoviOcene;
            if(roundDiff > maxDif) maxDif = roundDiff;

            double zavrsni = uc.srpski + uc.matematika + uc.kombinovani;
            double am = uc.bodovaAM;
            double takm = uc.bodovaTakmicenja;
            double total = avgTotal + zavrsni + am + takm;

            if(!uc.prijemni.isEmpty()) {
                for (int i = 0; i < uc.listaZelja1.size(); i++) {
                    if(!uc.listaZelja1.get(i).uslov) continue;
                    double prDif = uc.listaZelja1.get(i).bodovaZaUpis - uc.ukupnoBodova;
                    if (Math.abs(prDif) > 0.1) {
                        boolean ok = false;
                        for (double pb : uc.prijemni.values())
                            if (Math.abs(pb - prDif) < 0.001) {
                                ok = true;
                            }
                        if (!ok) {
                            System.out.println("Oops!");
                        } else {
                            prijemni.add(new Prijemni(i + 1, prDif));
                        }
                    }
                }
            }
            double diff = total - uc.ukupnoBodova;
            if(Math.abs(diff) > 0.06 && uc.prijemni.isEmpty() && uc.getBlizanac() == null) {
                System.out.println("Large diff! Uc " + uc.sifra + ", real: " + uc.ukupnoBodova + ", calc: " + total);
            }
        });
        System.out.println("Max: " + maxDif);
        System.out.println("Prijemnih: " + prijemni.size());
    }

    public static void makeDatafile() {
        File datafile = new File("/media/luka/Data/Shared/Projects/NI18test/ucenici.txt");
        File kvote = new File("/media/luka/Data/Shared/Projects/NI18test/kvote.txt");
        try {
            datafile.createNewFile();
            kvote.createNewFile();
            BufferedWriter ucw = new BufferedWriter(new FileWriter(datafile, false));
            BufferedWriter kvw = new BufferedWriter(new FileWriter(kvote, false));
            UceniciBase.svi().filter(uc -> uc.krug == 1).forEach(uc -> {
                StringBuilder s = new StringBuilder(500);
                s.append(uc.sifra).append(",").append(uc.getBlizanac() == null ? "0" : uc.getBlizanac().sifra).append("/");
                s.append(uc.smer.sifra).append("/");
                s.append(serializeOcene(uc.sestiRaz)).append("/");
                s.append(serializeOcene(uc.sedmiRaz)).append("/");
                s.append(serializeOcene(uc.osmiRaz)).append("/");
                s.append(uc.vukovaDiploma?"v/":"/");
                s.append(uc.srpski).append(",").append(uc.matematika).append(",").append(uc.kombinovani).append(",")
                        .append(uc.bodovaTakmicenja).append(",").append(uc.bodovaAM).append("/");
                s.append(serializeZelje(uc)).append("\n");
                try {
                    ucw.append(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ucw.close();

            SmeroviBase.getAll().forEach(s -> {
                try {
                    kvw.append(s.sifra).append(",").append(String.valueOf(s.kvota)).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            kvw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String serializeOcene(UcenikW.Ocene ocene) {
        return ocene.ocene.values().stream()
                .filter(oc -> oc > 0)
                .map(oc -> oc+"")
                .reduce("", (a,b)->a+","+b)
                .substring(1);
    }
    private static String serializeZelje(UcenikW ucenik) {
        DecimalFormat df = new DecimalFormat("#.##");
        return ucenik.listaZelja1.stream()
                .filter(zelja -> zelja.uslov)
                .map(zelja -> {
                    if(ucenik.prioritet && ucenik.smer.equals(zelja.smer))
                        return zelja.smer.sifra + "," + df.format(zelja.bodovaZaUpis - ucenik.ukupnoBodova + 1000);
                    else return zelja.smer.sifra + "," + df.format(zelja.bodovaZaUpis- ucenik.ukupnoBodova);
                })
                .reduce("", (a,b)->a+b+"/");
    }
}
