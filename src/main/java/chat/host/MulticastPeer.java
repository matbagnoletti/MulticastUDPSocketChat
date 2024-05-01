package chat.host;

import chat.comunicazione.Protocollo;
import chat.eccezioni.ProtocolException;
import chat.gestione.ChatLogger;
import chat.gestione.ChatLoggerType;
import chat.gestione.OutputType;
import chat.gestione.ProjectOutput;
import chat.comunicazione.Cronologia;
import chat.comunicazione.Messaggio;
import chat.eccezioni.CommunicationException;
import chat.eccezioni.MsgException;
import chat.eccezioni.NoSuchUserException;
import chat.utenze.*;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * Generico membro di un {@link GroupChat} multicast
 * @author Matteo Bagnoletti Tini
 * @version 1.0
 * @project MulticastUDPSocketChat
 */
public class MulticastPeer {

    /**
     * L'utente umano che utilizza il programma
     */
    private final Utente utente;

    /**
     * Socket per la comunicazione unicast
     */
    private DatagramSocket unicastSocket;

    /**
     * Variabile per lo status dell'host. <code>true</code> se operativo, <code>false</code> altrimenti.
     */
    private volatile boolean online;

    /**
     * {@link Rubrica} degli utenti conosciuti
     */
    private final Rubrica rubrica;

    /**
     * {@link Cronologia} dei messaggi
     */
    private final Cronologia cronologia;

    /**
     * Il {@link GroupChat} utilizzato
     */
    private final GroupChat gruppoUDP;

    /**
     * Crea un oggetto <code>multicastPeer</code> e configura le strutture di gestione e funzionamento associate
     * @see Utente
     * @see Rubrica
     * @see Cronologia
     * @param username il nome utenze
     * @param abilitaLog indica se abilitare le funzioni di logging
     * @throws IllegalArgumentException nei casi previsti dalla creazione dell'{@link Utente}
     */
    public MulticastPeer(final String username, final boolean abilitaLog, final GroupChat gruppo) throws IllegalArgumentException {
        utente = new Utente(username);
        rubrica = new Rubrica(utente);
        cronologia = new Cronologia(utente);
        gruppoUDP = gruppo;
        setOnline(false);
        ChatLogger.abilita(abilitaLog);
    }

    /**
     * Getter di {@link #online}
     * @return se l'<code>host</code> è online oppure no
     */
    private synchronized boolean isOnline() {
        return online;
    }

    /**
     * Setter di {@link #online}
     * @param online imposta lo status del <code>multicastPeer</code>
     */
    private synchronized void setOnline(boolean online) {
        this.online = online;
    }
    
    /**
     * Configura opportunamente il <code>multicastPeer</code>
     * @throws IOException se si verifica un errore di I/O
     */
    public synchronized void configura() throws IOException {
        try {
            this.unicastSocket = new DatagramSocket();
            ChatLogger.log("Determinati -> IPv4 locale: " + InetAddress.getLocalHost() + " | porta locale : " + unicastSocket.getLocalPort(), ChatLoggerType.OPTIONAL);
            setOnline(true);
            ChatLogger.log("Socket unicast creato con successo", ChatLoggerType.OPTIONAL);
        } catch (IOException e){
            setOnline(false);
            throw new IOException("Errore nella creazione del Socket unicast: " + e.getMessage());
        }
    }

    /**
     * Avvia le funzioni generali del {@link MulticastPeer}
     * @throws CommunicationException
     * @throws MsgException
     * @throws ProtocolException
     * @throws IllegalArgumentException
     */
    public synchronized void avvia() throws CommunicationException, MsgException, ProtocolException, IOException {
        gruppoUDP.avvia();
        inputUtente();
        leggiUnicast();
        leggiGruppo();
        invia("join-group");
    }

    /**
     * Avvia il {@link Thread} di ricezione unicast
     */
    private void leggiUnicast() {
        threadRicezione(unicastSocket);
        ChatLogger.log("Thread ricezione unicast avviato", ChatLoggerType.OPTIONAL);
    }

    /**
     * Avvio il {@link Thread} di ricezione multicast
     */
    private void leggiGruppo() {
        threadRicezione(gruppoUDP.getMulticastSocket());
        ChatLogger.log("Thread ricezione multicast avviato", ChatLoggerType.OPTIONAL);
    }

