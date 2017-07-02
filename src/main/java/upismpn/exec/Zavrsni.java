package upismpn.exec;

import upismpn.obrada.UceniciGroup;
import upismpn.obrada.UceniciGroupBuilder;
import upismpn.obrada.UcenikWrapper;

import java.util.List;

/**
 * Created by Luka on 1/9/2016.
 */
public class Zavrsni {

    public static void najboljiZavrsni() {
        UceniciGroupBuilder builder = new UceniciGroupBuilder((UcenikWrapper uc) -> uc.bodoviSaZavrsnog == 30);
        UceniciGroup        group   = builder.getGroup();
        List<UcenikWrapper> sorted = group.sortBy((UcenikWrapper uc1, UcenikWrapper uc2) -> {
            if (uc1.bodoviIzSkole > uc2.bodoviIzSkole)
                return 1;
            else if (uc1.bodoviIzSkole < uc2.bodoviIzSkole)
                return -1;
            return 0;
        });
        int i=0;
        do {
            System.out.println(sorted.get(i) + "\n\n ---NEXT--- \n\n");
            i++;
        } while(sorted.get(i).bodoviIzSkole == sorted.get(i-1
        ).bodoviIzSkole);
    }
}
