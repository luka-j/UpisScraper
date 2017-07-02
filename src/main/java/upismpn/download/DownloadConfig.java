package upismpn.download;

/**
 * Created by luka on 2.7.17..
 */
public interface DownloadConfig {
    void loadSmerovi();
    void saveSmerovi();
    StudentDownloader downloadStudents(int startingSmer, long startTime);
    Ucenik createUcenik(String sifra);
    Smerovi getSmerovi();

    class Old implements DownloadConfig {

        @Override
        public void loadSmerovi() {
            Smerovi.getInstance().load();
        }

        @Override
        public void saveSmerovi() {
            Smerovi.getInstance().save();
        }

        @Override
        public StudentDownloader downloadStudents(int startingSmer, long startTime) {
            StudentDownloader instance = StudentDownloader.getInstance(startingSmer, startTime);
            instance.downloadStudentData(this);
            return instance;
        }

        @Override
        public Ucenik createUcenik(String sifra) {
            return new Ucenik(sifra);
        }

        @Override
        public Smerovi getSmerovi() {
            return Smerovi.getInstance();
        }
    }
}