    /**
     * Crea un generico {@link Thread} di ricezione.
     * <p>
     * Il funzionamento in ricezione di un {@link MulticastPeer} è identico sia per socket di tipo <code>unicast</code>, che di tipo <code>multicast</code>:
     * <ol>
     *     <li>Il {@link Thread} viene creato e configurato con un proprio nome specifico per una più efficace gestione</li>
     *     <li>Procedendo in un loop che termina nel solo momento in cui l'<code>host</code> diventa offline o la <code>socket</code> viene chiusa, viene creato un buffer (array) di byte da utilizzare per il costruttore del {@link DatagramPacket} di ricezione</li>
     *     <li>Ricevuto un {@link DatagramPacket}, viene estratto il messaggio, salvato nella {@link Cronologia} e segnalato l'utente mittente alla {@link Rubrica}</li>
     *     <li>Nel caso in cui il messaggio sia di tipo <code>ACK</code>, viene avviata la procedura per la memorizzazione dell'avvenuta conferma di ricezione</li>
     *     <li>In caso contrario 3 situazioni vengono verificate:
     *       <ul>
     *           <li>Se il messaggio segnala che un utente ha abbandonato il gruppo: <code>left-group</code></li>
     *           <li>Se il messaggio segnala che un utente si è unito al gruppo: <code>join-group</code></li>
     *           <li>Se il messaggio contiene un generico testo, a cui si provvedere ad inviare un messaggio <code>ACK</code> di risposta</li>
     *       </ul>
     *     </li>
     * </ol>
     */
    private synchronized void threadRicezione(final DatagramSocket tipoSocket) {
        new Thread(()->{
            
            if(tipoSocket != null) {
                if (tipoSocket instanceof MulticastSocket) {
                    Thread.currentThread().setName("Thread di ricezione multicast");
                } else {
                    Thread.currentThread().setName("Thread di ricezione unicast");
                }
                
                while(this.isOnline() && !tipoSocket.isClosed()) {
                    try {
                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        tipoSocket.receive(packet);
                        
                        Messaggio msgRicevuto = Messaggio.configMsg(buffer);

                        cronologia.nuovoMessaggio(msgRicevuto);
                        rubrica.aggiungiUtente(msgRicevuto.getUtente(), packet.getAddress(), msgRicevuto.getPortaMittente());

                        if (msgRicevuto.isACK() && !msgRicevuto.getIDutente().equals(this.utente.getIDutente())) {
                            cronologia.confermaDiLettura(msgRicevuto);
                        } else if (!msgRicevuto.getIDutente().equals(this.utente.getIDutente())) {
                            if (msgRicevuto.getMsg().equals("left-group")) {
                                String utenteRimosso = rubrica.rimuoviUtente(msgRicevuto.getIDutente());
                                ProjectOutput.stampa(utenteRimosso + " ha abbandonato la chat del gruppo", OutputType.STDOUT);
                            } else if (msgRicevuto.getMsg().equals("join-group")) {
                                ProjectOutput.stampa(msgRicevuto.getUsername() + " si è unito al chat del gruppo", OutputType.STDOUT);
                                ChatLogger.log("Tentativo di invio del messaggio di saluto in corso...", ChatLoggerType.OPTIONAL);
                                invia("benvenuto/a " + msgRicevuto.getUsername() + "!");
                            } else {
                                ProjectOutput.stampa(msgRicevuto.estrai(rubrica), OutputType.STDOUT);
                                /* invio ACK */
                                preparaACK(String.valueOf(msgRicevuto.getID()), msgRicevuto.getIDutente());
                            }
                        }
                    } catch (SocketException e) {
                        setOnline(false);
                        chiudi();
                        break;
                    } catch (IOException e) {
                        setOnline(false);
                        ProjectOutput.stampa("Errore di I/O: " + e.getMessage(), OutputType.STDERR);
                        break;
                    } catch (MsgException e) {
                        ProjectOutput.stampa("Formato messaggio non valido", OutputType.STDERR);
                    } catch (NoSuchUserException e) {
                        ProjectOutput.stampa("Utente non inizializzato", OutputType.STDERR);
                    } catch (CommunicationException e) {
                        ProjectOutput.stampa(e.getMessage(), OutputType.STDERR);
                    } catch (ProtocolException e){
                        ProjectOutput.stampa(e.getMessage() + ". Il programma verrà terminato", OutputType.STDERR);
                        chiudi();
                    }
                }
            }

        }).start();
    }

