package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import pt.tecnico.sauron.silo.grpc.Silo.*;

public class SiloFrontend {

    final ManagedChannel channel;

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

    public ClearResponse ctrlClear(ClearRequest request) {
        return stub.ctrlClear(request);
    }

    public InitResponse ctrlInit(InitRequest request) {
        // Loads server with one camera, person and car
        return stub.ctrlInit(request);
    }

    public CameraRegistrationResponse camJoin(CameraRegistrationRequest request) {
        return stub.camJoin(request);
    }

    public CameraInfoResponse camInfo(CameraInfoRequest request) {
        return stub.camInfo(request);
    }

    public ReportResponse report(ReportRequest request) {
        return stub.report(request);
    }

    public TrackResponse track(TrackRequest request) {
        return stub.track(request);
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest request) {
        return stub.trackMatch(request);
    }

    public TraceResponse trace(TraceRequest request) {
        return stub.trace(request);
    }

    public void close() {
        channel.shutdown();
    }
}