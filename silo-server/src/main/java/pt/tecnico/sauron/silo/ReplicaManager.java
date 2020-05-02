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
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.ReplicaFrontend;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.GossipGrpc.GossipImplBase;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.GossipRequest;
import pt.tecnico.sauron.silo.grpc.Silo.GossipResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationType;

public class ReplicaManager extends GossipImplBase {
    private final int GOSSIP_INTERVAL = 30;
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
    // thread pool for gossip messages
    ScheduledExecutorService _executor;

    public ReplicaManager(int replicaCount, int instance, String zooHost, String zooPort, SiloServer siloServer) {
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
        Runnable gossipRunnable = new Runnable() {
            public void run() {
                // send gossip only when there's something to say
                if (!_camJoinLog.isEmpty() || !_reportLog.isEmpty()) {
                    _frontend.sendGossip(_camJoinLog, _reportLog, new Vector<>(_TS));
                    _camJoinLog.clear();
                    _reportLog.clear();
                }
            }
        };

        _executor = Executors.newScheduledThreadPool(1);
        _executor.scheduleAtFixedRate(gossipRunnable, GOSSIP_INTERVAL, GOSSIP_INTERVAL, TimeUnit.SECONDS);

    }

    public void stopGossip() {
        _executor.shutdownNow();
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
                    ObservationType type = item.getType();
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

    private Timestamp localDateTimeToTimestamp(LocalDateTime date) {
        return Timestamp.newBuilder().setSeconds(date.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond())
                .build();
    }

    public void logReport(ReportRequest report, LocalDateTime date) {

        ReportRequest request = ReportRequest.newBuilder() //
                .addAllReports(report.getReportsList()) // add item list (i.e. people, cars)
                .setCameraName(report.getCameraName()) // add camera name
                .setTimestamp(localDateTimeToTimestamp(date)) // add time of report
                .build();

        _reportLog.add(request);
    }

    public void logCamRegisterRequest(CameraRegistrationRequest cameraRegistrationRequest) {
        _camJoinLog.add(cameraRegistrationRequest);
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
