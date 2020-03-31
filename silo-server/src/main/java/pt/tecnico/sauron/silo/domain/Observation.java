package pt.tecnico.sauron.silo.domain;

import java.lang.String;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Observation {

    LocalDateTime _timestamp;
    DateTimeFormatter dtf;
    Camera _cam;

    public Observation(Camera camera) {
        _cam = camera;
        _timestamp = LocalDateTime.now();
        dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    public String getDate() {
        return dtf.format(_timestamp);
    }

    @Override
    public synchronized String toString() {
        return this.getDate() + "," + _cam.toString();
    }

}
