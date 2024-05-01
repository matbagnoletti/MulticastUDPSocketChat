package chat;

import chat.gestione.OutputType;
import chat.gestione.ProjectOutput;
import chat.host.GroupChat;
import chat.host.MulticastPeer;

/**
 * Classe di avvio del programma
 * @author Matteo Bagnoletti Tini
 */
public class RunMe {
    public static void main(String[] args) {
        if(args.length == 4) {
            try {
                GroupChat groupChat = new GroupChat(args[1], Integer.parseInt(args[2]));
                MulticastPeer MulticastPeer = new MulticastPeer(args[0], Boolean.parseBoolean(args[3]), groupChat);
                MulticastPeer.configura();
                MulticastPeer.avvia();
            } catch (Exception e) {
                ProjectOutput.stampa(e.getMessage(), OutputType.STDERR);
            }
        } else {
            try {
                GroupChat groupChat = new GroupChat("230.19.6.5", 19065);
                MulticastPeer multicastPeer = new MulticastPeer("Matteo", false, groupChat);
                multicastPeer.configura();
                multicastPeer.avvia();
            } catch (Exception e) {
                ProjectOutput.stampa(e.getMessage(), OutputType.STDERR);
            }
        }
    }
}
