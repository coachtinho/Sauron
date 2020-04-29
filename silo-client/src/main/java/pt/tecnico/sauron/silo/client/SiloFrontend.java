package pt.tecnico.sauron.silo.client;

import java.util.Collection;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloFrontend {

    static final String BASE_PATH = "/grpc/sauron/silo";

    ManagedChannel channel;
    SauronGrpc.SauronBlockingStub stub;
    ZKNaming zkNaming;

    public SiloFrontend(String host, String port, String instance) throws SiloFrontendException {
        this.zkNaming = new ZKNaming(host, port);
        connectReplica(instance);
    }

    public void connectReplica(String instance) throws SiloFrontendException {
        try {
            ZKRecord record;
            String target;

            /* If instance is not specified, connects to random replica */
            if (instance == null) {
                Collection<ZKRecord> records = this.zkNaming.listRecords(BASE_PATH);
                record = getRandom(records);
                if (record == null) {
                    throw new SiloFrontendException("No available Replica!");
                }
                target = record.getURI();
            }

            /* If instance is specified, connects to specified replica */
            else {
                record = this.zkNaming.lookup(BASE_PATH + "/" + instance);
                target = record.getURI();
            }

            /* If swapping to another replica, close the old channel */
            if (this.channel != null) {
                this.close();
            }
            this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.stub = SauronGrpc.newBlockingStub(channel);
        } catch (ZKNamingException e) {
            throw new SiloFrontendException("Error connecting to replica: " + e.getMessage() + ": " + e.getCause().getMessage(), e);
        } catch (SiloFrontendException e) {
            throw new SiloFrontendException("Error connecting to replica: " + e.getMessage(), e);
        }
    }

    public ZKRecord getRandom(Collection<ZKRecord> records) {
        int index = (int) (Math.random() * records.size());
        for (ZKRecord record : records) {
            if (--index < 0) {
                return record;
            }
        }
        return null;
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
        channel.shutdownNow();
    }
}