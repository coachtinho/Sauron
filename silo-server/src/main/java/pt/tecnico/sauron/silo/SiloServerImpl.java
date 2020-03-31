package pt.tecnico.sauron.silo;

import static io.grpc.Status.INVALID_ARGUMENT;

import pt.tecnico.sauron.silo.domain.Observation;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingResponse;
import pt.tecnico.sauron.silo.grpc.Silo.SearchRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationMessage;
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
    public void track(final SearchRequest request, final StreamObserver<ObservationMessage> responseObserver) {
        final String type = request.getType();
        final String id = request.getId();
        Observation obs;

        if (type.equals("person")) {
            obs = siloServer.trackPerson(id);
        } else if (type.equals("car")) {
            obs = siloServer.trackCar(id);
        }

        //final ObservationMessage response = ObservationMessage.newBuilder()
        //    .setId(id).setType(type).setTimestamp(obs.getTimestamp());
    }

}
