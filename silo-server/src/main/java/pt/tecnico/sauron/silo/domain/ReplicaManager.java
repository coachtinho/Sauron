package pt.tecnico.sauron.silo.domain;

import java.util.List;
import java.util.Vector;

public class ReplicaManager {
    private final int _instance;
    private Vector<Integer> _TS;

    public ReplicaManager(int instance) {
        _instance = instance;
        _TS = new Vector<Integer>();
        for (int i = 0; i < instance; i++) {
            _TS.add(0);
        }
    }

    public Vector<Integer> generateOtherTS(List<Integer> tsList) {
        // Get client's valueTS from list
        Vector<Integer> otherTS = new Vector<Integer>(tsList.size());

        for (int ts : tsList)
            otherTS.add(ts);

        // Ensure both vectors have same size
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

    public Vector<Integer> getTS() {
        return _TS;
    }

}