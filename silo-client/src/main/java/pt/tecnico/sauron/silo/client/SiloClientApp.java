package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingResponse;

public class SiloClientApp {

    public static void main(String[] args) {
        System.out.println(SiloClientApp.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        String host = "localhost";
        int port = 8080;

        SiloFrontend frontend = new SiloFrontend(host, port);

        try {
            PingRequest request = PingRequest.newBuilder().setMessage("").build();
            PingResponse response = frontend.ctrlPing(request);
            System.out.println(response);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        } finally {
            frontend.close();
        }

        System.out.println("bye!");

    }

}
