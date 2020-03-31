package pt.tecnico.sauron.silo.domain;

import java.lang.String;

public class Person extends Observation {

    String _id;

    public Person(String id, Camera cam) {
        super(cam);
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

    @Override
    public synchronized String toString() {
        return "Person," + _id + "," + super.toString();
    }

}
