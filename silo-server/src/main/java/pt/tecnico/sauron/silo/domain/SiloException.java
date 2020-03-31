package pt.tecnico.sauron.silo.domain;

public class SiloException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ErrorMessage errorMessage;

    public SiloException(ErrorMessage errorMessage) {
        super(errorMessage.label);
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
