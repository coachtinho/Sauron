package pt.tecnico.sauron.spotter;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;

public class Spotter {

    SiloFrontend frontend;

    public Spotter(String host, int port) {
        frontend = new SiloFrontend(host, port);
    }

    public void spot(String type, String id) {
        System.out.println("Executing spot");
    }
    
    public void trail(String type, String id) {
        System.out.println("Executing trail");
    }

    public void ping() {
        PingRequest request = PingRequest.newBuilder().setMessage("Hello server, are you there!").build();
        System.out.println("Server answered with:" + frontend.ctrlPing(request).getMessage());
    }

    public void clear() {
        System.out.println("Executing clear");
    }

    public void init() {
        System.out.println("Executing init");
    }

    public void help() {
        System.out.println(
            "Spot: shows information regarding observations of the objects with identifiers that start with id\n" +
            "   Usage: spotter objectType id\n" + 
            "Trail: shows the path taken by the object with id\n" + 
            "   Usage: trail objectType id\n" + 
            "Ping: shows information regarding the state of server\n" + 
            "   Usage: ping\n" + 
            "Clear: cleans server state\n" + 
            "   Usage: clear\n" + 
            "Init: configures server\n" + 
            "   Usage: init\n" + 
            "Help: shows commands supported by application\n" + 
            "   Usage: help\n" +
            "Exit: exits the application\n" + 
            "   Usage: exit"
        );
    }

    public void exit() {
        System.out.println("Exiting...");
        frontend.close();
    }

}