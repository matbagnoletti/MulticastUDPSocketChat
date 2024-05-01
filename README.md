<h1 align="center">MulticastUDPSocketChat</h1>

<p align="center" style="font-family: monospace">Made by <a href="https://github.com/matbagnoletti">@matbagnoletti</a></p>
<p align="center" style="font-family: monospace">Docenti: prof.ssa <a href="https://github.com/mciuchetti">@mciuchetti</a> e prof.ssa Fioroni</p>
<p align="center" style="font-family: monospace">Corso TPSIT a.s. 2023/2024, <a href="https://www.avoltapg.edu.it/">ITTS A. Volta (PG)</a></p>
<p align="center">
    <img src="https://img.shields.io/github/last-commit/matbagnoletti/MulticastUDPSocketChat?style=for-the-badge" alt="Ultimo commit">
    <img src="https://img.shields.io/github/languages/top/matbagnoletti/MulticastUDPSocketChat?style=for-the-badge" alt="Linguaggio">
</p>

## Descrizione
Applicazione Java che utilizza i DatagramSocket per implementare una comunicazione UDP unicast e multicast tra più MulticastPeer.

## Requisiti
- [JDK](https://www.oracle.com/it/java/technologies/downloads/) (v21.0.2)
- [Maven](https://maven.apache.org/download.cgi) (v3.9.6)

È possibile visualizzare le versioni già presenti sul proprio dispositivo mediante i seguenti comandi:
```
java -version
mvn -v
```

## Installazione e utilizzo
1. Scaricare il file compresso del progetto
2. Estrarre il progetto
3. Eseguire la [classe di avvio](src/main/java/chat/RunMe.java):
    - Tramite IDE
    - Tramite terminale:
        1. Naviga nella root del progetto
        2. Esegui la build del progetto: `mvn clean install`
        3. Identifica il file `jar` nella directory `/targer/`
        4. Esegui il programma: `java -cp target/<nome-del-file-jar>.jar chat.RunMe`

## Struttura e funzionamento
Il progetto si compone da 5 packages:

- Package [comunicazione](src/main/java/chat/comunicazione): Fornisce le classi-entità per la gestione della comunicazione tra [`MulticastPeer`](src/main/java/chat/host/MulticastPeer.java).
- Package [eccezioni](src/main/java/chat/eccezioni): Fornisce una collezione di eccezioni specifiche.
- Package [gestione](src/main/java/chat/gestione): Fornisce le classi-entità per specifiche operazioni di gestione del programma.
- Package [host](src/main/java/chat/host): Fornisce le classi-entità necessarie alla gestione dei [`MulticastPeer`](src/main/java/chat/host/MulticastPeer.java).
- Package [utenze](src/main/java/chat/utenze): Fornisce le classi-entità necessarie alla gestione degli oggetti [`Utente`](src/main/java/chat/utenze/Utente.java).

Per poter utilizzare il programma è necessario:
1. Creare un oggetto [`GroupChat`](src/main/java/chat/host/GroupChat.java) e configurarlo opportunamente;
2. Creare un oggetto [`multicastPeer`](src/main/java/chat/host/MulticastPeer.java) e configurarlo opportunamente con il `GroupChat` precedentemente creato;
3. Invocare il metodo `MulticastPeer.configura()` e successivamente `MulticastPeer.avvia()`.

Ad esempio:
```java
   try {
        GroupChat groupChat = new GroupChat("230.19.6.5", 19065);
        MulticastPeer multicastPeer = new MulticastPeer("Matteo", false, groupChat);
        multicastPeer.configura();
        multicastPeer.avvia();
   } catch (Exception e) {
        ProjectOutput.stampa(e.getMessage(), OutputType.STDERR);
   }
```

Un oggetto `GroupChat` necessita di un indirizzo IPv4 di classe D e di un numero di porta valido (1-65535) su cui avviare il `MulticastSocket`.

Un oggetto `MulticastPeer` necessita, invece, di uno <code>username</code>, la modalità di <code>log</code> scelta (<code>true</code> per abilitare la modalità avanzata, <code>false</code> altrimenti) e del `GroupChat` a cui deve connettersi.

Si consiglia di gestire le possibili eccezioni lanciate dal programma tramite la classe `ProjectOutput`. Il metodo `stampa()` richiede due parametri: il messaggio di errore, e la tipologia di messaggio (in questo caso `OutputType.STDERR`). La gestione dei <code>log</code> è, invece, affidata alla classe `ChatLogger`. Per un più consapevole utilizzo si consiglia la visualizzazione del package [`gestione`](src/main/java/chat/gestione).

## Documentazione
L'intero progetto è stato opportunamente documentato secondo lo standard [JavaDoc](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html) disponibile nella directory [docs](/docs). Si consiglia di visualizzare il file [docs/index.html](/docs/index.html) attraverso il proprio browser.

## Licenza d'uso
Questo progetto (e tutte le sue versioni) sono rilasciate sotto la [MB General Copyleft License](LICENSE).