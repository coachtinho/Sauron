package pt.tecnico.sauron.silo.domain;

public enum ErrorMessage {
    INVALID_NAME("Name doesn't match the rules"), //
    CAMERA_ALREADY_EXISTS("Camera with that name already exists"), //
    NO_SUCH_CAMERA("There's nocamera with that name"), //
    INVALID_PERSON_ID("Person ID doesn't match rules"), //
    INAVLID_CAR_ID("Car ID doesn't match rules");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
