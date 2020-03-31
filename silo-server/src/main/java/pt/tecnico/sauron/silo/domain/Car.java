package pt.tecnico.sauron.silo.domain;

public class Car extends Observation {

    String _name;

    public Car(String name, Camera cam) {
        super(cam);
        _name = name;
    }

    @Override
    public synchronized String toString() {
        return "Car," + _name + "," + super.toString();
    }

}
