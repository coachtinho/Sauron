package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.ResponseMessage;

import java.lang.String;

public class SiloServer {

    public SiloServer() {
    }

    @Override
    public synchronized String toString() {
        return "hey";
    }

    public ResponseMessage play() {
        return ResponseMessage.CLEAR_FAIL;

    }
}
