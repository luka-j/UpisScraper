package upismpn.exec;

import upismpn.download.Smer2017;
import upismpn.download.Smerovi2017;

public class Cleanup2017 {
    public static void test() {

    }

    public static void reloadSmerovi() {
        Smerovi2017 smerovi = Smerovi2017.getInstance();
        smerovi.loadFromFile();
        smerovi.iterate(0);
        while(smerovi.hasNext()) {
            Smer2017 smer = (Smer2017) smerovi.getNext();
            smer.loadFromJson();
        }
        smerovi.save();
    }
}
