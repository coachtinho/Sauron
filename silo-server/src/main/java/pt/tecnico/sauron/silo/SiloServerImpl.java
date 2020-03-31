package pt.tecnico.sauron.silo;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.ALREADY_EXISTS;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationResponse;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ResponseMessage;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;

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
