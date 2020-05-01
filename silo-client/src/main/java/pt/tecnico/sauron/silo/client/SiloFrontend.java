package pt.tecnico.sauron.silo.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.Code.UNAVAILABLE;

import pt.tecnico.sauron.silo.grpc.SauronGrpc;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloFrontend {

    static final String BASE_PATH = "/grpc/sauron/silo";
    static final int TRIES = 3;
    static final int BASE_WAIT = 2000;
    static final int MULTIPLIER = 2000;
    static final long CACHE_SIZE = 50; // number of request response pairs each cache can hold

    ManagedChannel channel;
    SauronGrpc.SauronBlockingStub stub;
    ZKNaming zkNaming;
    String target; //Replica URI
    String instance; //Replica to contact

    //Caches
    HashMap<CameraInfoRequest, CameraInfoResponse> cameraInfoCache = new HashMap<CameraInfoRequest, CameraInfoResponse>();
    HashMap<TrackRequest, TrackResponse> trackCache = new HashMap<TrackRequest, TrackResponse>();
    HashMap<TrackMatchRequest, TrackMatchResponse> trackMatchCache = new HashMap<TrackMatchRequest, TrackMatchResponse>();
    HashMap<TraceRequest, TraceResponse> traceCache = new HashMap<TraceRequest, TraceResponse>();

    public SiloFrontend(String host, String port, String instance) throws SiloFrontendException {
        this.zkNaming = new ZKNaming(host, port);
        this.instance = instance;

        // If instance is not specified, connects to random replica
        if (this.instance == null) {
            connectToRandomReplica();
        }

        // If instance is specified, connects to specified replica
        else {
            try {
                ZKRecord record = this.zkNaming.lookup(BASE_PATH + "/" + instance);
                target = record.getURI();
                this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                this.stub = SauronGrpc.newBlockingStub(channel);
            } catch (ZKNamingException e) {
                throw new SiloFrontendException(
                        "Error connecting to specified replica:" + e.getMessage() + ":" + e.getCause().getMessage(), e);
            }
        }
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

    public CameraRegistrationResponse camJoin(CameraRegistrationRequest request) throws SiloFrontendException {
        int numTries = 0;
        while (true) {
            try {
                return stub.camJoin(request);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != UNAVAILABLE) {
                    // Let client handle the exception
                    throw e;
                }
            }
            System.out.println("Failed to contact replica at " + this.target);
            if (numTries >= TRIES - 1) {
                break;
            }
            // Waits before retrying
            exponentialBackoff(numTries);
            numTries++;
        }
        // Tries to change to another replica
        if (this.instance != null) {
            throw new SiloFrontendException("Error: Failed to contact the desired replica the maximum amount of times");
        }
        System.out.println("Trying to contact another replica to tolerate fault...");
        connectToRandomReplica();

        // Retries operation
        System.out.println("Retrying operation...");
        return camJoin(request);
    }

    public CameraInfoResponse camInfo(CameraInfoRequest request) throws SiloFrontendException {
        int numTries = 0;
        while (true) {
            try {
                CameraInfoResponse response = stub.camInfo(request);
                System.out.println(response.getTsList());

                // If we have this request cached
                if (this.trackCache.containsKey(request)) {

                    // If response timestamp is greater or equal, replaces the cached response
                    if (greaterThan(response.getTsList(), this.trackCache.get(request).getTsList())) {
                        System.out.println("novo!!");
                        this.cameraInfoCache.put(request, response);
                        return response;
                    }

                    // Else, returns cached response
                    else {
                        System.out.println("velho!!");
                        return this.cameraInfoCache.get(request);
                    }
                } 
                
                // Else, Caches response
                else {
                    System.out.println("não tou ca dentro");
                    this.cameraInfoCache.put(request, response);
                    return response;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != UNAVAILABLE) {
                    // Let client handle the exception
                    throw e;
                }
            }
            System.out.println("Failed to contact replica at " + this.target);
            if (numTries >= TRIES - 1) {
                break;
            }
            // Waits before retrying
            exponentialBackoff(numTries);
            numTries++;
        }
        // Tries to change to another replica
        if (this.instance != null) {
            throw new SiloFrontendException("Error: Failed to contact the desired replica the maximum amount of times");
        }
        System.out.println("Trying to contact another replica to tolerate fault...");
        connectToRandomReplica();

        // Retries operation
        System.out.println("Retrying operation...");
        return camInfo(request);
    }

    public ReportResponse report(ReportRequest request) throws SiloFrontendException {
        int numTries = 0;
        while (true) {
            try {
                return stub.report(request);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != UNAVAILABLE) {
                    // Let client handle the exception
                    throw e;
                }
            }
            System.out.println("Failed to contact replica at " + this.target);
            if (numTries >= TRIES - 1) {
                break;
            }
            // Waits before retrying
            exponentialBackoff(numTries);
            numTries++;
        }
        // Tries to change to another replica
        if (this.instance != null) {
            throw new SiloFrontendException("Error: Failed to contact the desired replica the maximum amount of times");
        }
        System.out.println("Trying to contact another replica to tolerate fault...");
        connectToRandomReplica();

        // Retries operation
        System.out.println("Retrying operation...");
        return report(request);
    }

    public TrackResponse track(TrackRequest request) throws SiloFrontendException {
        int numTries = 0;
        while (true) {
            try {
                TrackResponse response = stub.track(request);
                System.out.println(response.getTsList());

                // If we have this request cached
                if (this.trackCache.containsKey(request)) {

                    // If response timestamp is greater or equal, replaces the cached response
                    if (greaterThan(response.getTsList(), this.trackCache.get(request).getTsList())) {
                        System.out.println("novo!!");
                        this.trackCache.put(request, response);
                        return response;
                    }

                    // Else, returns cached response
                    else {
                        System.out.println("velho!!");
                        return this.trackCache.get(request);
                    }
                } 
                
                // Else, Caches response
                else {
                    System.out.println("não tou ca dentro");
                    this.trackCache.put(request, response);
                    return response;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != UNAVAILABLE) {
                    // Let client handle the exception
                    throw e;
                }
            }
            System.out.println("Failed to contact replica at " + this.target);
            if (numTries >= TRIES - 1) {
                break;
            }
            // Waits before retrying
            exponentialBackoff(numTries);
            numTries++;
        }
        // Tries to change to another replica
        if (this.instance != null) {
            throw new SiloFrontendException("Error: Failed to contact the desired replica the maximum amount of times");
        }
        System.out.println("Trying to contact another replica to tolerate fault...");
        connectToRandomReplica();

        // Retries operation
        System.out.println("Retrying operation...");
        return track(request);
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest request) throws SiloFrontendException {
        int numTries = 0;
        while (true) {
            try {
                TrackMatchResponse response = stub.trackMatch(request);
                System.out.println(response.getTsList());

                // If we have this request cached
                if (this.trackMatchCache.containsKey(request)) {

                    // If response timestamp is greater or equal, replaces the cached response
                    if (greaterThan(response.getTsList(), this.trackMatchCache.get(request).getTsList())) {
                        System.out.println("novo!!");
                        this.trackMatchCache.put(request, response);
                        return response;
                    }

                    // Else, returns cached response
                    else {
                        System.out.println("velho!!");
                        return this.trackMatchCache.get(request);
                    }
                } 
                
                // Else, Caches response
                else {
                    System.out.println("não tou ca dentro");
                    this.trackMatchCache.put(request, response);
                    return response;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != UNAVAILABLE) {
                    // Let client handle the exception
                    throw e;
                }
            }
            System.out.println("Failed to contact replica at " + this.target);
            if (numTries >= TRIES - 1) {
                break;
            }
            // Waits before retrying
            exponentialBackoff(numTries);
            numTries++;
        }
        // Tries to change to another replica
        if (this.instance != null) {
            throw new SiloFrontendException("Error: Failed to contact the desired replica the maximum amount of times");
        }
        System.out.println("Trying to contact another replica to tolerate fault...");
        connectToRandomReplica();

        // Retries operation
        System.out.println("Retrying operation...");
        return trackMatch(request);
    }

    public TraceResponse trace(TraceRequest request) throws SiloFrontendException {
        int numTries = 0;
        while (true) {
            try {
                TraceResponse response = stub.trace(request);
                System.out.println(response.getTsList());

                // If we have this request cached
                if (this.traceCache.containsKey(request)) {

                    // If response timestamp is greater or equal, replaces the cached response
                    if (greaterThan(response.getTsList(), this.traceCache.get(request).getTsList())) {
                        System.out.println("novo!!");
                        this.traceCache.put(request, response);
                        return response;
                    }

                    // Else, returns cached response
                    else {
                        System.out.println("velho!!");
                        return this.traceCache.get(request);
                    }
                } 
                
                // Else, Caches response
                else {
                    System.out.println("não tou ca dentro");
                    this.traceCache.put(request, response);
                    return response;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != UNAVAILABLE) {
                    // Let client handle the exception
                    throw e;
                }
            }
            System.out.println("Failed to contact replica at " + this.target);
            if (numTries >= TRIES - 1) {
                break;
            }
            // Waits before retrying
            exponentialBackoff(numTries);
            numTries++;
        }
        // Tries to change to another replica
        if (this.instance != null) {
            throw new SiloFrontendException("Error: Failed to contact the desired replica the maximum amount of times");
        }
        System.out.println("Trying to contact another replica to tolerate fault...");
        connectToRandomReplica();

        // Retries operation
        System.out.println("Retrying operation...");
        return trace(request);
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

    private void connectToRandomReplica() throws SiloFrontendException {
        try {
            Collection<ZKRecord> records = this.zkNaming.listRecords(BASE_PATH);
            ZKRecord record;

            // Connects to a random available replica
            while (records.size() > 0) {
                record = getRandom(records);
                this.target = record.getURI();
                // Close the old channel
                this.close();
                this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                this.stub = SauronGrpc.newBlockingStub(channel);
                // We test to see if the replica is alive before commiting
                try {
                    System.out.println("Trying to contact replica at " + this.target + "...");
                    PingRequest request = PingRequest.newBuilder().setMessage("Are you alive?").build();
                    stub.ctrlPing(request);
                    System.out.println("Found new replica at " + this.target);
                    return;
                } catch (StatusRuntimeException e) {
                    // We consider any error here to be reason enough to try another replica
                    System.out.println("Failed to contact replica");
                    records.remove(record);
                }
            }
            throw new SiloFrontendException(
                    "Error connecting to random replica:Tried all known replicas without success");
        } catch (ZKNamingException e) {
            throw new SiloFrontendException(
                    "Error connecting to random replica:" + e.getMessage() + ": " + e.getCause().getMessage(), e);
        }
    }

    private ZKRecord getRandom(Collection<ZKRecord> records) {
        int index = (int) (Math.random() * records.size());
        for (ZKRecord record : records) {
            if (--index < 0) {
                return record;
            }
        }
        return null;
    }

    private boolean greaterThan(List<Integer> valueTS, List<Integer> prevTS) {
        for (int i = 0; i < valueTS.size(); i++) {
            if (valueTS.get(i).intValue() < prevTS.get(i).intValue()) {
                return false;
            }
        }
        return true;
    }

    public void close() {
        if (this.channel != null) {
            channel.shutdownNow();
        }
    }

}