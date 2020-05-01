package pt.tecnico.sauron.silo.domain;

import java.util.Collection;
import java.util.List;
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

    ZKNaming zkNaming;

    public ReplicaFrontend(String host, String port) throws SiloException {
        this.zkNaming = new ZKNaming(host, port);
    }

    public void gossipData(GossipRequest request) {
        try {

            Collection<ZKRecord> records = this.zkNaming.listRecords(BASE_PATH);
            ExecutorService pool = Executors.newCachedThreadPool();
            for (ZKRecord record : records)
                pool.execute(new SendGossipTask(record, request));
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
