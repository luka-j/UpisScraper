package upismpn.obrada2017;

import java.util.*;
import java.util.stream.Collectors;

public class Simulation {

    private static class UcenikZelja implements Comparable<UcenikZelja> {
        private final UcenikWrapper ucenik;
        private final int redniBroj;
        private final int krug;
        private final UcenikWrapper.Zelja zelja;
        private UcenikWrapper blizanac;

        public UcenikZelja(UcenikWrapper ucenik, int redniBroj, int krug) {
            this.ucenik = ucenik;
            this.redniBroj = redniBroj;
            if(krug != 1 && krug != 2) throw new IllegalArgumentException("Invalid krug: " + krug);
            this.krug = krug;
            zelja = getZelja();
            blizanac = ucenik.getBlizanac();
            if(blizanac == null) blizanac = ucenik; //this definition comes handy in this particular case
        }

        private UcenikWrapper.Zelja getZelja() {
            if(krug == 1) return ucenik.listaZelja1.get(redniBroj);
            else return ucenik.listaZelja2.get(redniBroj);
        }

        private double getBodovaZaUpisWeighted() {
            double brBodova = zelja.bodovaZaUpis;
            if(ucenik.prioritet && ucenik.smer.equals(zelja.smer)) brBodova += 1000;
            if(ucenik.vukovaDiploma || blizanac.vukovaDiploma) brBodova += 0.001;
            brBodova += Math.max(ucenik.bodovaTakmicenja, blizanac.bodovaTakmicenja) * 0.00001;
            brBodova += Math.max(ucenik.bodovaZavrsni, blizanac.bodovaZavrsni) * 0.0000001;
            return brBodova;
        }

        /**
         * U slucaju da imaju isto bodova, odredjuje koji ucenik ima prioritet. Ponasa se slicno kao compareTo
         * @param o drugi ucenik
         * @return -1 ako ovaj ucenik ima prioritet, 1 ako drugi ucenik ima prioritet, 0 ako su jednaki
         */
        int priority(UcenikZelja o) {
            if((ucenik.vukovaDiploma || blizanac.vukovaDiploma) && !(o.ucenik.vukovaDiploma || o.blizanac.vukovaDiploma)) return -1;
            if(!(ucenik.vukovaDiploma || blizanac.vukovaDiploma) && (o.ucenik.vukovaDiploma || o.blizanac.vukovaDiploma)) return 1;
            int cmpTakmicenja = -Double.compare(Math.max(ucenik.bodovaTakmicenja, blizanac.bodovaTakmicenja),
                    Math.max(o.ucenik.bodovaTakmicenja, o.blizanac.bodovaTakmicenja));
            if(cmpTakmicenja != 0) return cmpTakmicenja;
            int cmpZavrsni = -Double.compare(Math.max(ucenik.bodovaZavrsni, blizanac.bodovaZavrsni),
                    Math.max(o.ucenik.bodovaZavrsni, o.blizanac.bodovaZavrsni));
            return cmpZavrsni;
        }

        @Override
        public int compareTo(UcenikZelja o) {
            int cmp = Double.compare(o.getBodovaZaUpisWeighted(), getBodovaZaUpisWeighted());
            if(cmp != 0) return cmp;

            if(ucenik.equals(o.ucenik)) return Integer.compare(redniBroj, o.redniBroj);
            if(blizanac.equals(o.ucenik)) return 0;
            return priority(o);
        }

        @Override
        public String toString() {
            return "(" + redniBroj + ") " + zelja.smer.toString();
        }
    }

    private static Map<UcenikWrapper, UcenikZelja> upisani = new HashMap<>();
    private static Set<UcenikWrapper> neupisani = new HashSet<>();

    public static void simulate() {
        UceniciBase.load();
        Map<SmerWrapper, TreeSet<UcenikWrapper>> ranking = new HashMap<>();
        List<UcenikWrapper> sviUcenici = UceniciBase.svi().filter(uc -> uc.krug == 1).collect(Collectors.toList());
        List<UcenikZelja> zelje = new ArrayList<>(sviUcenici.size() * 12);
        for(UcenikWrapper ucenik : sviUcenici) {
            for(int i=0; i<ucenik.listaZelja1.size(); i++) {
                UcenikZelja zelja = new UcenikZelja(ucenik, i, 1);
                if(zelja.zelja.uslov) {
                    zelje.add(zelja);
                    ucenik.addProperty(zelja.zelja.smer, zelja);
                }
            }
        }
        zelje.sort(null);

        for(int i=0; i<zelje.size(); i++) {
            UcenikZelja zelja = zelje.get(i);
            SmerWrapper smer = zelja.zelja.smer;

            if(!ranking.containsKey(smer)) ranking.put(smer, new TreeSet<>((o1, o2) -> {
                if(o1.equals(o2)) return 0; //this is the only case we return 0 !! (for TreeSet, compareTo=0 <=> equals=true)
                UcenikZelja uz1 = (UcenikZelja)o1.getProperty(smer), uz2 = (UcenikZelja)o2.getProperty(smer);
                int cmp = uz1.compareTo(uz2);
                if(cmp != 0) return cmp;
                cmp = uz1.priority(uz2);
                if(cmp != 0) return cmp;
                return Integer.compare(o1.sifra, o2.sifra);
            }));

            boolean upisi = false;
            if(ranking.get(smer).size() < smer.kvota) upisi = true;
            else {
                UcenikZelja uz = (UcenikZelja)ranking.get(smer).last().getProperty(smer);
                if(uz.getBodovaZaUpisWeighted() < zelja.getBodovaZaUpisWeighted())
                    upisi = true;
                if(uz.getBodovaZaUpisWeighted() == zelja.getBodovaZaUpisWeighted() && uz.priority(zelja) <= 0)
                    upisi = true;
            }
            if(!upisi) continue;


            //obavljanje upisa:
            if(!upisani.containsKey(zelja.ucenik)) {
                ranking.get(smer).add(zelja.ucenik);
                upisani.put(zelja.ucenik, zelja);
                zelje.remove(i);
                i--;
            } else if(upisani.get(zelja.ucenik).redniBroj > zelja.redniBroj) {
                ranking.get(upisani.get(zelja.ucenik).zelja.smer).remove(zelja.ucenik);
                upisani.put(zelja.ucenik, zelja);
                ranking.get(smer).add(zelja.ucenik);
                //trimSet(ranking.get(smer), smer); //ne bi trebalo da ima efekta, posto je lista sortirana
                zelje.remove(i);
                i=-1;
            }
        }

        /*for(Map.Entry<SmerWrapper, TreeSet<UcenikWrapper>> e : ranking.entrySet()) {
            if(e.getKey().upisano1k != e.getValue().size() &&
                    e.getKey().kvota != e.getValue().size()) {
                System.out.println(e.getKey().skola + ": " + e.getKey().smer);
            }
        }*/

        //Neupisanih: 0
        //Greska: 19
        verifySimulation(sviUcenici);
    }

