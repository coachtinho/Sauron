package pt.tecnico.sauron.silo;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.ALREADY_EXISTS;

import java.util.Vector;
import java.time.LocalDateTime;
import java.util.List;

import pt.tecnico.sauron.silo.domain.Observation;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;
import pt.tecnico.sauron.silo.grpc.Silo.ReportResponse.FailureItem;
import pt.tecnico.sauron.silo.grpc.SauronGrpc.SauronImplBase;

import com.google.protobuf.Timestamp;

public class SiloServerImpl extends SauronImplBase {

    private final SiloServer _siloServer;
    private final ReplicaManager _replicaManager;

    public SiloServerImpl(ReplicaManager replicaManager, SiloServer siloServer) {
        _siloServer = siloServer;
        _replicaManager = replicaManager;
    }

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

    @Override
    public void ctrlClear(final ClearRequest request, final StreamObserver<ClearResponse> responseObserver) {
        _siloServer.clear();
        final ClearResponse response = ClearResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(final InitRequest request, final StreamObserver<InitResponse> responseObserver) {
        _siloServer.registerCamera("Camera1", 678.91, 123.45);
        _siloServer.reportObservation("Camera1", "car", "87JB40", LocalDateTime.now());
        _siloServer.reportObservation("Camera1", "person", "12345", LocalDateTime.now());
        final InitResponse response = InitResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void track(final TrackRequest request, final StreamObserver<TrackResponse> responseObserver) {
        final String type = request.getType();
        final String id = request.getId();
        Observation obs;

        if (type.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        try {
            if (type.equals("person")) {
                obs = _siloServer.trackPerson(id);
            } else if (type.equals("car")) {
                obs = _siloServer.trackCar(id);
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

        Vector<Integer> valueTS = _replicaManager.getTS();
        responseBuilder.addAllTs(valueTS);

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

        if (type.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        try {
            if (type.equals("person")) {
                observations = _siloServer.tracePerson(id);
            } else if (type.equals("car")) {
                observations = _siloServer.traceCar(id);
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

        Vector<Integer> valueTS = _replicaManager.getTS();
        responseBuilder.addAllTs(valueTS);

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

        if (type.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        try {
            if (type.equals("person")) {
                observations = _siloServer.trackMatchPerson(id);
            } else if (type.equals("car")) {
                observations = _siloServer.trackMatchCar(id);
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

        Vector<Integer> valueTS = _replicaManager.getTS();
        responseBuilder.addAllTs(valueTS);

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

        if (name.isBlank()) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
        else if (name.length() > 15 || name.length() < 3) // Check name size
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name length is illegal!").asRuntimeException());
        else {
            try {
                _siloServer.registerCamera(name, request.getLatitude(), request.getLongitude());
                _replicaManager.logCamRegisterRequest(request);

                CameraRegistrationResponse.Builder responseBuilder = CameraRegistrationResponse.newBuilder();

                Vector<Integer> valueTS = _replicaManager.update();
                responseBuilder.addAllTs(valueTS);

                final CameraRegistrationResponse response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (SiloException e) {
                responseObserver.onError(ALREADY_EXISTS.withDescription("Camera already exists!").asRuntimeException());
            }

        }
    }

    @Override
    public void camInfo(final CameraInfoRequest request, final StreamObserver<CameraInfoResponse> responseObserver) {
        String name = request.getName();
        Camera cam;

        if (name.isBlank()) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
        else if ((cam = _siloServer.camInfo(name)) == null) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("No such camera!").asRuntimeException());
        else {
            double latitude = cam.getLatitude();
            double longitude = cam.getLongitude();

            CameraInfoResponse.Builder responseBuilder = CameraInfoResponse.newBuilder();

            Vector<Integer> valueTS = _replicaManager.getTS();
            responseBuilder.addAllTs(valueTS);

            responseBuilder.setLatitude(latitude).setLongitude(longitude);

            final CameraInfoResponse response = responseBuilder.build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void report(final ReportRequest request, final StreamObserver<ReportResponse> responseObserver) {
        String cameraName = request.getCameraName();

        // check if camera name is legit
        if (!_siloServer.hasCamera(cameraName)) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("No such camera").asRuntimeException());
        } else {
            // get reports
            List<ReportItem> items = request.getReportsList();
            ReportResponse.Builder responseBuilder = ReportResponse.newBuilder();

            // get message timestamp
            Vector<Integer> valueTS = _replicaManager.update();
            responseBuilder.addAllTs(valueTS);

            // Create timestamp
            LocalDateTime timestamp = LocalDateTime.now();

            // process report items
            for (ReportItem item : items) {
                String type = item.getType();
                String id = item.getId();
                if (!_siloServer.isValidType(type)) {
                    responseBuilder.addFailures(FailureItem.newBuilder() // invalid report type
                            .setType(type).setId(id).setMessage("Invalid type").build());
                } else if (!_siloServer.isValidId(type, id)) {
                    responseBuilder.addFailures(FailureItem.newBuilder() // invalid id
                            .setType(type).setId(id).setMessage("Invalid id '" + id + "' for type " + type).build());
                } else { // register report
                    _siloServer.reportObservation(cameraName, type, id, timestamp);
                }
            }
            _replicaManager.logReport(request, timestamp);

            // build response message
            ReportResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}
