package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.ResponseMessage;

import java.lang.String;

public class Person extends Observation {

    // ID _id
    
    public Person() {
    }

    @Override
    public synchronized String toString() {
        return "hey";
    }

    public ResponseMessage play() {
        return ResponseMessage.CLEAR_FAIL;

    }
}
