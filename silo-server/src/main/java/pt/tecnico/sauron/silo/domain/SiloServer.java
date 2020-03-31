package pt.tecnico.sauron.silo.domain;


import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

public class SiloServer {
    private Map<String, Camera> _cameras;
    private LinkedHashMap<String, ArrayList<Observation>> _cars;
    private LinkedHashMap<String, ArrayList<Observation>> _persons;

    public SiloServer() {
        _cameras = new HashMap<>();
        _cars = new LinkedHashMap<>();
        _persons = new LinkedHashMap<>();
    }

    // Track command
    public Observation trackPerson(String id) {
        return track(_persons, id);
    }

    public Observation trackCar(String id) {
        return track(_cars, id);
    }

    // Having a generic method for command allows for adding more types of observations in the future
    private Observation track(Map<String, ArrayList<Observation>> observations, String id) {
        if (observations.containsKey(id)) {
            ArrayList<Observation> list = observations.get(id);

            // sorting is handled in insertion so the first element is always most recent
            return list != null ? list.get(0) : null;
        }

        return null;
    }

    // Trace command
    public List<Observation> tracePerson(String id) {
        return _persons.get(id);
    }

    public List<Observation> traceCar(String id) {
        return _cars.get(id);
    }


    // Track Match command
    public List<Observation> trackMatchPerson(String id) {
        return trackMatch(_persons, id);
    }

    public List<Observation> trackMatchCar(String id) {
        return trackMatch(_cars, id);
    }

    // Having a generic method for command allows for adding more types of observations in the future
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
            String[] patterns = expr.split("'*'");// separate prefix and suffix

            for (String id : observations.keySet()) {
                if (id.startsWith(patterns[0]) && id.endsWith(patterns[1])) {
                    list.add(observations.get(id).get(0));
                }
            }
        }

        return list;
    }

    // TODO: Create custom exception
    public boolean registerCamera(Camera camera) {
        if (_cameras.containsKey(camera.getName()))
            return false;
        _cameras.put(camera.getName(), camera);
        return true;
    }

}
