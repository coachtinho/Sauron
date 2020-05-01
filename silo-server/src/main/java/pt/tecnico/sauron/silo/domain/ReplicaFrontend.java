package pt.tecnico.sauron.silo.domain;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import static io.grpc.Status.Code.UNAVAILABLE;

import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.grpc.GossipGrpc;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import pt.tecnico.sauron.silo.grpc.GossipGrpc.GossipFutureStub;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class ReplicaFrontend {

    static final String BASE_PATH = "/grpc/sauron/silo";
    static final int TRIES = 3;
    static final int BASE_WAIT = 2000;
    static final int MULTIPLIER = 2000;

    private final int _instance;
    ZKNaming zkNaming;

    public ReplicaFrontend(String host, String port, int instance) throws SiloException {
        this.zkNaming = new ZKNaming(host, port);
        _instance = instance;
    }

    public void gossipData(Vector<CameraRegistrationRequest> camJoinLog, Vector<ReportRequest> reportLog,
            Vector<Integer> ts) {

        // Create gossip message
        final GossipRequest.Builder requestBuilder = GossipRequest.newBuilder();

        // Update timestamp on camera join requests
        for (CameraRegistrationRequest cam : camJoinLog) {
            CameraRegistrationRequest camRequest = CameraRegistrationRequest.newBuilder() //
                    .setName(cam.getName()) //
                    .setLatitude(cam.getLatitude()) //
                    .setLongitude(cam.getLongitude()) //
                    .addAllTs(ts).build();
            requestBuilder.addCameras(camRequest);
        }

        // Update timestamp on report requests
        for (ReportRequest report : reportLog) {
            ReportRequest reportRequest = ReportRequest.newBuilder() //
                    .setCameraName(report.getCameraName()) //
                    .addAllReports(report.getReportsList()) //
                    .addAllTs(ts)//
                    .build();
            requestBuilder.addReports(reportRequest);
        }

        GossipRequest request = requestBuilder.build();

        try {
            Collection<ZKRecord> records = this.zkNaming.listRecords(BASE_PATH);
            ExecutorService pool = Executors.newCachedThreadPool();

            for (ZKRecord record : records) {
                if (record.getPath().matches(".*\\/" + _instance + "$")) {
                    System.out.println("Caught myself: " + record.getPath());
                    continue;
                }

                pool.execute(new SendGossipTask(record, request));
            }
        } catch (ZKNamingException e) {
            System.out.println("An error ocurred with zookeeper");
        }
    }

    class SendGossipTask implements Runnable {
        private ZKRecord _record;
        GossipRequest _request;

        public SendGossipTask(ZKRecord record, GossipRequest request) {
            _record = record;
            _request = request;
        }

        // Connects to replica specified in 'record',
        // sends gossip message and then closes the channel
        @Override
        public void run() {
            // Connects to server in 'record' if available
            String target = _record.getURI();
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

            try {
                // Create grpc stub
                GossipGrpc.GossipBlockingStub stub = GossipGrpc.newBlockingStub(channel);
                // Send gossip msg
                GossipResponse response = stub.gossipData(_request); // TODO: either use reponse or dont allocate it
            } catch (Exception e) {
                System.out.println("Gossip with server " + _record.getURI() + " failed!");
            } finally {
                // Close channel
                channel.shutdownNow();
            }
        }
    }
}
