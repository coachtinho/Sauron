package pt.tecnico.sauron.silo.domain;

public enum ErrorMessage {
    INVALID_NAME("Name doesn't match the rules"), //
    CAMERA_ALREADY_EXISTS("Camera with that name already exists");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
