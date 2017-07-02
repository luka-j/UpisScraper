package upismpn.exec;

import upismpn.obrada.UceniciGroup;
import upismpn.obrada.UceniciGroupBuilder;

/**
 * Created by luka on 5.5.16..
 */
public class MetaTools {
    public static void doNothing() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void loadAll() {
        UceniciGroup all = new UceniciGroupBuilder(null).getGroup();
        try {
            Thread.sleep((long) all.getProsekNaZavrsnom() * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
