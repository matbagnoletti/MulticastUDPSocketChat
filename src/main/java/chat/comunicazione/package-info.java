/**
 * Fornisce le classi-entità per la gestione della comunicazione tra {@link chat.host.MulticastPeer}.
 * <p>
 * La comunicazione prevede uno scambio di oggetti {@link chat.comunicazione.Messaggio} dedicati.
 * La gestione e organizzazione di ciascun <code>messaggio</code> è demandata all'<code>host</code> che lo ha generato o ricevuto, attraverso opportuni metodi di cui è fornito, e in particolare mediante un oggetto di tipo {@link chat.comunicazione.Cronologia}.
 * <p>
 * Dalla versione <code>v1.2</code> ciascun <code>messaggio</code> deve contenere il {@link chat.comunicazione.Protocollo} utilizzato.   
 * <p>
 * Contiene:
 * <ul>
 *     <li>{@link chat.comunicazione.Cronologia}</li>
 *     <li>{@link chat.comunicazione.Messaggio}</li>
 *     <li>{@link chat.comunicazione.Protocollo}</li>
 * </ul>
 * 
 * @author Matteo Bagnoletti Tini
 * @version 1.1
 * @since 1.0
 * @project MulticastUDPSocketChat
 */
package chat.comunicazione;