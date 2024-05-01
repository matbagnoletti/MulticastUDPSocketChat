<h1 align="center">MulticastUDPSocketChat</h1>

<p align="center" style="font-family: monospace">Made by <a href="https://github.com/matbagnoletti">@matbagnoletti</a></p>
<p align="center" style="font-family: monospace">Docenti: prof.ssa <a href="https://github.com/mciuchetti">@mciuchetti</a> e prof.ssa Fioroni</p>
<p align="center" style="font-family: monospace">Corso TPSIT a.s. 2023/2024, <a href="https://www.avoltapg.edu.it/">ITTS A. Volta (PG)</a></p>
<p align="center">
    <img src="https://img.shields.io/github/last-commit/matbagnoletti/MulticastUDPSocketChat?style=for-the-badge" alt="Ultimo commit">
    <img src="https://img.shields.io/github/languages/top/matbagnoletti/MulticastUDPSocketChat?style=for-the-badge" alt="Linguaggio">
</p>

## Descrizione
Applicazione Java che utilizza i DatagramSocket per implementare una comunicazione UDP unicast e multicast tra più host MulticastPeer.

## Requisiti
- [JDK](https://www.oracle.com/it/java/technologies/downloads/) (v21.0.2)
- [Maven](https://maven.apache.org/download.cgi) (v3.9.6)

È possibile visualizzare le versioni già presenti sul proprio dispositivo mediante i seguenti comandi:
```bash
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
        3. Identifica il file `jar` nella directory `/target/`
        4. Esegui il programma: `java -cp target/<nome-del-file-jar>.jar chat.RunMe`

## Struttura e funzionamento
Il progetto si compone da 5 packages:

- Package [comunicazione](src/main/java/chat/comunicazione): Fornisce le classi-entità per la gestione della comunicazione tra [`MulticastPeer`](src/main/java/chat/host/MulticastPeer.java).
- Package [eccezioni](src/main/java/chat/eccezioni): Fornisce una collezione di eccezioni specifiche.
- Package [gestione](src/main/java/chat/gestione): Fornisce le classi-entità per specifiche operazioni di gestione del programma.
- Package [host](src/main/java/chat/host): Fornisce le classi-entità necessarie alla gestione dei [`MulticastPeer`](src/main/java/chat/host/MulticastPeer.java).
- Package [utenze](src/main/java/chat/utenze): Fornisce le classi-entità necessarie alla gestione degli oggetti [`Utente`](src/main/java/chat/utenze/Utente.java).

### Utilizzo

Per poter utilizzare il programma è necessario:
1. Creare un oggetto [`GroupChat`](src/main/java/chat/host/GroupChat.java) e configurarlo opportunamente;
2. Creare un oggetto [`MulticastPeer`](src/main/java/chat/host/MulticastPeer.java) e configurarlo opportunamente con il `GroupChat` precedentemente creato;
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

Un oggetto `GroupChat` necessita di un indirizzo IPv4 di classe D e di un numero di porta valido (0-65535) su cui avviare il `MulticastSocket`.

Un oggetto `MulticastPeer` necessita, invece, di uno <code>username</code>, la modalità di <code>log</code> scelta (<code>true</code> per abilitare la modalità avanzata, <code>false</code> altrimenti) e del `GroupChat` a cui deve unirsi.

### Interazione con l'utente

Il programma, tramite un Thread dedicato, sarà in continua attesa di ricevere comandi dall'utente tramite tastiera. L'avviso <code>"# Terminale pronto all'invio di messaggi"</code> segnalerà la disponibilità del programma a ricevere input dall'utente.
I comandi vengono distinti dai normali messaggi attraverso il carattere <code>$</code>.

Elenco dei comandi:
<ul>
   <li><code>$exit</code>: termina l'esecuzione del <code>MulticastPeer</code> e del programma.</li>
   <li><code>$help</code>: stampa a video l'elenco dei comandi.</li>
   <li><code>$utenti</code>: stampa a video l'elenco degli utenti memorizzati in rubrica (presenti nel gruppo).</li>
   <li><code>$stat</code>: stampa a video le statistiche riguardo ai <code>messaggi</code> inviati in output. Tale funzionamento si basa sullo scambio di <code>messaggi ACK</code>.</li>
   <li><code>$rn</code>: permette di rinominare un utente memorizzato in rubrica.</li>
   <li><code>$log</code>: abilita e disabilita la modalità di <code>log</code> avanzata.</li>
</ul>

Digitando una generica sequenza di caratteri, invece, il programma interpreterà il testo come un messaggio da comunicare al <code>GroupChat</code> in modalità multicast.

Dopo che il programma ha opportunamente memorizzato in rubrica un generico utente del <code>GroupChat</code> dal quale si è ricevuto un generico messaggio, è possibile comunicare in modalità privata (unicast) con esso specificando, dopo il testo del messaggio, il suo nome utente.

Ad esempio:
```bash
   ciao > usernameDestinatario
```

> [!CAUTION]
>
> La ricerca degli utenti in rubrica è di tipo _case-sensitive_: l'utente _"matteo"_ è diverso dall'utente _"Matteo"_.

### Gestione dei MulticastPeer nel gruppo

A ogni <code>MulticastPeer</code> è associato un oggetto [Utente](src/main/java/chat/utenze/Utente.java). Per evitare conflitti di username, ogni utente è fornito di un proprio codice univoco <code>UUID</code>, che viene trasmesso insieme allo username. Attraverso metodi per la verifica dei duplicati, il programma rinominerà automaticamente utenti il cui username non risulta univoco.

Si consiglia di gestire le possibili eccezioni lanciate dal programma tramite il costrutto <code>try-catch</code>, affidando l'output dell'evento alla classe [`ProjectOutput`](src/main/java/chat/gestione/ProjectOutput.java). Il metodo `ProjectOutput.stampa()` richiede due parametri: il messaggio di errore e la tipologia di messaggio (in questo caso `OutputType.STDERR`). La gestione dei <code>log</code> è, invece, affidata alla classe [`ChatLogger`](src/main/java/chat/gestione/ChatLogger.java). Per un più consapevole utilizzo di tali classi si consiglia la visualizzazione del package [`gestione`](src/main/java/chat/gestione).

## Documentazione
L'intero progetto è stato opportunamente documentato secondo lo standard [JavaDoc](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html) disponibile nella directory [docs](/docs). Si consiglia di visualizzare il file [index.html](/docs/index.html) attraverso il proprio browser.

## Licenza d'uso
Questo progetto (e tutte le sue versioni) sono rilasciate sotto la [MB General Copyleft License](LICENSE).
