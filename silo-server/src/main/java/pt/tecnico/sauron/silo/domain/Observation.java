package pt.tecnico.sauron.silo.domain;

import java.lang.String;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationType;;


public abstract class Observation {

    LocalDateTime _timestamp;
    DateTimeFormatter _dtf;
    Camera _cam;

    public Observation(Camera camera, LocalDateTime timestamp) {
        _cam = camera;
        _timestamp = timestamp;
        _dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    public String getDate() {
        return _dtf.format(_timestamp);
    }

    public Instant getInstant() {
        return _timestamp.atZone(ZoneId.systemDefault()).toInstant();
    }

    public abstract String getId();

    public abstract ObservationType getType();

    public String getCamName() {
        return _cam.getName();
    }

    public double getCamLat() {
        return _cam.getLatitude();
    }

    public double getCamLong() {
        return _cam.getLongitude();
    }

    @Override
    public synchronized String toString() {
        return this.getDate() + "," + _cam.toString();
    }

}
