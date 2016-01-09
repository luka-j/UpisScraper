# Upis15Crawler
Crawls upin.mpn.gov.rs for data. `Exec.java` could use some work.

Preuzima:
- Rezultate na završnom (broj bodova na svakom testu)
- Rezultate na prijemnom, ako je učenik upisao neku od škola za koje se polaže poseban ispit
- Ocene od šestog razreda
- Uspeh na državnim i međunarodnim takmičenjima (predmet i mesto)
- Podaci o osnovnoj školi: ime, mesto, okrug
- Podaci o upisanoj školi: ime, mesto, okrug, smer, područje rada, kvota
- Lista želja
- Krug u kojem je učenik upisan

Generiše oko 60k fajlova, za obradu je preporučljivo spojiti ih u jedan koji je velik 66,7MB koristeći `exec merge` komandu.

Za obradu podataka, poželjno je koristiti klasu `Exec` koja koristi reflekciju za pozivanje datih metoda i `UcenikWrapper` koji ima mnoge vrednosti prekalkulisane i pretvorene u odgovarajuć format, pa je samim tim pogodan za razna izračunavanja (`Ucenik` se sastoji isključivo od stringova). Klase iz kojih se uzimaju metode se mogu nalaziti bilo gde, jedino ograničenje je da se njihovi Class objekti nalaze u `Exec#executableClasses` (videti `Exec#registerExecutables`).
Izlaz može biti u obliku System.out ili tabele (videti Spreadsheets klasu)

Dependencies:
- Apache POI

Argumenti:
- `dl` za preuzimanje podataka i čuvanje na disk
- `exec` praćen nazivom metode za izvršavanje željenog postupka
