package pt.tecnico.sauron.silo.domain;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class SiloServer {
    private Map<String, Camera> _cameras;
    // private ArrayList<Car> _observations; <-- use what coutinho codes

    public SiloServer() {
        _cameras = new HashMap<>();
        // _observations = new ArrayList<>();
    }

    // TODO: Create custom exception
    public boolean registerCamera(Camera camera) {
        if (_cameras.containsKey(camera.getName()))
            return false;
        _cameras.put(camera.getName(), camera);
        return true;
    }

}
