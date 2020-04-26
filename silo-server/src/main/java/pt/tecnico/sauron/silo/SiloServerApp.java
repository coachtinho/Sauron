package pt.tecnico.sauron.silo;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;

public class SiloServerApp {

    public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
        System.out.println(SiloServerApp.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // TODO: cleanup arg reading
        final String zooHost = args[0];
        final String zooPort = args[1];
        final int instance = Integer.parseInt(args[2]);
        final String serverHost = args[3];
        final Integer serverPort = Integer.parseInt(args[4]);

        final String path = "/grpc/sauron/silo/" + instance;
        final BindableService impl = new SiloServerImpl();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(serverPort).addService(impl).build();
        ZKNaming zkNaming = null;
        try {
            zkNaming = new ZKNaming(zooHost, zooPort);

            // publish
            zkNaming.rebind(path, serverHost, serverPort.toString());

            // start gRPC server
            server.start();

            // Server threads are running in the background.
            System.out.println("Server started");

            // Do not exit the main thread. Wait until server is terminated.
            server.awaitTermination();
        } finally {
            System.out.println("zoo unbinding");
            if (zkNaming != null) {
                // remove
                zkNaming.unbind(path, serverHost, serverPort.toString());
            }

        }
    }

}
