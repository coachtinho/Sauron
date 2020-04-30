package pt.tecnico.sauron.spotter;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.client.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.InitRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TraceRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackMatchRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.Timestamp;

import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;

public class Spotter {

    SiloFrontend frontend;

    public Spotter(String host, String port, String instance) throws SiloFrontendException {
        frontend = new SiloFrontend(host, port, instance);
    }

    public void spot(String type, String id) {
        try {
            if (id.contains("*")) {
                TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(type).setId(id).build();
                LinkedList<TrackResponse> observations = new LinkedList<TrackResponse>();
                observations.addAll(frontend.trackMatch(request).getObservationList()); 
                Collections.sort(observations, new Comparator<TrackResponse>() {
                    @Override
                    public int compare(TrackResponse t1, TrackResponse t2) {
                        switch (t1.getType()) {
                            case "person":
                                return ((Long) Long.parseLong(t1.getId())).compareTo((Long) Long.parseLong(t2.getId()));                               
                            case "car":
                                return t1.getId().compareTo(t2.getId());  
                            default:
                                return 0;
                        }
                    }
                });
                observations.forEach((observation) -> printObservation(observation));
            } else {
                TrackRequest request = TrackRequest.newBuilder().setType(type).setId(id).build();
                TrackResponse response = frontend.track(request);
                if (!response.getType().isBlank()) {
                    printObservation(response);
                } 
            }
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        } catch (SiloFrontendException exception) {
            handleFrontendException(exception);
        }
    }

    public void trail(String type, String id) {
        try {
            TraceRequest request = TraceRequest.newBuilder().setType(type).setId(id).build();
            List<TrackResponse> observations = frontend.trace(request).getObservationList();
            observations.forEach((observation) -> printObservation(observation));
        } catch (StatusRuntimeException exception) {
           handleRuntimeException(exception);
        } catch (SiloFrontendException exception) {
            handleFrontendException(exception);
        }
    }

    public void printObservation(TrackResponse observation) {
        System.out.printf("%s,%s,%s,%s,%f,%f%n", observation.getType(), observation.getId(),  timeConverter(observation.getTimestamp()).toString(), 
                observation.getName(), observation.getLatitude(), observation.getLongitude());
    }

    public LocalDateTime timeConverter(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()).atZone(ZoneId.of("Portugal")).toLocalDateTime();
    }

    public void ping() {
        try {
            PingRequest request = PingRequest.newBuilder().setMessage("Hello server, are you there!?").build();
            System.out.println("Server answered with:" + frontend.ctrlPing(request).getMessage());
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        } 
    }

    public void clear() {
        try {
            ClearRequest request = ClearRequest.newBuilder().build();
            frontend.ctrlClear(request);
            System.out.println("Server state cleared");
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        }
    }

    public void init() {
        try {
            InitRequest request = InitRequest.newBuilder().build();
            frontend.ctrlInit(request);
            System.out.println("Server initialized");
        } catch (StatusRuntimeException exception) {
            handleRuntimeException(exception);
        }
    }

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