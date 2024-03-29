package pt.tecnico.sauron.silo.domain;

import java.lang.String;
import java.time.LocalDateTime;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationType;;

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
    public ObservationType getType() {
        return ObservationType.PERSON;
    }

    public static boolean isValidId(String id) {
        return id.matches("[0-9]+");
    }

    @Override
    public synchronized String toString() {
        return "Person," + _id + "," + super.toString();
    }

}
