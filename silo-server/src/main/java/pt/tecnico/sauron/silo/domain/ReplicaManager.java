package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;

public class ReplicaManager {
    private final int _instance;
    private Vector<Integer> _TS;
    // logs
    private Vector<ReportRequest> _reportLog;
    private Vector<CameraRegistrationRequest> _cameraJoinLog;
    // queues
    private List<CameraRegistrationRequest> _camJoinQueue;
    private List<ReportRequest> _reportQueue;

    public ReplicaManager(int instance) {
        _instance = instance;
        _TS = new Vector<Integer>();
        for (int i = 0; i < instance; i++) {
            _TS.add(0);
        }
        _reportLog = new Vector<>();
        _cameraJoinLog = new Vector<>();
        _camJoinQueue = new ArrayList<CameraRegistrationRequest>();
        _reportQueue = new ArrayList<ReportRequest>();
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

    public void applyUpdate(SiloServer silo) {
        applyCamJoinUpdates(silo);
        applyReportUpdates(silo);
    }

    private void applyCamJoinUpdates(SiloServer silo) {
        // apply camJoin updates when possible
        boolean camUpdates = false;
        do {
            camUpdates = false;
            for (CameraRegistrationRequest c : _camJoinQueue) {
                Vector<Integer> otherTS = generateOtherTS(c.getTsList());

                if (canUpdate(otherTS)) {
                    camUpdates = true;
                    try {
                        silo.registerCamera(c.getName(), c.getLatitude(), c.getLongitude());
                    } catch (SiloException e) {
                        // TODO: throw something
                    } finally {
                        update(otherTS);
                    }
                }
            }
        } while (camUpdates);
    }

    private void applyReportUpdates(SiloServer silo) {
        // register reports when possible
        boolean reportUpdates;

        do {
            reportUpdates = false;
            for (ReportRequest r : _reportQueue) {
                Vector<Integer> otherTS = generateOtherTS(r.getTsList());

                if (canUpdate(otherTS)) {
                    reportUpdates = true;
                    List<ReportItem> items = r.getReportsList();

                    for (ReportItem item : items) {
                        String cameraName = r.getCameraName();
                        String type = item.getType();
                        String id = item.getId();
                        if (silo.isValidType(type) && silo.isValidId(type, id)) {
                            silo.reportObservation(cameraName, type, id);
                        }
                    }
                    update(otherTS);
                }
            }
        } while (reportUpdates);

    }

    public void logReport(ReportRequest report) {
        _reportLog.add(report);
    }

    public void logCamRegisterRequest(CameraRegistrationRequest cameraRegistrationRequest) {
        _cameraJoinLog.add(cameraRegistrationRequest);
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

}