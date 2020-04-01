package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
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

    public void camJoin(String name, double longitude, double latitude) {
        CameraRegistrationRequest request = CameraRegistrationRequest.newBuilder().setName(name).setLongitude(longitude)
                .setLatitude(latitude).build();
        try {
            CameraRegistrationResponse response = stub.camJoin(request);
            System.out.println("Camera sucessfully registered!");
        } catch (StatusRuntimeException e) {
            System.out.println("Camera wasn't registered!");
            System.out.println(e.getMessage());
        }
    }

    public double[] camInfo(String name) {
        CameraInfoRequest request = CameraInfoRequest.newBuilder().setName(name).build();
        double latitude = 0;
        double longitude = 0;
        try {
            CameraInfoResponse response = stub.camInfo(request);
            latitude = Double.parseDouble(response.getLatitude());
            longitude = Double.parseDouble(response.getLongitude());

        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
        return new double[] { longitude, latitude };
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