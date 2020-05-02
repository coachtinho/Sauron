package pt.tecnico.sauron.spotter;

import pt.tecnico.sauron.silo.client.*;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.InitRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TraceRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackMatchRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.Timestamp;

import io.grpc.StatusRuntimeException;

public class Spotter {

    SiloFrontend frontend;

    public Spotter(String host, String port, String instance) throws SiloFrontendException {
        frontend = new SiloFrontend(host, port, instance);
    }

    // spot command
    public void spot(ObservationType type, String id) {
        try {
            if (id.contains("*")) {
                // create a track match request with partial id and send it
                TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(type).setId(id).build();
                LinkedList<TrackResponse> observations = new LinkedList<TrackResponse>();
                observations.addAll(frontend.trackMatch(request).getObservationList()); 
                Collections.sort(observations, new Comparator<TrackResponse>() {
                    // compare function for sorting
                    @Override
                    public int compare(TrackResponse t1, TrackResponse t2) {
                        switch (t1.getType()) {
                            case PERSON:
                                return ((Long) Long.parseLong(t1.getId())).compareTo((Long) Long.parseLong(t2.getId()));                               
                            case CAR:
                                return t1.getId().compareTo(t2.getId());  
                            default:
                                return 0;
                        }
                    }
                });
                // print returned observations
                observations.forEach((observation) -> printObservation(observation));
            } else {
                // create track request with full id and send it
                TrackRequest request = TrackRequest.newBuilder().setType(type).setId(id).build();
                TrackResponse response = frontend.track(request);
                if (response.getType() != ObservationType.UNKNOWN) {
                    // print returned observation
                    printObservation(response);
                } 
            }
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        } catch (SiloFrontendException exception) {
            handleFrontendException(exception);
        }
    }

    // trail command
    public void trail(ObservationType type, String id) {
        try {
            // create trace request and send it
            TraceRequest request = TraceRequest.newBuilder().setType(type).setId(id).build();
            List<TrackResponse> observations = frontend.trace(request).getObservationList();
            // print observations
            observations.forEach((observation) -> printObservation(observation));
        } catch (StatusRuntimeException exception) {
           handleRuntimeException(exception);
        } catch (SiloFrontendException exception) {
            handleFrontendException(exception);
        }
    }

    public void printObservation(TrackResponse observation) {
        String type;
        switch (observation.getType()) {
            // translate observation type to string
            case PERSON:
                type = "person";
                break;
            case CAR:
                type = "car";
                break;
            default:
                type = "unknown";

        }
        System.out.printf("%s,%s,%s,%s,%f,%f%n", type, observation.getId(),  timeConverter(observation.getTimestamp()).toString(), 
                observation.getName(), observation.getLatitude(), observation.getLongitude());
    }

    public LocalDateTime timeConverter(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()).atZone(ZoneId.of("Portugal")).toLocalDateTime();
    }

    // ping command
    public void ping() {
        try {
            PingRequest request = PingRequest.newBuilder().setMessage("Hello server, are you there!?").build();
            System.out.println("Server answered with:" + frontend.ctrlPing(request).getMessage());
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        } 
    }

    // clear command
    public void clear() {
        try {
            ClearRequest request = ClearRequest.newBuilder().build();
            frontend.ctrlClear(request);
            System.out.println("Server state cleared");
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        }
    }

    // init command
    public void init() {
        try {
            InitRequest request = InitRequest.newBuilder().build();
            frontend.ctrlInit(request);
            System.out.println("Server initialized");
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        }
    }

    // AUXILIARY FUNCTIONS

    public void help() {
        System.out.println(
                "Spot: shows information regarding observations of the objects with identifiers that match with id\n" + //
                        "   Usage: spotter objectType id\n" + //
                        "Trail: shows the path taken by the object with id\n" + //
                        "   Usage: trail objectType id\n" + //
                        "Ping: shows information regarding the state of server\n" + //
                        "   Usage: ping\n" + //
                        "Clear: cleans server state\n" + //
                        "   Usage: clear\n" + //
                        "Init: configures server\n" + //
                        "   Usage: init\n" + //
                        "Help: shows commands supported by application\n" + //
                        "   Usage: help\n" + //
                        "Exit: exits the application\n" + //
                        "   Usage: exit" //
        );
    }

    public void exit() {
        System.out.println("Exiting...");
        frontend.close();
    }

    public void handleRuntimeException(StatusRuntimeException exception) {
        System.out.println("Caught exception with code " + exception.getStatus().getCode() + " and description: " + exception.getMessage()); 
    }

    public void handleFrontendException(SiloFrontendException exception) {
        System.out.println(exception.getMessage());
    }

}