package pt.tecnico.sauron.silo.domain;

import java.lang.String;

public class Person extends Observation {

    String _name;

    public Person(String name, Camera cam) {
        super(cam);
        _name = name;
    }

    @Override
    public synchronized String toString() {
        return "Person," + _name + "," + super.toString();
    }

}
