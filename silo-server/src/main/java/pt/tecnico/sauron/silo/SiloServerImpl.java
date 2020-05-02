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
        // ctrl operation to check if server is responsive
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
        // ctrl operation to clear the server's data
        _siloServer.clear();
        final ClearResponse response = ClearResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(final InitRequest request, final StreamObserver<InitResponse> responseObserver) {
        // ctrl operation to initialize server with some testing data
        _siloServer.registerCamera("Camera1", 678.91, 123.45);
        _siloServer.reportObservation("Camera1", ObservationType.CAR, "87JB40", LocalDateTime.now());
        _siloServer.reportObservation("Camera1", ObservationType.PERSON, "12345", LocalDateTime.now());
        final InitResponse response = InitResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void track(final TrackRequest request, final StreamObserver<TrackResponse> responseObserver) {
        final ObservationType type = request.getType();
        final String id = request.getId();
        Observation obs;

        if (type == ObservationType.UNKNOWN) { // check if type exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) { // check if id exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        try {
            // check if type of observation is valid and perform track operation
            if (type == ObservationType.PERSON) {
                obs = _siloServer.trackPerson(id);
            } else if (type == ObservationType.CAR) {
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

        // Build and send response
        TrackResponse.Builder responseBuilder = TrackResponse.newBuilder();

        // get server timestamp
        Vector<Integer> valueTS = _replicaManager.getTS();
        responseBuilder.addAllTs(valueTS);

        // add observation to message
        if (obs != null) {
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(obs.getInstant().getEpochSecond()).build();
            responseBuilder.setId(obs.getId()) //
                    .setType(obs.getType()) //
                    .setTimestamp(timestamp) //
                    .setName(obs.getCamName()) //
                    .setLatitude(obs.getCamLat()) //
                    .setLongitude(obs.getCamLong());
        }

        // build and send response
        final TrackResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void trace(final TraceRequest request, final StreamObserver<TraceResponse> responseObserver) {
        final ObservationType type = request.getType();
        final String id = request.getId();
        List<Observation> observations;

        if (type == ObservationType.UNKNOWN) { // check if type exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) { // check if id exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        try {
            // check if type of observation is valid and perform trace operation
            if (type == ObservationType.PERSON) {
                observations = _siloServer.tracePerson(id);
            } else if (type == ObservationType.CAR) {
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

        // get server timestamp
        Vector<Integer> valueTS = _replicaManager.getTS();
        responseBuilder.addAllTs(valueTS);

        // add observations to message
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

        // build and send response
        final TraceResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void trackMatch(final TrackMatchRequest request, final StreamObserver<TrackMatchResponse> responseObserver) {
        final ObservationType type = request.getType();
        final String id = request.getId();
        List<Observation> observations;

        if (type == ObservationType.UNKNOWN) { // check if type exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) { // check if id exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Id cannot be empty!").asRuntimeException());
            return;
        }

        try {
            // check if type of observation is valid and perform trackmatch operation
            if (type == ObservationType.PERSON) {
                observations = _siloServer.trackMatchPerson(id);
            } else if (type == ObservationType.CAR) {
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

        // get server timestamp
        Vector<Integer> valueTS = _replicaManager.getTS();
        responseBuilder.addAllTs(valueTS);

        // add observations to message
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

        // build and send response
        final TrackMatchResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void camJoin(final CameraRegistrationRequest request,
            final StreamObserver<CameraRegistrationResponse> responseObserver) {

        String name = request.getName();

        if (name.isBlank()) // check if name exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
        else if (name.length() > 15 || name.length() < 3) // check if name size fits restrictions
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name length is illegal!").asRuntimeException());
        else {
            try {
                // register camera in server and log operation
                _siloServer.registerCamera(name, request.getLatitude(), request.getLongitude());
                _replicaManager.logCamRegisterRequest(request);

                CameraRegistrationResponse.Builder responseBuilder = CameraRegistrationResponse.newBuilder();

                // update server timestamp
                Vector<Integer> valueTS = _replicaManager.update();
                responseBuilder.addAllTs(valueTS);

                // build and send response
                final CameraRegistrationResponse response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (SiloException e) {
                responseObserver.onError(ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
            }

        }
    }

    @Override
    public void camInfo(final CameraInfoRequest request, final StreamObserver<CameraInfoResponse> responseObserver) {
        String name = request.getName();
        Camera cam;

        if (name.isBlank()) // check if name exists in message
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
        else if ((cam = _siloServer.camInfo(name)) == null) // check if camera exists in server
            responseObserver.onError(INVALID_ARGUMENT.withDescription("No such camera!").asRuntimeException());
        else {
            // get parameters from server
            double latitude = cam.getLatitude();
            double longitude = cam.getLongitude();

            CameraInfoResponse.Builder responseBuilder = CameraInfoResponse.newBuilder();

            // get server timestamp
            Vector<Integer> valueTS = _replicaManager.getTS();
            responseBuilder.addAllTs(valueTS);

            // set message parameters
            responseBuilder.setLatitude(latitude).setLongitude(longitude);

            // build and send response
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
                ObservationType type = item.getType();
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
