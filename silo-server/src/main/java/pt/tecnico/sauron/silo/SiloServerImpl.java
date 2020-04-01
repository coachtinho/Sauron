package pt.tecnico.sauron.silo;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.ALREADY_EXISTS;

import pt.tecnico.sauron.silo.domain.Observation;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.Silo.SearchResponse;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ClearResponse;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingResponse;
import pt.tecnico.sauron.silo.grpc.Silo.SearchRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationMessage;
import pt.tecnico.sauron.silo.grpc.Silo.ResponseMessage;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import com.google.protobuf.Timestamp;
import java.util.List;

public class SiloServerImpl extends SauronGrpc.SauronImplBase {

    private final SiloServer siloServer = new SiloServer();

    @Override
    public void ctrlPing(final PingRequest request, final StreamObserver<PingResponse> responseObserver) {
        final String input = request.getMessage();

        if (input == null || input.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Message cannot be empty!").asRuntimeException());
        } else {

            final String output = "Hello " + input + "!";
            final PingResponse response = PingResponse.newBuilder().setMessage(output).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public void ctrlClear(final ClearRequest request, final StreamObserver<ClearResponse> responseObserver) {
            siloServer.clear();
            final ClearResponse response = ClearResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
    }

    @Override
    public void track(final SearchRequest request, final StreamObserver<ObservationMessage> responseObserver) {
        final String type = request.getType();
        final String id = request.getId();
        Observation obs;

        if (type == null) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id == null) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        if (type.equals("person")) {
            obs = siloServer.trackPerson(id);
        } else if (type.equals("car")) {
            obs = siloServer.trackCar(id);
        } else {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type is not a valid observation!").asRuntimeException());
            return;
        }

        Timestamp timestamp = Timestamp.newBuilder().setSeconds(obs.getInstant().getEpochSecond()).build();
        final ObservationMessage response = ObservationMessage.newBuilder()
            .setId(obs.getId()).setType(obs.getType()).setTimestamp(timestamp).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void trace(final SearchRequest request, final StreamObserver<SearchResponse> responseObserver) {
        final String type = request.getType();
        final String id = request.getId();
        List<Observation> observations;

        if (type == null) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id == null) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        if (type.equals("person")) {
            observations = siloServer.tracePerson(id);
        } else if (type.equals("car")) {
            observations = siloServer.traceCar(id);
        } else {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type is not a valid observation!").asRuntimeException());
            return;
        }

        SearchResponse.Builder responseBuilder = SearchResponse.newBuilder();

        for (Observation obs : observations) {
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(obs.getInstant().getEpochSecond()).build();
            final ObservationMessage observation = ObservationMessage.newBuilder()
                .setId(obs.getId()).setType(obs.getType()).setTimestamp(timestamp).build();
            responseBuilder.addObservation(observation);
        }

        final SearchResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void trackMatch(final SearchRequest request, final StreamObserver<SearchResponse> responseObserver) {
        final String type = request.getType();
        final String id = request.getId();
        List<Observation> observations;

        if (type == null) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id == null) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        if (type.equals("person")) {
            observations = siloServer.trackMatchPerson(id);
        } else if (type.equals("car")) {
            observations = siloServer.trackMatchCar(id);
        } else {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type is not a valid observation!").asRuntimeException());
            return;
        }

        SearchResponse.Builder responseBuilder = SearchResponse.newBuilder();

        for (Observation obs : observations) {
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(obs.getInstant().getEpochSecond()).build();
            final ObservationMessage observation = ObservationMessage.newBuilder()
                    .setId(obs.getId()).setType(obs.getType()).setTimestamp(timestamp).build();
            responseBuilder.addObservation(observation);
        }

        final SearchResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void camJoin(final CameraRegistrationRequest request,
            final StreamObserver<CameraRegistrationResponse> responseObserver) {
        String name = request.getName();
        if (name == null) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be null!").asRuntimeException());
        if (name.length() > 15 || name.length() < 3) // Check name size
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be null!").asRuntimeException());
        else if (request.getCoords() == null) // Check coords exist
            responseObserver
                    .onError(INVALID_ARGUMENT.withDescription("Coordinates cannot be null!").asRuntimeException());
        else {
            try {
                Camera cam = new Camera(name, request.getCoords().getLongitude(), request.getCoords().getLatitude());
                siloServer.registerCamera(cam);
            } catch (SiloException e) {                
                responseObserver.onError(
                        ALREADY_EXISTS.withDescription("Camera with taht id already exists!").asRuntimeException());
            }

            final CameraRegistrationResponse response = CameraRegistrationResponse.newBuilder()
                    .setResponse(ResponseMessage.SUCCESS).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}
