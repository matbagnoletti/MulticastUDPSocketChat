package chat.comunicazione;

import chat.gestione.ChatLogger;
import chat.gestione.ChatLoggerType;
import chat.eccezioni.NoSuchUserException;
import chat.utenze.Utente;

import java.util.ArrayList;

/**
 * Cronologia dei messaggi inviati e ricevuti da un dato {@link chat.host.MulticastPeer}>
 * Fornisce una collezione di metodi per il salvataggio e l'analisi dei dati di comunicazione scambiati.
 * <p>
 * {@link #messaggiInviati} e {@link #messaggiRicevuti} rappresentano le due collezioni principali su cui si basa una <code>cronologia</code> e vengono gestiti rispettivamente dai metodi {@link #storicizzaMessaggio(Messaggio)} e {@link #nuovoMessaggio(Messaggio)}.
 * <p>
 * È possibile ottenere una stima approssimativa e statistica dei soli dati trasmessi attraverso il metodo {@link #getStatistiche()}, sfruttando il meccanismo di <code>ACK</code> implementato dal programma.
 * Ciascun {@link Messaggio}, se configurato come messaggio di <code>acknowledge</code>, restituirà <code>true</code> invocando il metodo {@link Messaggio#isACK()}.
 * 
 * @author Matteo Bagnoletti Tini
 * @version 1.0
 * @see Messaggio
 * @project MulticastUDPSocketChat
 */
public class Cronologia {
    
    /**
     * L'insieme dei messaggi ricevuti memorizzati nella {@link Cronologia}
     */
    private ArrayList<Messaggio> messaggiRicevuti;

    /**
     * L'insieme dei messaggi inviati memorizzati nella {@link Cronologia}
     */
    private ArrayList<Messaggio> messaggiInviati;

    /**
     * L'elenco di ID utilizzati dall'{@link chat.host.MulticastPeer} che fa uso di uno specifico oggetto <code>cronologia</code> 
     */
    private ArrayList<Integer> IDs;

    /**
     * L'utente inizializzato di un {@link chat.host.MulticastPeer} in cui è utilizzata la {@link Cronologia}
     */
    private final Utente utente;

    /**
     * Crea un'istanza di {@link Cronologia}
     * 
     * @param utente l'utente di un dato {@link chat.host.MulticastPeer} che fa uso della cronologia 
     */
    public Cronologia(Utente utente) {
        messaggiRicevuti = new ArrayList<>();
        messaggiInviati = new ArrayList<>();
        IDs = new ArrayList<>();
        this.utente = utente;
    }

    /**
     * Memorizza un nuovo {@link Messaggio} ricevuto in input
     * @param messaggio il messaggio ricevuto
     */
    public synchronized void nuovoMessaggio(final Messaggio messaggio) throws NoSuchUserException {
        if(!messaggio.getIDutente().equals(utente.getIDutente())) {
            messaggiRicevuti.add(messaggio);
            ChatLogger.log("(Cronologia) messaggio in ingresso memorizzato: " + messaggio.getMsg() + " da " + messaggio.getUsername() + " (" + messaggio.getIDutente() + ")", ChatLoggerType.OPTIONAL);
        } else {
            ChatLogger.log("(Cronologia) messaggio personale '" + messaggio.getMsg() + "' non memorizzato [loop-back]", ChatLoggerType.OPTIONAL);
        }
    }

    /**
     * Memorizza un nuovo {@link Messaggio} inviato in output
     * @param messaggio il messaggio inviato
     */
    public synchronized void storicizzaMessaggio(final Messaggio messaggio) {
        messaggiInviati.add(messaggio);
        ChatLogger.log("(Cronologia) messaggio in uscita memorizzato: " + messaggio.getMsg() + " con msgID " + messaggio.getID(), ChatLoggerType.OPTIONAL);
    }

    /**
     * Ricevuto un {@link Messaggio} di <code>acknowledge</code> ricerca la corrispondenza tra l'<code>ID</code> contenuto nel corpo del <code>messaggio</code> e l'<code>ID</code> di ciascuno dei {@link #messaggiInviati}.
     * Se la ricerca va a buon fine, il valore <code>contaACK</code> del <code>messaggio</code> viene incrementato con il metodo {@link Messaggio#ACK()}.
     * 
     * @param messaggio il messaggio ACK ricevuto
     */
    public synchronized void confermaDiLettura(final Messaggio messaggio) {
        ChatLogger.log("(Cronologia) messaggio di ACK ricevuto: ricerca in corso per msgID " + messaggio.getMsg(), ChatLoggerType.OPTIONAL);
        for(Messaggio msgInviato : messaggiInviati){
            if((msgInviato.getID() == Integer.parseInt(messaggio.getMsg())) && (msgInviato.isInviatoCorrettamente() == 0)){
                msgInviato.ACK();
                ChatLogger.log("(Cronologia) match msgID per ACK avvenuto", ChatLoggerType.OPTIONAL);
            }
        }
    }

    /**
     * Fornisce un nuovo <code>ID</code> per un {@link Messaggio} creato da uno specifico {@link chat.host.MulticastPeer} incrementando di <code>1</code> ad ogni chiamata
     * @return il nuovo <code>ID</code> generato
     */
    public synchronized int getNewID() {
        IDs.add(IDs.size() + 1);
        return IDs.getLast();
    }

    /**
     * Calcola le statistiche inerenti ai {@link #messaggiInviati}.
     * <p>
     * Per ciascun <code>messaggio</code>, viene invocato il metodo {@link Messaggio#isInviatoCorrettamente()} ottenendo una stima dei messaggi in cui il numero di <code>ACK</code> corrisponde al valore atteso.
     *     
     * @return le statistiche calcolate
     */
    public String getStatistiche() {
        String stat = "Calcolo statistiche...\n";
        int msgInviati = messaggiInviati.size();
        int msgInviatiCorrettamente = 0;
        for(Messaggio msg : messaggiInviati){
            msgInviatiCorrettamente += msg.isInviatoCorrettamente();
            stat += "(msgID " + msg.getID() + ") " + msg.getContaACK() + " ACK di " + msg.getTargetACK() + " richiesti, con contenuto: '" + msg.getMsg() + "'\n";
        }

        double tot = Math.round(((double) msgInviatiCorrettamente / (double) msgInviati) * 100.0);

        stat += "Messaggi inviati: " + msgInviati + " | ";
        stat += "Messaggi con ACK: " + msgInviatiCorrettamente + " | ";
        stat += "Percentuale di successo: " + tot + "%";
        return stat;
    }
}