    //proverava da ne pređe preko kvote, posebno u slučaju da imaju isto poena
    private static void trimSet(TreeSet<UcenikWrapper> rank, SmerWrapper smer) {
        int over = rank.size() - smer.kvota;
        if(over <= 0) return;

        double points;
        Iterator<UcenikWrapper> it = rank.descendingIterator();
        for(int i=0; i<over; i++) it.next();
        points = (double)it.next().getProperty(smer);
        it = rank.descendingIterator();
        while((double)it.next().getProperty(smer) < points) it.remove();
    }

    private static <T> int position(TreeSet<T> set, T element) {
        return set.contains(element)? set.headSet(element).size(): Integer.MAX_VALUE;
    }

    private static void verifySimulation(List<UcenikWrapper> sviUcenici) {
        sviUcenici.forEach(uc -> {
            if(!upisani.containsKey(uc))
                neupisani.add(uc);
        });

        for(UcenikWrapper uc : sviUcenici)
            if(!upisani.containsKey(uc))
                neupisani.add(uc);
        System.out.println("Neupisanih: " + neupisani.size());

        int greska = 0;
        for(Map.Entry<UcenikWrapper, UcenikZelja> entry : upisani.entrySet()) {
            if(!entry.getKey().smer.sifra.equals(entry.getValue().zelja.smer.sifra))
                greska++;
        }
        System.out.println("Greska: " + greska);
    }



    public static void simulateBad() {
        UceniciBase.load();
        Map<SmerWrapper, TreeSet<UcenikZelja>> ranking = new HashMap<>();
        Map<UcenikWrapper, HashSet<UcenikZelja>> copies = new HashMap<>();
        List<UcenikWrapper> sviUcenici = UceniciBase.svi().filter(uc -> uc.krug == 1).collect(Collectors.toList());
        for(UcenikWrapper uc : sviUcenici) {
            copies.put(uc, new HashSet<>());
            for(int i=0; i<uc.listaZelja1.size(); i++) {
                UcenikZelja uz = new UcenikZelja(uc, i, 1)/* { //this, curiously enough, makes things worse
                    @Override
                    public int compareTo(UcenikZelja o) {
                        int comp = super.compareTo(o);
                        if(comp == 0) return Integer.compare(hashCode(), o.hashCode());
                        else return comp;
                    }
                }*/;
                if(!uz.zelja.uslov) continue;
                copies.get(uc).add(uz);

                SmerWrapper smer = uz.zelja.smer;
                if(!ranking.containsKey(smer)) ranking.put(smer, new TreeSet<>());
                ranking.get(smer).add(uz);
            }
        }

        for(int i=0; i<20; i++) {
            boolean change = false;
            for(Map.Entry<SmerWrapper, TreeSet<UcenikZelja>> e : ranking.entrySet()) {
                SmerWrapper smer = e.getKey();
                Iterator<UcenikZelja> it = e.getValue().iterator();
                for(int j=0; j<smer.kvota && it.hasNext(); j++) {
                    UcenikZelja uz = it.next();
                    if(uz.redniBroj == i && (!upisani.containsKey(uz.ucenik) || upisani.get(uz.ucenik).redniBroj > i)) {
                        upisani.put(uz.ucenik, uz);
                        for(UcenikZelja alt : copies.get(uz.ucenik)) {
                            if(alt.redniBroj > i) ranking.get(alt.zelja.smer).remove(uz);
                        }
                        change = true;
                    }
                }
            }
            if(change) i=-1;
            System.out.println(i); //debug purposes (to see if progresses)
        }

        upisani.clear();
        for(Map.Entry<SmerWrapper, TreeSet<UcenikZelja>> e : ranking.entrySet()) {
            Iterator<UcenikZelja> it = e.getValue().iterator();
            for(int j=0; j<e.getKey().kvota && it.hasNext(); j++) {
                UcenikZelja uz = it.next();
                upisani.put(uz.ucenik, uz);
            }
        }

        verifySimulation(sviUcenici);
    }

}
