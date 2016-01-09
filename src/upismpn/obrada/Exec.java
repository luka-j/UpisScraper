package upismpn.obrada;

import upismpn.download.UceniciManager;
import upismpn.exec.Ocene;
import upismpn.exec.Teritorijalno;
import upismpn.exec.Zavrsni;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luka
 */
public class Exec {

    public static final Set<Class> executableClasses = new HashSet<>(); //kad je ovde radi, kad je u Exec ne radi

    private static boolean hasMethod(Class cls, String method) {
        Method[] methods = cls.getDeclaredMethods();
        for(Method m : methods) {
            if(m.getName().equals(method) && m.getParameterCount() == 0 && m.getReturnType().equals(void.class))
                return true;
        }
        return false;
    }

    public static void doExec(String... methods) {
        if(executableClasses.isEmpty()) registerExecutables();
        SmeroviBase.load();
        for(String method : methods) {
            try {
                boolean found = false;
                for(Class cls : executableClasses) {
                    if(hasMethod(cls, method)) {
                        System.out.println("Pokrecem " + method);
                        Method m = cls.getDeclaredMethod(method);
                        m.invoke(null, null);
                        found=true;
                        break;
                    }
                }
                if(!found) System.out.println("Nisam nasao " + method);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Exec.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void merge() {
        FileMerger.mergeToOne(UceniciManager.DATA_FOLDER);
    }

    /**
     * Iz dole navedenih klasa se uzimaju metode za izvrsavanje. Ne, ne postoji drugi nacin, bilo kakav pokusaj da
     * klasa sama sebe registruje ce biti ignorisan od strane JVM-a (osim ako klasa nema neko polje koje se ispisuje
     * ili loguje u nekoj metodi pre izvrsavanja doExec, ali uvodjenje takvog ogranicenja nije nista bolje od ovoga)
     */
    private static void registerExecutables() {
        executableClasses.add(Exec.class);
        executableClasses.add(Ocene.class);
        executableClasses.add(Teritorijalno.class);
        executableClasses.add(Zavrsni.class);
        //...
    }
}
