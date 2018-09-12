package rs.lukaj.upisstats.scraper.obrada2017;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Simulator {

    /**
     * Određuje način rangiranja učenika. Definiše kako se računaju bodovi i koji učenik ima prioritet
     * u slučaju da su obojica "na crti". Definiše krugove upisa i služi da se pristupi listama želja
     * za svakog učenika. Svaka od ovih metoda može biti override-ovana zasebno.
     */
    public interface RankingMethod {
        /**
         * Prvi uslov za poredjenje, veci broj bodova se rangira iznad
         * @param zelja broj bodova za datu zelju
         * @return broj bodova koji je ucenik ostvario za datu zelju
         */
        double getBrojBodova(UcenikZelja zelja);

        /**
         * Odredjuje prioritet izmedju zelja dva ucenika, u slucaju da imaju isti broj bodova.
         * Ako su jednaki, oba ucenika ce biti upisana, cak i preko kvote.
         * @param zelja1 zelja prvog ucenika
         * @param zelja2 zelja drugog ucenika
         * @return -1 ako prvi ima prioritet, 1 ako drugi ima prioritet, 0 ako su jednaki
         */
        int getPriority(UcenikZelja zelja1, UcenikZelja zelja2);

        /**
         * Određuje da li učenik ima uslov za upisivanje date želje. Ako ova metoda vrati false,
         * želja se neće razmatrati pri upisu.
         */
        default boolean uslov(UcenikZelja zelja) {
            return zelja.zelja.uslov;
        }

        /**
         * Broj želja koje je učenik izrazio u datom krugu. Po difoltu, dužina liste
         * {@link UcenikW#listaZelja1} ili {@link UcenikW#listaZelja2}.
         */
        default int getBrojZelja(UcenikW ucenik, int krug) {
            if(krug == 1) return ucenik.listaZelja1.size();
            else return ucenik.listaZelja2.size();
        }

        /**
         * Želja na datom mestu u datom krugu upisa. Po difoltu koristi liste želja iz wrappera.
         */
        default UcenikW.Zelja getZelja(UcenikW ucenik, int krug, int redniBroj) {
            if(krug == 1) return ucenik.listaZelja1.get(redniBroj);
            else return ucenik.listaZelja2.get(redniBroj);
        }

        /**
         * Upoređuje dva učenika, i vraća -1 ako prvi treba biti upisan, 1 ako drugi treba biti upisan,
         * 0 ako su jednaki (npr. blizanci).
         */
        default int compare(UcenikZelja zelja1, UcenikZelja zelja2) {
            int cmp = Double.compare(getBrojBodova(zelja2), getBrojBodova(zelja1));
            if(cmp != 0) return cmp;

            if(zelja1.blizanac.equals(zelja2.ucenik)) return 0;
            return getPriority(zelja1, zelja2);
        }
    }

    public class UcenikZelja implements Comparable<UcenikZelja> {
        public final UcenikW ucenik;
        public final int redniBroj;
        public final UcenikW.Zelja zelja;
        /**
         * Blizanac, ako ga ovaj ucenik ima, u suprotnom blizanac = ucenik
         */
        public final UcenikW blizanac;

        public UcenikZelja(UcenikW ucenik, int redniBroj, int krug) {
            this.ucenik = ucenik;
            this.redniBroj = redniBroj;
            zelja = rankingMethod.getZelja(ucenik, krug, redniBroj);
            blizanac = ucenik.getBlizanac() == null ? ucenik : ucenik.getBlizanac(); //this definition comes handy in this particular case
        }
        @Override
        public int compareTo(UcenikZelja o) {
            return rankingMethod.compare(this, o);
        }

        public boolean uslov() {
            return rankingMethod.uslov(this);
        }

        @Override
        public String toString() {
            return "(" + redniBroj + ") " + zelja.smer.toString();
        }
    }

    private RankingMethod rankingMethod;
    private Predicate<UcenikW> filter;

    private Map<UcenikW, UcenikZelja> upisani = new HashMap<>();
    private Set<UcenikW> neupisani = new HashSet<>();
    private Map<SmerW, TreeSet<UcenikW>> ranking = new HashMap<>();

    /**
     *
     * @param rankingMethod opisuje kako se vrsi rangiranje ucenika
     * @param filter odredjuje koji se ucenici rangiraju (koristi se i za verifySimulation)
     */
    public Simulator(RankingMethod rankingMethod, Predicate<UcenikW> filter) {
        this.rankingMethod = rankingMethod;
        this.filter = filter;
    }

    /**
     * Simulira upis koristeći {@link RankingMethod} koji je prosleđen ovom simulatoru.
     *
     * @param krug krug za koji se vrši simulacija. Simulator ne pretpostavlja ništa o krugu niti o broju krugova:
     *             ovaj parametar se prosleđuje metodama {@link RankingMethod#getBrojZelja(UcenikW, int)} i
     *             {@link RankingMethod#getZelja(UcenikW, int, int)} (obe imaju default implementaciju).
     */
    public void simulate(int krug) {
        UceniciBase.load();
        List<UcenikW> sviUcenici = UceniciBase.svi().filter(filter).collect(Collectors.toList());
        List<UcenikZelja> zelje = new ArrayList<>(sviUcenici.size() * 12);
        for(UcenikW ucenik : sviUcenici) {
            for(int i=0; i<rankingMethod.getBrojZelja(ucenik, krug); i++) {
                UcenikZelja zelja = new UcenikZelja(ucenik, i, krug);
                if(zelja.uslov()) {
                    zelje.add(zelja);
                    ucenik.addProperty(zelja.zelja.smer, zelja);
                }
            }
        }
        zelje.sort(null);

        for(int i=0; i<zelje.size(); i++) {
            UcenikZelja zelja = zelje.get(i);
            SmerW smer = zelja.zelja.smer;

            if(!ranking.containsKey(smer)) ranking.put(smer, new TreeSet<>((o1, o2) -> {
                if(o1.equals(o2)) return 0; //this is the only case we return 0 !! (for TreeSet, compareTo=0 <=> equals=true)
                UcenikZelja uz1 = (UcenikZelja)o1.getProperty(smer), uz2 = (UcenikZelja)o2.getProperty(smer);
                int cmp = uz1.compareTo(uz2);
                if(cmp != 0) return cmp;
                return Integer.compare(o1.sifra, o2.sifra);
            }));

            boolean upisi = false;
            if(ranking.get(smer).size() < smer.kvota) upisi = true;
            else {
                UcenikZelja uz = (UcenikZelja) ranking.get(smer).last().getProperty(smer);
                if(zelja.compareTo(uz) == 0)
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


        sviUcenici.forEach(uc -> {
            if(!upisani.containsKey(uc))
                neupisani.add(uc);
        });
    }

    //proverava da ne pređe preko kvote, posebno u slučaju da imaju isto poena
    private static void trimSet(TreeSet<UcenikW> rank, SmerW smer) {
        int over = rank.size() - smer.kvota;
        if(over <= 0) return;

        double points;
        Iterator<UcenikW> it = rank.descendingIterator();
        for(int i=0; i<over; i++) it.next();
        points = (double)it.next().getProperty(smer);
        it = rank.descendingIterator();
        while((double)it.next().getProperty(smer) < points) it.remove();
    }

    private static <T> int position(TreeSet<T> set, T element) {
        return set.contains(element)? set.headSet(element).size(): Integer.MAX_VALUE;
    }

    public void verifySimulation() {
        System.out.println("Neupisanih: " + neupisani.size());

        int greska = 0;
        for(Map.Entry<UcenikW, UcenikZelja> entry : upisani.entrySet()) {
            if(!entry.getKey().smer.sifra.equals(entry.getValue().zelja.smer.sifra)) {
                greska++;
                recurseDebug(entry.getValue().zelja.smer, entry.getKey(), new HashSet<>());
                if(entry.getValue().redniBroj > entry.getKey().upisanaZelja) {
                    System.out.println("Upisan kasnije! " + entry.getKey().sifra);
                }
            }
        }
        System.out.println("Greska: " + greska);
    }

    private void recurseDebug(SmerW faultySmer, UcenikW culprit, Set<SmerW> visited) {
        System.out.println("Recurse debug @ " + culprit + " <- " + faultySmer);
        if(visited.contains(faultySmer)) {
            System.out.println("Visited " + faultySmer + "; end recursion");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~");
            return;
        }
        visited.add(faultySmer);
        Set<UcenikW> ranklist = new HashSet<>(ranking.get(faultySmer));
        Set<UcenikW> reallist = UceniciBase.svi().filter(uc -> uc.smer.equals(faultySmer)).collect(Collectors.toSet());
        List<UcenikW> diff = diffList(ranklist, reallist, true);
        for(Iterator<UcenikW> it = diff.iterator(); it.hasNext();) {
            UcenikW uc = it.next();
            if(uc.krug != 1) {
                System.out.println("Ignoring " + uc.sifra + "; krug: " + uc.krug);
                it.remove();
            } else if(uc.equals(culprit)) {
                it.remove();
            }
        }
        if(diff.isEmpty()) {
            System.out.println("Possible root! Sifra: " + culprit + " -> " + faultySmer);
            System.out.println("---------------");
            if(culprit != null) {
                new ArrayList<>(reallist).sort(Comparator.comparingInt(uc->uc.sifra));
                recurseDebug(culprit.smer, null, visited);
            }
        }
        for(UcenikW uc : diff) {
            recurseDebug(upisani.get(uc).zelja.smer, uc, visited);
        }
    }

    private List<UcenikW> diffList(Set<UcenikW> rank, Set<UcenikW> real, boolean twoway) {
        List<UcenikW> diff = new ArrayList<>(2);
        for(UcenikW uc : real) {
            if(!rank.contains(uc)) {
                diff.add(uc);
            }
        }
        if(twoway) {
            for (UcenikW uc : rank) {
                if (!real.contains(uc))
                    diff.add(uc);
            }
        }
        return diff;
    }

    public Set<UcenikW> getNeupisani() {
        return neupisani;
    }
    public boolean isNeupisan(UcenikW uc) {
        return neupisani.contains(uc);
    }
    public Map<UcenikW, UcenikZelja> getUpisani() {
        return upisani;
    }
    public UcenikZelja getUpisanaZelja(UcenikW uc) {
        return upisani.get(uc);
    }
    public Map<SmerW, TreeSet<UcenikW>> getRanking() {
        return ranking;
    }
    public TreeSet<UcenikW> getRankingFor(SmerW smer) {
        return ranking.get(smer);
    }
}
