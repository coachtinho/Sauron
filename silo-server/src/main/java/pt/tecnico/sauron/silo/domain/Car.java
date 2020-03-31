package pt.tecnico.sauron.silo.domain;

public class Car extends Observation {

    String _id;

    public Car(String id, Camera cam) {
        super(cam);
        _id = id;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public String getType() {
        return "car";
    }

    @Override
    public synchronized String toString() {
        return "Car," + _id + "," + super.toString();
    }

}
