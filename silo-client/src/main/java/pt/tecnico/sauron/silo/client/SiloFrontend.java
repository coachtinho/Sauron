package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class SiloFrontend {

    final ManagedChannel channel;

    SauronGrpc.SauronBlockingStub stub;

    public SiloFrontend(final String host, final String port, final int instance) throws ZKNamingException {
        System.out.println(host + ":" + port);
        ZKNaming zkNaming = new ZKNaming(host, port);
        final String path = "/grpc/sauron/silo/" + instance;

        ZKRecord record = zkNaming.lookup(path);
        String target = record.getURI();
        
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SauronGrpc.newBlockingStub(channel);
    }

    public PingResponse ctrlPing(final PingRequest request) {
        return stub.ctrlPing(request);
    }

    public ClearResponse ctrlClear(final ClearRequest request) {
        return stub.ctrlClear(request);
    }

    public InitResponse ctrlInit(final InitRequest request) {
        // Loads server with one camera, person and car
        return stub.ctrlInit(request);
    }

    public CameraRegistrationResponse camJoin(final CameraRegistrationRequest request) {
        return stub.camJoin(request);
    }

    public CameraInfoResponse camInfo(final CameraInfoRequest request) {
        return stub.camInfo(request);
    }

    public ReportResponse report(final ReportRequest request) {
        return stub.report(request);
    }

    public TrackResponse track(final TrackRequest request) {
        return stub.track(request);
    }

    public TrackMatchResponse trackMatch(final TrackMatchRequest request) {
        return stub.trackMatch(request);
    }

    public TraceResponse trace(final TraceRequest request) {
        return stub.trace(request);
    }

    public void close() {
        channel.shutdownNow();
    }
}