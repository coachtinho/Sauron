package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;


public class SiloServerImpl extends SauronGrpc.SauronImplBase {

    private SiloServer siloServer = new SiloServer();

    @Override
    public void ctrlPing(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        String input = request.getMessage();
        String output = "Hello " + input + "!";
        PingResponse response = PingResponse.newBuilder().setMessage(output).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
