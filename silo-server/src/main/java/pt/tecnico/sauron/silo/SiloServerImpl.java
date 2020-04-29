package pt.tecnico.sauron.silo;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.ALREADY_EXISTS;

import java.util.Vector;
import java.util.List;

import pt.tecnico.sauron.silo.domain.Observation;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;
import pt.tecnico.sauron.silo.grpc.Silo.ReportResponse.FailureItem;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import com.google.protobuf.Timestamp;


public class SiloServerImpl extends SauronGrpc.SauronImplBase {

    private final SiloServer siloServer;
    private final int _instance;
    private Vector<Integer> _valueTS;

    public SiloServerImpl(int instance) {
        siloServer = new SiloServer();
        _instance = instance;
        _valueTS = new Vector<Integer>();
        for (int i = 0; i < _instance; i++) _valueTS.add(0); // valueTS starts big enough to include own instance
    }

    private Vector<Integer> generateOtherTS(List<Integer> tsList) {
        // Get client's valueTS from list
        Vector<Integer> otherTS = new Vector<Integer>();

        for (int ts : tsList)
            otherTS.add(ts);

        // Ensure both vectors have same size
        if (_valueTS.size() > otherTS.size())
            for (int i = otherTS.size(); i < _valueTS.size(); i++)
                otherTS.add(0);
        else if (_valueTS.size() < otherTS.size())
            for (int i = _valueTS.size(); i < otherTS.size(); i++)
                _valueTS.add(0);

        return otherTS;
    }

    private boolean canUpdate(Vector<Integer> otherTS) {

        for (int i = 0; i < _valueTS.size(); i++)
            if (otherTS.get(i) > _valueTS.get(i)) return false;
            
        return true;
    }

    private Vector<Integer> updateTS(Vector<Integer> otherTS) {
        // Merges ts and increments replica's own value
        Vector<Integer> newTS = new Vector<Integer>();
        
        for (int i = 0; i < _valueTS.size(); i++)
            newTS.add(Math.max(_valueTS.get(i), otherTS.get(i)));

        int ts = newTS.get(_instance - 1) + 1;
        newTS.setElementAt(ts, _instance - 1);

        return newTS;
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
        siloServer.clear();
        final ClearResponse response = ClearResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(final InitRequest request, final StreamObserver<InitResponse> responseObserver) {
        siloServer.registerCamera("Camera1", 678.91, 123.45);
        siloServer.reportObservation("Camera1", "car", "87JB40");
        siloServer.reportObservation("Camera1", "person", "12345");
        final InitResponse response = InitResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void track(final TrackRequest request, final StreamObserver<TrackResponse> responseObserver) {
        final String type = request.getType();
        final String id = request.getId();
        Observation obs;
        Vector<Integer> otherTS = generateOtherTS(request.getTsList());

        if (type.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) {
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
                
        _valueTS = updateTS(otherTS);
        for (int ts : _valueTS)
            responseBuilder.addTs(ts);

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
        Vector<Integer> otherTS = generateOtherTS(request.getTsList());

        if (type.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) {
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
                
        _valueTS = updateTS(otherTS);
        for (int ts : _valueTS)
            responseBuilder.addTs(ts);

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
        Vector<Integer> otherTS = generateOtherTS(request.getTsList());

        if (type.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Type cannot be empty!").asRuntimeException());
            return;
        } else if (id.isBlank()) {
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
                
        _valueTS = updateTS(otherTS);
        for (int ts : _valueTS)
            responseBuilder.addTs(ts);

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
        Vector<Integer> otherTS = generateOtherTS(request.getTsList());

        if (name.isBlank()) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
        else if (name.length() > 15 || name.length() < 3) // Check name size
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name length is illegal!").asRuntimeException());
        else if (!canUpdate(otherTS)) {
            CameraRegistrationResponse.Builder responseBuilder = CameraRegistrationResponse.newBuilder();

            for (int ts : _valueTS)
                responseBuilder.addTs(ts);

            final CameraRegistrationResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            try {
                siloServer.registerCamera(name, request.getLatitude(), request.getLongitude());
                
                CameraRegistrationResponse.Builder responseBuilder = CameraRegistrationResponse.newBuilder();
                
                _valueTS = updateTS(otherTS);
                for (int ts : _valueTS)
                    responseBuilder.addTs(ts);

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
        Vector<Integer> otherTS = generateOtherTS(request.getTsList());

        if (name.isBlank()) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
        else if ((cam = siloServer.camInfo(name)) == null) // Check name exists
            responseObserver.onError(INVALID_ARGUMENT.withDescription("No such camera!").asRuntimeException());
        else {
            double latitude = cam.getLatitude();
            double longitude = cam.getLongitude();

            CameraInfoResponse.Builder responseBuilder = CameraInfoResponse.newBuilder();
                
            _valueTS = updateTS(otherTS);
            for (int ts : _valueTS)
                responseBuilder.addTs(ts);

            responseBuilder.setLatitude(latitude).setLongitude(longitude);

            final CameraInfoResponse response = responseBuilder.build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void report(final ReportRequest request, final StreamObserver<ReportResponse> responseObserver) {
        String cameraName = request.getCameraName();
        Vector<Integer> otherTS = generateOtherTS(request.getTsList());
        
        if (!siloServer.hasCamera(cameraName)) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("No such camera").asRuntimeException());
        } else if (!canUpdate(otherTS)) {
            ReportResponse.Builder responseBuilder = ReportResponse.newBuilder();

            _valueTS = updateTS(otherTS);
            for (int ts : _valueTS)
                responseBuilder.addTs(ts);

            ReportResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            List<ReportItem> items = request.getReportsList();
            ReportResponse.Builder responseBuilder = ReportResponse.newBuilder();

            _valueTS = updateTS(otherTS);
            for (int ts : _valueTS)
                responseBuilder.addTs(ts);

            for (ReportItem item : items) {
                String type = item.getType();
                String id = item.getId();
                if (!siloServer.isValidType(type)) {
                    responseBuilder.addFailures(
                            FailureItem.newBuilder().setType(type).setId(id).setMessage("Invalid type").build());
                } else if (!siloServer.isValidId(type, id)) {
                    responseBuilder.addFailures(FailureItem.newBuilder().setType(type).setId(id)
                            .setMessage("Invalid id '" + id + "' for type " + type).build());
                } else {
                    siloServer.reportObservation(cameraName, type, id);
                }
            }
            ReportResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}
