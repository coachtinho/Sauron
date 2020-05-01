package pt.tecnico.sauron.silo.domain;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import static io.grpc.Status.Code.UNAVAILABLE;

import pt.tecnico.sauron.silo.grpc.GossipGrpc;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class ReplicaFrontend {

    static final String BASE_PATH = "/grpc/sauron/silo";
    static final int TRIES = 4;
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
        final GossipRequest request = GossipRequest.newBuilder()//
                .addAllCameras(camJoinLog) // add camera requests
                .addAllReports(reportLog)// add reports
                .addAllTs(ts) // add server timestamp
                .build();

        try {
            Collection<ZKRecord> records = this.zkNaming.listRecords(BASE_PATH);
            ExecutorService pool = Executors.newCachedThreadPool();

            for (ZKRecord record : records) {
                if (record.getPath().matches(".*\\/" + _instance + "$")) {
                    System.out.println("Myself: " + record.getPath());
                    continue;
                }

                System.out.println("Gossiping to: " + record.getPath());
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
            // Create grpc stub
            GossipGrpc.GossipBlockingStub stub = GossipGrpc.newBlockingStub(channel);

            int numTries = 0;
            while (true) {
                try {
                    // Send gossip msg
                    stub.gossipData(_request);
                    return;
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() != UNAVAILABLE) {
                        System.out.println("Gossip with server " + _record.getURI() + " failed!");
                        throw e;
                    }
                }
                System.out.println("Failed to contact replica at " + _record.getURI());
                if (numTries++ >= TRIES) {
                    break;
                }
                // Waits before retrying
                exponentialBackoff(numTries);
            }

            System.out.println("Gossip with server " + _record.getURI() + " timed out!");
        }

        private void exponentialBackoff(int numTries) {
            int waitTime = BASE_WAIT + MULTIPLIER * numTries;
            System.out.println("Retrying in " + waitTime / 1000 + " seconds...");
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                // We don't expect this to happen frequently
                throw new RuntimeException(e);
            }
        }

    }
}
