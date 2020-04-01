package pt.tecnico.sauron.silo.domain;

public class Car extends Observation {

    String _id;

    public Car(String id, Camera cam) {
        super(cam);
        _id = id;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public String getType() {
        return "car";
    }

    public static boolean isValidId(String id) {
        if (id.length() != 6)
            return false;

        int nLetterGroups = 0; // number of letter groups in license
        int nNumberGroups = 0; // number of number groups in license
        String group = id.substring(0, 2);

        for (int i = 0, j = 2; j <= 6; i += 2, j += 2, group = id.substring(i, j))
            if (group.matches("[A-Z][A-Z]"))
                nLetterGroups++;
            else if (group.matches("[0-9][0-9]"))
                nNumberGroups++;
            else
                return false;

        return nLetterGroups <= 2 && nNumberGroups <= 2;
    }

    @Override
    public synchronized String toString() {
        return "Car," + _id + "," + super.toString();
    }

}
