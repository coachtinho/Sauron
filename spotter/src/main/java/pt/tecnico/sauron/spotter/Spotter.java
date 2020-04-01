package pt.tecnico.sauron.spotter;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TraceRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackMatchRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackRequest;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;

public class Spotter {

    SiloFrontend frontend;

    public Spotter(String host, int port) {
        frontend = new SiloFrontend(host, port);
    }

    public void spot(String type, String id) {
        try {
            if (id.contains("*")) {
                TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(type).setId(id).build();
                // frontend.trackMatch(request);
            } else {
                TrackRequest request = TrackRequest.newBuilder().setType(type).setId(id).build();
                // frontend.track(request);
            }
        } catch (StatusRuntimeException exception) {
            handleException(exception);
        }
    }

    public void trail(String type, String id) {
        try {
            TraceRequest request = TraceRequest.newBuilder().setType(type).setId(id).build();
            // frontend.trace(request);
        } catch (StatusRuntimeException exception) {
            handleException(exception);
        }
    }

    public void ping() {
        try {
            PingRequest request = PingRequest.newBuilder().setMessage("Hello server, are you there!?").build();
            System.out.println("Server answered with:" + frontend.ctrlPing(request).getMessage());
        } catch (StatusRuntimeException exception) {
            handleException(exception);
        }
    }

    public void clear() {
        try {
            ClearRequest request = ClearRequest.newBuilder().build();
            frontend.ctrlClear(request);
            System.out.println("Server state cleared");
        } catch (StatusRuntimeException exception) {
            handleException(exception);
        }
    }

    public void init() {
        // TODO: implement
        System.out.println("To be implemented");
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

    // Add more custom messages if needed
    public void handleException(StatusRuntimeException exception) {
        Code statusCode = exception.getStatus().getCode();
        switch (statusCode) {
            case UNAVAILABLE:
                System.out.println("Server is currently unavailable");
                break;
            default:
                System.out.println(
                        "Caught exception with code " + statusCode + " and description: " + exception.getMessage());
        }
    }

}