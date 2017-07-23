# UpisScrapper
Scraps data from upis.mpn.gov.rs.

## Preuzima:
- Rezultate na završnom (broj bodova na svakom testu)
- Rezultate na prijemnom, ako je učenik upisao neku od škola za koje se polaže poseban ispit
- Ocene od šestog razreda
- Uspeh na državnim i međunarodnim takmičenjima (predmet i mesto)
- Podaci o osnovnoj školi: ime, mesto, okrug
- Podaci o upisanoj školi: ime, mesto, okrug, smer, područje rada, kvota
- Listu želja
- Krug u kojem je učenik upisan
- 2017: ... i ostale stvari koje su dostupne na sajtu

## Zašto?

Zašto da ne? Podaci su tu, mogu reći svašta uz dovoljno (dobrih) ideja, a možda još nekom sem meni ovo bude korisno. Godišnji izveštaji od završnom ispitu Zavoda za vrednovanje kvaliteta obrazovanja su fin početak.

Za zanimljive stvari koje se mogu uraditi s ovim podacima, videti [UpisStats](https://github.com/luq-0/UpisStats) projekat koji se direktno nadovezuje na ovaj.

## Kako?

Generiše oko 60k fajlova, za obradu je preporučljivo spojiti ih u jedan koji je velik 60-70MB koristeći `exec merge` komandu. (2017 update: generiše duplo više fajlova jer čuva i raw json za sve, ali je spojeni fajl duplo manji).

Za obradu podataka, poželjno je koristiti klasu `Exec` koja koristi reflekciju za pozivanje datih metoda i `UcenikWrapper` koji ima mnoge vrednosti prekalkulisane i pretvorene u odgovarajuć format, pa je samim tim pogodan za razna izračunavanja (`Ucenik` se sastoji isključivo od stringova). Klase iz kojih se uzimaju metode se mogu nalaziti bilo gde, jedino ograničenje je da se njihovi Class objekti nalaze u `Exec#executableClasses` (videti `Exec#registerExecutables`).
Izlaz može biti u obliku System.out ili tabele (videti Spreadsheets klasu)

Dependencies:
- Apache POI for Spreadsheets

Argumenti:
- `dl` za preuzimanje podataka i čuvanje na disk
- `exec` praćen nazivom metode za izvršavanje željenog postupka
