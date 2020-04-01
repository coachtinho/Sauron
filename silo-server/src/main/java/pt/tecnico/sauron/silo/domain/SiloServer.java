package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.lang.String;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SiloServer {
    private Map<String, Camera> _cameras;
    private Map<String, ArrayList<Observation>> _cars;
    private Map<String, ArrayList<Observation>> _persons;

    public SiloServer() {
        _cameras = new HashMap<>();
        _cars = Collections.synchronizedMap(new LinkedHashMap<>());
        _persons = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    // Track command
    public Observation trackPerson(String id) {
        if (!Person.isValidId(id))
            throw new SiloException(ErrorMessage.INVALID_PERSON_ID);
        return track(_persons, id);
    }

    public Observation trackCar(String id) {
        if (!Car.isValidId(id))
            throw new SiloException(ErrorMessage.INAVLID_CAR_ID);
        return track(_cars, id);
    }

    // Having a generic method for command allows for adding more types of
    // observations in the future
    private Observation track(Map<String, ArrayList<Observation>> observations, String id) {
        if (observations.containsKey(id)) {
            ArrayList<Observation> list = observations.get(id);

            // sorting is handled in insertion so the first element is always the most
            // recent
            return list != null ? list.get(0) : null;
        }

        return null;
    }

    // Trace command
    public List<Observation> tracePerson(String id) {
        if (!Person.isValidId(id))
            throw new SiloException(ErrorMessage.INVALID_PERSON_ID);
        return _persons.get(id);
    }

    public List<Observation> traceCar(String id) {
        if (!Car.isValidId(id))
            throw new SiloException(ErrorMessage.INAVLID_CAR_ID);
        return _cars.get(id);
    }

    // Track Match command
    public List<Observation> trackMatchPerson(String id) {
        if (!Person.isValidId(id))
            throw new SiloException(ErrorMessage.INVALID_PERSON_ID);
        return trackMatch(_persons, id);
    }

    public List<Observation> trackMatchCar(String id) {
        if (!Car.isValidId(id))
            throw new SiloException(ErrorMessage.INAVLID_CAR_ID);
        return trackMatch(_cars, id);
    }

    // Having a generic method for command allows for adding more types of
    // observations in the future
    private List<Observation> trackMatch(Map<String, ArrayList<Observation>> observations, String expr) {
        List<Observation> list = new ArrayList<>();

        if (!expr.contains("*")) {
            for (String id : observations.keySet()) {
                if (id.contains(expr)) {
                    // sorting is handled in insertion so the first element is always most recent
                    list.add(observations.get(id).get(0));
                }
            }
        } else {
            // since split method uses regex it's better to not use a special regex
            // characater
            String[] patterns = expr.replace("*", "@").split("@", -1);

            for (String id : observations.keySet()) {
                if (id.startsWith(patterns[0]) && id.endsWith(patterns[1])) {
                    // sorting is handled in insertion so the first element is always most recent
                    list.add(observations.get(id).get(0));
                }
            }
        }

        return list;
    }

    // TODO: Create custom exception
    public boolean registerCamera(Camera camera) {
        if (_cameras.containsKey(camera.getName()))
            throw new SiloException(ErrorMessage.CAMERA_ALREADY_EXISTS);
        _cameras.put(camera.getName(), camera);
        return true;
    }

    public Camera camInfo(String name) {
        return _cameras.get(name);
    }

    public void clear() {
        _cameras = Collections.synchronizedMap(new HashMap<>());
        _cars = Collections.synchronizedMap(new LinkedHashMap<>());
        _persons = Collections.synchronizedMap(new LinkedHashMap<>());
    }

}
