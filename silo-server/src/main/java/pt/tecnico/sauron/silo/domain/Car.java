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
        if (id.length() == 6) {
            int nLetterGroups = 0; // number of letter groups in license
            int nNumberGroups = 0; // number of number groups in license

            for (int i = 0, j = 2; j <= 6; i+=2, j+=2) {
                String group = id.substring(i, j);

                if (group.matches("[A-Z][A-Z]")) {
                    nLetterGroups++;
                    continue;
                } else if (group.matches("[0-9][0-9]")) {
                    nNumberGroups++;
                    continue;
                } else {
                    return false;
                }
            }

            return nLetterGroups <= 2 && nNumberGroups <= 2;

        } else {
            return false;
        }
    }

    @Override
    public synchronized String toString() {
        return "Car," + _id + "," + super.toString();
    }

}
