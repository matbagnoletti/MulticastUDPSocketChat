/**
 * Fornisce le classi-entità necessarie alla gestione degli oggetti <code>host</code>.
 * <p>
 * Ciascun {@code host} rappresenta un end-point che comunica con un altro (o altri) end-point attraverso la rete, sfruttando i {@link java.net.DatagramSocket} con protocollo UDP.
 * <p>
 * Contiene:
 * <ul>
 *     <li>{@link chat.host.GroupChat}</li>
 *     <li>{@link chat.host.MulticastPeer}</li>
 * </ul>
 * 
 * @author Matteo Bagnoletti Tini
 * @version 1.0
 * @project MulticastUDPSocketChat
 */
package chat.host;