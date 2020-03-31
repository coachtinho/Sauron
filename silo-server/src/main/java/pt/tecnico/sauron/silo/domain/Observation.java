package pt.tecnico.sauron.silo.domain;


import java.lang.String;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Observation {

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

    public Instant getInstant() {
        return _timestamp.atZone(ZoneId.systemDefault()).toInstant();
    }

    public abstract String getId();

    public abstract String getType();

    @Override
    public synchronized String toString() {
        return this.getDate() + "," + _cam.toString();
    }

}
