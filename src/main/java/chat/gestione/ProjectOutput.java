package chat.gestione;

/**
 * Classe di gestione dell'output del programma
 * @author Matteo Bagnoletti Tini
 * @version 1.0
 * @project MulticastUDPSocketChat
 * @see OutputType
 */
public class ProjectOutput {

    /**
     *Stampa a video di un dato messaggio
     * @param output il messaggio da stampare
     * @param isErrore indica se il messaggio è un errore
     * @param isCommento indica se il messaggio è un commento
     * @deprecated sostituito da {@link #stampa(String, OutputType)}
     */
    @Deprecated
    public static synchronized void outputApp(String output, boolean isErrore, boolean isCommento){
        if(isErrore) {
            System.err.println("\033[1;31m#\033[0m\033[31m " + output + "\033[0m");
        } else if(isCommento) {
            System.out.println("\033[1;37m# " + output + "\033[0m");
        } else {
            System.out.println("\033[1;32m> \033[0m" + output);
        }
    }

    /**
     * Stampa a video di un dato <code>messaggio</code>
     * @param output il messaggio da stampare
     * @param tipologia la tipologia di output
     * @see OutputType
     */
    public static synchronized void stampa(String output, OutputType tipologia) {
        switch (tipologia) {
            case OutputType.STDOUT -> System.out.println("\033[1;32m> \033[0m" + output);
            
            case OutputType.STDERR -> System.err.println("\033[1;31m#\033[0m\033[31m " + output + "\033[0m");
            
            case OutputType.LOG -> System.out.println("\033[1;37m# " + output + "\033[0m");
        }
    }
}
