package upismpn.download;

/**
 * Created by luka on 2.7.17..
 */
public abstract class DownloadConfig {
    public void loadSmerovi() {
        getSmerovi().load();
    }
    public void saveSmerovi() {
        getSmerovi().save();
    }

    public abstract StudentDownloader downloadStudents(int startingSmer, long startTime);
    public abstract Smerovi getSmerovi();
    public abstract UceniciManager getUceniciManager();
    public abstract Ucenik generateUcenik(String sifra, String ukBodova, String prop);


    public static class Old extends DownloadConfig {

        @Override
        public StudentDownloader downloadStudents(int startingSmer, long startTime) {
            StudentDownloader instance = StudentDownloader.getInstance(startingSmer, startTime);
            instance.downloadStudentData(this);
            return instance;
        }

        @Override
        public Smerovi getSmerovi() {
            return Smerovi.getInstance();
        }

        @Override
        public UceniciManager getUceniciManager() {
            return UceniciManager.getInstance(this);
        }

        @Override
        public Ucenik generateUcenik(String sifra, String ukBodova, String mestoOs) {
            return new Ucenik(sifra).setDetails(ukBodova, mestoOs);
        }
    }


    public static class New extends DownloadConfig {
        @Override
        public StudentDownloader downloadStudents(int startingSmer, long startTime) {
            StudentDownloader instance = StudentDownloader2017.getInstance(startingSmer, startTime);
            instance.downloadStudentData(this);
            return instance;
        }

        @Override
        public Smerovi getSmerovi() {
            return Smerovi2017.getInstance();
        }

        @Override
        public UceniciManager getUceniciManager() {
            return UceniciManager.getInstance(this);
        }

        @Override
        public Ucenik generateUcenik(String sifra, String ukBodova, String krug) {
            return new Ucenik2017(sifra).setDetails(ukBodova, krug);
        }
    }
}
