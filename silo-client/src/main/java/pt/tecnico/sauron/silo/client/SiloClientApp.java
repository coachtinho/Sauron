package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class SiloClientApp {

    public static void main(String[] args) throws ZKNamingException {
        System.out.println(SiloClientApp.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        String host = "localhost";
        Integer port = 8080;

        SiloFrontend frontend = new SiloFrontend(host, port.toString(), 1);

        try {
            CameraRegistrationRequest request = CameraRegistrationRequest.newBuilder().setLatitude(12.3)
                    .setLongitude(-123.23).build();
            frontend.camJoin(request);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        } finally {
            frontend.close();
        }

        System.out.println("bye!");

    }
}
