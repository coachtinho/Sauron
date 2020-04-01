package pt.tecnico.sauron.eye;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;

public class Eye {

    SiloFrontend frontend;

    public Eye(String host, int port) {
        frontend = new SiloFrontend(host, port);
    }

    public void spot(String type, String id) {

    }

    public void trail(String type, String id) {
        System.out.println("Executing trail");
    }

    public void ping() {
        
    }

    public void clear() {        
    }

    public void init() {        
    }

    public void help() {
        System.out.println(
                "Spot: shows information regarding observations of the objects with identifiers that match with id\n"
                        + "   Usage: spotter objectType id\n" + "Trail: shows the path taken by the object with id\n"
                        + "   Usage: trail objectType id\n" + "Ping: shows information regarding the state of server\n"
                        + "   Usage: ping\n" + "Clear: cleans server state\n" + "   Usage: clear\n"
                        + "Init: configures server\n" + "   Usage: init\n"
                        + "Help: shows commands supported by application\n" + "   Usage: help\n"
                        + "Exit: exits the application\n" + "   Usage: exit");
    }

    public void exit() {
        System.out.println("Exiting...");
        frontend.close();
    }

}