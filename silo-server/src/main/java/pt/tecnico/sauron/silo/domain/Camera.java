package pt.tecnico.sauron.silo.domain;

public class Camera {

    double _latitude;
    double _longitude;
    String _name;

    public Camera(String name, double latitude, double longitude) {
        _name = name;
        _latitude = latitude;
        _longitude = longitude;        
    }

    public String getName() {
        return _name;
    }

    public double getLongitude() {
        return _longitude;
    }

    public double getLatitude() {
        return _latitude;
    }

    @Override
    public synchronized String toString() {
        return _name + "," + _latitude + "," + _longitude;
    }
}
