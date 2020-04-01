package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;

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
            frontend.camJoin("cam1", 234.432, 123.123);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        } finally {
            frontend.close();
        }

        System.out.println("bye!");

    }

}
