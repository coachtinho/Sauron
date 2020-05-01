package pt.tecnico.sauron.silo.domain;

import java.lang.String;
import java.time.LocalDateTime;

public class Person extends Observation {

    String _id;

    public Person(String id, Camera cam, LocalDateTime timestamp) {
        super(cam, timestamp);
        _id = id;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public String getType() {
        return "person";
    }

    public static boolean isValidId(String id) {
        return id.matches("[0-9]+");
    }

    @Override
    public synchronized String toString() {
        return "Person," + _id + "," + super.toString();
    }

}
