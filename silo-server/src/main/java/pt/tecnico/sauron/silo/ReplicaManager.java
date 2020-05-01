package pt.tecnico.sauron.silo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.GossipRequest;
import pt.tecnico.sauron.silo.grpc.Silo.GossipResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;
import pt.tecnico.sauron.silo.domain.ReplicaFrontend;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.GossipGrpc.GossipImplBase;

public class ReplicaManager extends GossipImplBase {
    private final int _instance;
    private Vector<Integer> _TS;
    private final SiloServer _siloServer;
    // logs
    private Vector<ReportRequest> _reportLog;
    private Vector<CameraRegistrationRequest> _camJoinLog;
    // queues
    private List<CameraRegistrationRequest> _camJoinQueue;
    private List<ReportRequest> _reportQueue;
    // frontend
    ReplicaFrontend frontend;

    public ReplicaManager(int instance, String zooHost, String zooPort, SiloServer siloServer) {
        _instance = instance;
        _siloServer = siloServer;
        _TS = new Vector<Integer>();
        for (int i = 0; i < instance; i++) {
            _TS.add(0);
        }
        _reportLog = new Vector<>();
        _camJoinLog = new Vector<>();
        _camJoinQueue = new ArrayList<CameraRegistrationRequest>();
        _reportQueue = new ArrayList<ReportRequest>();

        frontend = new ReplicaFrontend(zooHost, zooPort);
    }

    public Vector<Integer> generateOtherTS(List<Integer> tsList) {
        // get client's valueTS from list
        Vector<Integer> otherTS = new Vector<Integer>(tsList.size());

        for (int ts : tsList)
            otherTS.add(ts);

        // ensure both vectors have same size
        if (_TS.size() > otherTS.size())
            for (int i = otherTS.size(); i < _TS.size(); i++)
                otherTS.add(0);
        else if (_TS.size() < otherTS.size())
            for (int i = _TS.size(); i < otherTS.size(); i++)
                _TS.add(0);

        return otherTS;
    }

    public boolean canUpdate(Vector<Integer> otherTS) {
        for (int i = 0; i < _TS.size(); i++)
            if (otherTS.get(i) < _TS.get(i))
                return false;

        return true;
    }

    public Vector<Integer> update(Vector<Integer> otherTS) {
        // Merges ts and increments replica's own value
        Vector<Integer> newTS = new Vector<Integer>();

        for (int i = 0; i < _TS.size(); i++)
            newTS.add(Math.max(_TS.get(i), otherTS.get(i)));

        int ts = newTS.get(_instance - 1) + 1;
        newTS.setElementAt(ts, _instance - 1);
        _TS = newTS;
        return _TS;
    }

    public void applyUpdate() {
        boolean updates;

        do {
            updates = false;

            for (CameraRegistrationRequest c : _camJoinQueue) {
                Vector<Integer> otherTS = generateOtherTS(c.getTsList());

                if (canUpdate(otherTS)) {
                    updates = true;
                    _camJoinQueue.remove(c);
                    try {
                        _siloServer.registerCamera(c.getName(), c.getLatitude(), c.getLongitude());
                    } catch (SiloException e) {
                        // TODO: throw something
                    } finally {
                        update(otherTS);
                    }
                }
            }

            for (ReportRequest r : _reportQueue) {
                Vector<Integer> otherTS = generateOtherTS(r.getTsList());

                if (canUpdate(otherTS)) {
                    updates = true;
                    _reportQueue.remove(r);
                    List<ReportItem> items = r.getReportsList();

                    for (ReportItem item : items) {
                        String cameraName = r.getCameraName();
                        String type = item.getType();
                        String id = item.getId();
                        if (_siloServer.isValidType(type) && _siloServer.isValidId(type, id)) {
                            _siloServer.reportObservation(cameraName, type, id);
                        }
                    }
                    update(otherTS);
                }
            }
        } while (updates);
    }

    public void logReport(ReportRequest report) {
        _reportLog.add(report);
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

    public Vector<Integer> getTS() {
        return _TS;
    }

    @Override
    public void gossipData(final GossipRequest request, final StreamObserver<GossipResponse> responseObserver) {
        // get new cameras and reports from request
        Collection<CameraRegistrationRequest> newCameras = request.getCamerasList();
        Collection<ReportRequest> newReports = request.getReportsList();

        // put them in queue
        _camJoinQueue.addAll(newCameras);
        _reportQueue.addAll(newReports);

        // apply queue
        this.applyUpdate();

        GossipResponse response = GossipResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