    /**
     * Avvia il {@link Thread} per la lettura e interpretazione dell'input dell'utente da tastiera. Specifici comandi preceduti dal carattere <code>$</code> possono essere visualizzati attraverso il comando <code>$help</code>
     */
    private synchronized void inputUtente() {
        new Thread(()->{
            Thread.currentThread().setName("Thread per l'input dell'utente");
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            Scanner inUtente = new Scanner(System.in);
            ChatLogger.log("Terminale pronto all'invio di messaggi", ChatLoggerType.MANDATORY);
            ChatLogger.log("Digita una qualsiasi sequenza di caratteri per comunicare con il GRUPPO", ChatLoggerType.MANDATORY);
            ChatLogger.log("Per poter comunicare privatamente con un utente digitare: 'testo > aliasUtente'", ChatLoggerType.MANDATORY);
            ChatLogger.log("Digita '$exit' per terminare l'esecuzione", ChatLoggerType.MANDATORY);
            ChatLogger.log("Digita '$help' per visualizzare l'elenco dei comandi", ChatLoggerType.MANDATORY);
            
            while(this.isOnline()) {
                try {
                    String input = inUtente.nextLine().trim();
                    
                    String[] arrayInput;
                    
                    if(!input.isBlank()){
                        /* comandi */
                        if(input.contains("$")){
                            arrayInput = input.split(" ");
                            
                            switch (arrayInput[0].trim()){
                                case "$exit" -> {
                                    invia("left-group");
                                    chiudi();
                                }
                                
                                case "$utenti" -> ChatLogger.log(rubrica.getRubrica(), ChatLoggerType.MANDATORY);
                                
                                case "$stat" -> ChatLogger.log(cronologia.getStatistiche(), ChatLoggerType.MANDATORY);
                                
                                case "$rn" -> {
                                    if(arrayInput.length == 3){
                                        rubrica.rinomina(arrayInput[1].trim(), arrayInput[2].trim());
                                    } else {
                                        ProjectOutput.stampa("Parametri <alias> e <nuovoAlias> assenti o non validi. Digita $help per l'elenco dei comandi", OutputType.STDERR);
                                    }
                                }
                                
                                case "$help" -> {
                                    ChatLogger.log("Digita '$exit' per terminare l'esecuzione", ChatLoggerType.MANDATORY);
                                    ChatLogger.log("Digita '$help' per visualizzare l'elenco dei comandi", ChatLoggerType.MANDATORY);
                                    ChatLogger.log("Digita '$utenti' per visualizzare la rubrica memorizzata", ChatLoggerType.MANDATORY);
                                    ChatLogger.log("Digita '$stat' per visualizzare le statistiche di output", ChatLoggerType.MANDATORY);
                                    ChatLogger.log("Digita '$rn <alias> <nuovoAlias>' per rinominare l'alias di un utente in rubrica", ChatLoggerType.MANDATORY);
                                    ChatLogger.log("Digita '$log' per attivare/disattivare la modalità di logging", ChatLoggerType.MANDATORY);
                                }
                                
                                case "$log" -> ChatLogger.abilita(!ChatLogger.isAbilitato());
                                
                                default -> ProjectOutput.stampa("Comando " + arrayInput[0].trim() + " non riconosciuto. Digita $help per l'elenco dei comandi", OutputType.STDERR);
                            }
                            
                        } else if (input.contains(">")){
                            arrayInput = input.split(">");

                            if(arrayInput.length == 2) {
                                /* scrittura unicast */
                                try {
                                    preparaInvio(arrayInput[0].trim(), arrayInput[1].trim());
                                } catch (MsgException | IllegalArgumentException e) {
                                    ProjectOutput.stampa(e.getMessage(), OutputType.STDERR);
                                }

                            } else {
                                ProjectOutput.stampa("Formato per invio messaggio non valido", OutputType.STDERR);
                            }
                        } else {
                            /* scrittura multicast */
                            try {
                                invia(input);
                            } catch (MsgException | IllegalArgumentException | CommunicationException e) {
                                ProjectOutput.stampa(e.getMessage(), OutputType.STDERR);
                            }
                        }
                    } else {
                        ProjectOutput.stampa("Formato input non valido: null", OutputType.STDERR);
                    }
                } catch (Exception e){
                    setOnline(false);
                    ProjectOutput.stampa("Errore: " + e.getMessage(), OutputType.STDERR);
                    break;
                }
            }
            chiudi();
        }).start();
    }

