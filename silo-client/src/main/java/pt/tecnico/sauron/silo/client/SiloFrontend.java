package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.*;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;


import static pt.tecnico.sauron.silo.grpc.SauronGrpc.newBlockingStub;


public class SiloFrontend {


    // Channel is the abstraction to connect to a service endpoint.
    // Let us use plaintext communication because we do not have certificates.
    final ManagedChannel channel;

    // It is up to the client to determine whether to block the call.
    // Here we create a blocking stub, but an async stub, or an async stub with
    // Future are also available.
    SauronGrpc.SauronBlockingStub stub;


    public SiloFrontend(String host, int port) {
        System.out.println(host + ":" + port);
        String target = host + ":" + port;

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SauronGrpc.newBlockingStub(channel);
    }

    public PingResponse ctrlPing(PingRequest request) {
        return stub.ctrlPing(request);

    }
}