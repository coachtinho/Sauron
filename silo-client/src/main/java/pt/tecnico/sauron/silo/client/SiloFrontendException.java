package pt.tecnico.sauron.silo.client;

public class SiloFrontendException extends Exception {

    private static final long serialVersionUID = 1L;

    public SiloFrontendException() {
    }

    public SiloFrontendException(String message) {
        super(message);
    }

    public SiloFrontendException(Throwable t) {
        super(t);
    }

    public SiloFrontendException(String message, Throwable t) {
        super(message, t);
    }

}