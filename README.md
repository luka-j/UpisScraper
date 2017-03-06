# Upis15Crawler
Crawls upin.mpn.gov.rs for data. 

## Preuzima:
- Rezultate na završnom (broj bodova na svakom testu)
- Rezultate na prijemnom, ako je učenik upisao neku od škola za koje se polaže poseban ispit
- Ocene od šestog razreda
- Uspeh na državnim i međunarodnim takmičenjima (predmet i mesto)
- Podaci o osnovnoj školi: ime, mesto, okrug
- Podaci o upisanoj školi: ime, mesto, okrug, smer, područje rada, kvota
- Listu želja
- Krug u kojem je učenik upisan

## Zašto?

Smatram da se radi o podacima koji bi bilo lepo da su organizovani i dostupni u lepšem obliku od onog na zvaničnom sajtu za upis, kako bi se lakše iz njih izvukle ionako dostupne (samo dosta teže) informacije. Program sam pokretao tek kad se saobraćaj na sajtu "stiša" kako ne bih remetio server, a crawler je generalno nežan po tom pitanju i pravi oko tri zahteva po sekundi. Ovo naravno ne bi bilo neophodno da se ovi podaci nalaze u sklopu OpenData portala, međutim to nije slučaj iz meni nepoznatog razloga, iako mi se čini da imaju svako pravo biti tamo: mogu biti itekako značajni javnosti, a ovaj program ne može obuhvatiti sve učenike (to jest, ne računa one koji nisu upisali srednju školu iz prostog razloga što se ne nalaze na sajtu). 
Za zanimljive stvari koje se mogu uraditi s ovim podacima, videti [UpisStats](https://github.com/luq-0/UpisStats) projekat koji se direktno nadovezuje na ovaj.

## Kako?

Generiše oko 60k fajlova, za obradu je preporučljivo spojiti ih u jedan koji je velik 60-70MB koristeći `exec merge` komandu.

Za obradu podataka, poželjno je koristiti klasu `Exec` koja koristi reflekciju za pozivanje datih metoda i `UcenikWrapper` koji ima mnoge vrednosti prekalkulisane i pretvorene u odgovarajuć format, pa je samim tim pogodan za razna izračunavanja (`Ucenik` se sastoji isključivo od stringova). Klase iz kojih se uzimaju metode se mogu nalaziti bilo gde, jedino ograničenje je da se njihovi Class objekti nalaze u `Exec#executableClasses` (videti `Exec#registerExecutables`).
Izlaz može biti u obliku System.out ili tabele (videti Spreadsheets klasu)

Dependencies:
- Apache POI

Argumenti:
- `dl` za preuzimanje podataka i čuvanje na disk
- `exec` praćen nazivom metode za izvršavanje željenog postupka
