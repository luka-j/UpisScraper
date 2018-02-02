package rs.lukaj.upisstats.scraper.exec;

import rs.lukaj.upisstats.scraper.obrada2017.Simulator;

import java.util.function.Predicate;

public class DefaultSimulation {
    public static Simulator.RankingMethod defaultRanking = new Simulator.RankingMethod() {
        @Override
        public double getBrojBodova(Simulator.UcenikZelja zelja) {
            double brBodova = zelja.zelja.bodovaZaUpis;
            if(zelja.ucenik.prioritet && zelja.ucenik.smer.equals(zelja.zelja.smer)) brBodova += 1000;
            return brBodova;
        }

        @Override
        public int getPriority(Simulator.UcenikZelja zelja1, Simulator.UcenikZelja zelja2) {
            Predicate<Simulator.UcenikZelja> vukova = uz -> uz.ucenik.vukovaDiploma || uz.blizanac.vukovaDiploma;
            if(vukova.test(zelja1) && !vukova.test(zelja2)) return -1;
            if(!vukova.test(zelja1) && vukova.test(zelja2)) return 1;
            int cmpTakmicenja = -Double.compare(Math.max(zelja1.ucenik.bodovaTakmicenja, zelja1.blizanac.bodovaTakmicenja),
                    Math.max(zelja2.ucenik.bodovaTakmicenja, zelja2.blizanac.bodovaTakmicenja));
            if(cmpTakmicenja != 0) return cmpTakmicenja;
            int cmpZavrsni = -Double.compare(Math.max(zelja1.ucenik.bodovaZavrsni, zelja1.blizanac.bodovaZavrsni),
                    Math.max(zelja2.ucenik.bodovaZavrsni, zelja2.blizanac.bodovaZavrsni));
            return cmpZavrsni;
        }
    };

    public static void defaultSim() {
        Simulator sim = new Simulator(defaultRanking, uc -> uc.krug == 1);
        sim.simulate();
        sim.verifySimulation();
    }
}
