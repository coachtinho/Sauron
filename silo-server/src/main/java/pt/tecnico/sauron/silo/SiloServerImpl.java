package pt.tecnico.sauron.silo;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.ALREADY_EXISTS;

import pt.tecnico.sauron.silo.domain.Observation;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.Silo.*;
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
    public void track(final TrackRequest request, final StreamObserver<TrackResponse> responseObserver) {
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

        try {
            if (type.equals("person")) {
                obs = siloServer.trackPerson(id);
            } else if (type.equals("car")) {
                obs = siloServer.trackCar(id);
            } else {
                responseObserver.onError(
                        INVALID_ARGUMENT.withDescription("Type is not a valid observation!").asRuntimeException());
                return;
            }
        } catch (SiloException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            return;
        }

        TrackResponse.Builder responseBuilder = TrackResponse.newBuilder();

        if (obs != null) {
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(obs.getInstant().getEpochSecond()).build();
            responseBuilder.setId(obs.getId()) //
                    .setType(obs.getType()) //
                    .setTimestamp(timestamp) //
                    .setName(obs.getCamName()) //
                    .setLatitude(obs.getCamLat()) //
                    .setLongitude(obs.getCamLong());
        }

        final TrackResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void trace(final TraceRequest request, final StreamObserver<TraceResponse> responseObserver) {
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

        try {
            if (type.equals("person")) {
                observations = siloServer.tracePerson(id);
            } else if (type.equals("car")) {
                observations = siloServer.traceCar(id);
            } else {
                responseObserver.onError(
                        INVALID_ARGUMENT.withDescription("Type is not a valid observation!").asRuntimeException());
                return;
            }
        } catch (SiloException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            return;
        }

        TraceResponse.Builder responseBuilder = TraceResponse.newBuilder();

        if (observations != null && !observations.isEmpty()) {

            for (Observation obs : observations) {
                Timestamp timestamp = Timestamp.newBuilder().setSeconds(obs.getInstant().getEpochSecond()).build();
                final TrackResponse observation = TrackResponse.newBuilder() //
                        .setId(obs.getId()) //
                        .setType(obs.getType())//
                        .setTimestamp(timestamp) //
                        .setName(obs.getCamName()) //
                        .setLatitude(obs.getCamLat()) //
                        .setLongitude(obs.getCamLong()) //
                        .build();
                responseBuilder.addObservation(observation);
            }
        }

        final TraceResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void trackMatch(final TrackMatchRequest request, final StreamObserver<TrackMatchResponse> responseObserver) {
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

        try {
            if (type.equals("person")) {
                observations = siloServer.trackMatchPerson(id);
            } else if (type.equals("car")) {
                observations = siloServer.trackMatchCar(id);
            } else {
                responseObserver.onError(
                        INVALID_ARGUMENT.withDescription("Type is not a valid observation!").asRuntimeException());
                return;
            }
        } catch (SiloException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            return;
        }

        TrackMatchResponse.Builder responseBuilder = TrackMatchResponse.newBuilder();

        if (observations != null && !observations.isEmpty()) {

            for (Observation obs : observations) {
                Timestamp timestamp = Timestamp.newBuilder().setSeconds(obs.getInstant().getEpochSecond()).build();
                final TrackResponse observation = TrackResponse.newBuilder() //
                        .setId(obs.getId()) //
                        .setType(obs.getType()) //
                        .setTimestamp(timestamp) //
                        .setName(obs.getCamName()) //
                        .setLatitude(obs.getCamLat()) //
                        .setLongitude(obs.getCamLong()) //
                        .build();
                responseBuilder.addObservation(observation);
            }
        }

        final TrackMatchResponse response = responseBuilder.build();
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
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name length is illegal!").asRuntimeException());
        else {
            try {
                Camera cam = new Camera(name, request.getLongitude(), request.getLatitude());
                siloServer.registerCamera(cam);
            } catch (SiloException e) {
                responseObserver.onError(ALREADY_EXISTS.withDescription("Camera already exists!").asRuntimeException());
            }

            final CameraRegistrationResponse response = CameraRegistrationResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void camInfo(final CameraInfoRequest request, final StreamObserver<CameraInfoResponse> responseObserver) {
        String name = request.getName();
        Camera cam;
        if (name == null) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be null!").asRuntimeException());
        else if ((cam = siloServer.camInfo(name)) == null) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("No such camera!").asRuntimeException());
        else {
            String latitude = Double.toString(cam.getLatitude());
            String longitude = Double.toString(cam.getLongitude());
            final CameraInfoResponse response = CameraInfoResponse.newBuilder() //
                    .setLatitude(latitude).setLongitude(longitude).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}