    /**
     * Prepara l'output di messaggi unicast
     * @param messaggioUnicast il contenuto del messaggio
     * @param destinatario il destinatario (UUID o username)
     * @throws NoSuchUserException se si verifica un errore legato agli utenti e la {@link #rubrica}
     * @throws MsgException se si verifica un errore nella creazione del {@link Messaggio}
     * @throws IOException se si verifica un errore nell'invio del datagramma
     * @throws ProtocolException se il parametro <code>protocollo</code> non è un valido {@link Protocollo}
     */
    private synchronized void preparaInvio(String messaggioUnicast, String destinatario) throws MsgException, NoSuchUserException, ProtocolException, IOException {
        IndiceRubrica infoDestinatario;
        
        if(destinatario.contains("-")) {
            String aliasDaUUID = rubrica.ottieniAliasDaUUID(destinatario);
            infoDestinatario = rubrica.ottieniInfoUtente(aliasDaUUID);
        } else {
            infoDestinatario = rubrica.ottieniInfoUtente(destinatario);
        }
        
        Messaggio messaggio = new Messaggio(cronologia.getNewID(), utente, unicastSocket.getLocalPort(), 1, messaggioUnicast, false, Protocollo.UDP.unicast);
        ChatLogger.log("Invio messaggio unicast per " + utente.getIDutente() + " con msgID " + messaggioUnicast + " in corso...", ChatLoggerType.OPTIONAL);
        cronologia.storicizzaMessaggio(messaggio);
        
        byte[] out = Messaggio.configMsg(messaggio);
        DatagramPacket packet = new DatagramPacket(out, out.length, infoDestinatario.inetAddress(), infoDestinatario.porta());
        invia(packet);
    }

    /**
     * Prepara l'output di messaggi ACK (unicast)
     * @param msgIDxACK il contenuto del messaggio
     * @param UUID l'identificativo univoco del destinatario
     * @throws MsgException se si verifica un errore nella creazione del {@link Messaggio}
     * @throws IOException se si verifica un errore nell'invio del datagramma
     * @throws ProtocolException se il parametro <code>protocollo</code> non è un valido {@link Protocollo}
     */
    private synchronized void preparaACK(String msgIDxACK, String UUID) throws MsgException, IOException, ProtocolException, NoSuchUserException {
        String aliasDaUUID = rubrica.ottieniAliasDaUUID(UUID);
        IndiceRubrica infoDestinatario = rubrica.ottieniInfoUtente(aliasDaUUID);
        
        Messaggio messaggio = new Messaggio(cronologia.getNewID(), utente, unicastSocket.getLocalPort(), true, msgIDxACK, Protocollo.UDP.unicast);
        ChatLogger.log("Invio messaggio ACK per " + utente.getIDutente() + " con msgID " + msgIDxACK + " in corso...", ChatLoggerType.OPTIONAL);
        cronologia.storicizzaMessaggio(messaggio);
        
        byte[] out = Messaggio.configMsg(messaggio);
        DatagramPacket packet = new DatagramPacket(out, out.length, infoDestinatario.inetAddress(), infoDestinatario.porta());
        invia(packet);
    }

    /**
     * Invia i datagrammi unicast al destinatario
     * @param datagramPacket il datagramma da inviare
     * @throws IOException se si verifica un errore nell'invio del datagramma
     */
    private synchronized void invia(DatagramPacket datagramPacket) throws IOException {
        try {
            unicastSocket.send(datagramPacket);
        } catch (IOException e) {
            throw new IOException("Impossibile inviare ACK: " + e.getMessage(), e.getCause());
        }
    }

    /**
     * Metodo di scrittura di messaggi a un <code>gruppo</code> multicast
     * @param messaggioMulticast il contenuto del messaggio
     * @throws MsgException se si verifica un errore nella creazione del {@link Messaggio}
     * @throws CommunicationException se si verifica un errore legato alla chat multicast
     * @throws ProtocolException se il parametro <code>protocollo</code> non è un valido {@link Protocollo}
     */
    private synchronized void invia(String messaggioMulticast) throws MsgException, CommunicationException, ProtocolException {
        ChatLogger.log("Invio messaggio multicast in corso...", ChatLoggerType.OPTIONAL);
        Messaggio messaggio = new Messaggio(cronologia.getNewID(), utente, unicastSocket.getLocalPort(), rubrica.partecipantiGruppo(), messaggioMulticast, true, Protocollo.UDP.multicast);
        cronologia.storicizzaMessaggio(messaggio);
        byte[] out = Messaggio.configMsg(messaggio);
        gruppoUDP.multicast(out);
    }

    /**
     * Chiude la {@link #unicastSocket}, rilasciando le risorse
     */
    public synchronized void chiudi() {
        if(isOnline()){
            setOnline(false);
            ChatLogger.log("Terminazione in corso...", ChatLoggerType.MANDATORY);

            try {
                gruppoUDP.chiudi();
            } catch (IOException e) {
                ChatLogger.log("Impossibile abbandonare correttamente il gruppo", ChatLoggerType.OPTIONAL);
            }
            if(unicastSocket != null && !unicastSocket.isClosed()) unicastSocket.close();
        }
    }
}
