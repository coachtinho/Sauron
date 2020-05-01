package pt.tecnico.sauron.silo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Timestamp;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.ReplicaFrontend;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.GossipGrpc.GossipImplBase;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.GossipRequest;
import pt.tecnico.sauron.silo.grpc.Silo.GossipResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;

public class ReplicaManager extends GossipImplBase {
    private final int GOSSIP_INTERVAL = 30;
    private final int _replicaCount;
    private final int _instance;
    private Vector<Integer> _TS;
    private final SiloServer _siloServer;
    // logs
    private Vector<ReportRequest> _reportLog;
    private Vector<CameraRegistrationRequest> _camJoinLog;
    // queues
    private Vector<CameraRegistrationRequest> _camJoinQueue;
    private Vector<ReportRequest> _reportQueue;
    // frontend
    ReplicaFrontend _frontend;

    public ReplicaManager(int replicaCount, int instance, String zooHost, String zooPort, SiloServer siloServer) {
        _replicaCount = replicaCount;
        _instance = instance;
        _siloServer = siloServer;
        _TS = new Vector<Integer>();
        for (int i = 0; i < replicaCount; i++) {
            _TS.add(0);
        }
        _reportLog = new Vector<>();
        _camJoinLog = new Vector<>();
        _camJoinQueue = new Vector<CameraRegistrationRequest>();
        _reportQueue = new Vector<ReportRequest>();

        _frontend = new ReplicaFrontend(zooHost, zooPort, instance);

        // Set gossip message timer
        Runnable helloRunnable = new Runnable() {
            public void run() {
                // System.out.println("Started...");
                _frontend.gossipData(_camJoinLog, _reportLog, new Vector<>(_TS));
                _camJoinLog.clear();
                _reportLog.clear();
                // System.out.println("Stopped...");
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, GOSSIP_INTERVAL, GOSSIP_INTERVAL, TimeUnit.SECONDS);

    }

    public Vector<Integer> generateOtherTS(List<Integer> tsList) {
        // get client's valueTS from list
        Vector<Integer> otherTS = new Vector<Integer>(tsList.size());

        for (int ts : tsList)
            otherTS.add(ts);

        synchronized (_TS) {
            // ensure both vectors have same size
            if (_TS.size() > otherTS.size())
                for (int i = otherTS.size(); i < _TS.size(); i++)
                    otherTS.add(0);
            else if (_TS.size() < otherTS.size())
                for (int i = _TS.size(); i < otherTS.size(); i++)
                    _TS.add(0);
        }

        return otherTS;
    }

    public boolean canUpdate(Vector<Integer> otherTS) {
        synchronized (_TS) {
            for (int i = 0; i < _TS.size(); i++)
                if (otherTS.get(i) < _TS.get(i))
                    return false;
        }
        return true;

    }

    public Vector<Integer> update() {
        synchronized (_TS) {
            // Increment timestamp
            _TS.setElementAt(_TS.get(_instance - 1) + 1, _instance - 1);
        }

        return _TS;
    }

    public void applyUpdate(List<Integer> newTS) {

        // Apply camera registration requests
        synchronized (_camJoinQueue) {
            for (int i = _camJoinQueue.size() - 1; i >= 0; i--) {

                CameraRegistrationRequest c = _camJoinQueue.get(i);
                _camJoinQueue.remove(c);
                try {
                    _siloServer.registerCamera(c.getName(), c.getLatitude(), c.getLongitude());
                } catch (SiloException e) {
                    System.out.println("Corrupted camera registration request!");
                } finally {
                }
            }
        }

        // Apply report requests
        synchronized (_reportQueue) {
            for (int i = _reportQueue.size() - 1; i >= 0; i--) {

                ReportRequest r = _reportQueue.get(i);
                _reportQueue.remove(r);
                List<ReportItem> items = r.getReportsList();
                LocalDateTime timestamp = timestampToLocalDateTime(r.getTimestamp());
                for (ReportItem item : items) {
                    String cameraName = r.getCameraName();
                    String type = item.getType();
                    String id = item.getId();
                    if (_siloServer.isValidType(type) && _siloServer.isValidId(type, id))
                        _siloServer.reportObservation(cameraName, type, id, timestamp);
                }
            }
        }

        // Update server TS
        synchronized (_TS) {
            for (int i = 0; i < _TS.size(); i++) {
                int myVal = _TS.get(i);
                int newVal = newTS.get(i);
                if (newVal > myVal)
                    _TS.set(i, newVal);
            }
        }

    }

    private LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()).atZone(ZoneId.of("Portugal"))
                .toLocalDateTime();
    }

    public void logReport(ReportRequest report, LocalDateTime timestamp) {
        // TODO: move this to private function
        Timestamp instant = Timestamp.newBuilder()
                .setSeconds(timestamp.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond()).build();

        ReportRequest request = ReportRequest.newBuilder() //
                .addAllReports(report.getReportsList()) //
                .setCameraName(report.getCameraName()) //
                .setTimestamp(instant) //
                .build();

        _reportLog.add(request);
    }

    public void logCamRegisterRequest(CameraRegistrationRequest cameraRegistrationRequest) {
        _camJoinLog.add(cameraRegistrationRequest);
    }

    public void queueReport(ReportRequest report) {
        _reportQueue.add(report);
    }

    public void queueCamRegisterRequest(CameraRegistrationRequest cameraRegistrationRequest) {
        _camJoinQueue.add(cameraRegistrationRequest);
    }

    @Override
    public void gossipData(final GossipRequest request, final StreamObserver<GossipResponse> responseObserver) {
        System.out.println("Caught gossip");

        // get new cameras and reports from request
        Collection<CameraRegistrationRequest> newCameras = request.getCamerasList();
        Collection<ReportRequest> newReports = request.getReportsList();

        // put them in queue
        _camJoinQueue.addAll(newCameras);
        _reportQueue.addAll(newReports);

        // apply queue
        this.applyUpdate(request.getTsList());

        GossipResponse response = GossipResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public Vector<Integer> getTS() {
        return _TS;
    }

}
