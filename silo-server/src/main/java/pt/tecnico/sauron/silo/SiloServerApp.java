package pt.tecnico.sauron.silo;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.Scanner;

import pt.tecnico.sauron.silo.domain.SiloServer;
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

        // Dependencies
        final SiloServer silo = new SiloServer();
        final ReplicaManager replicaManager = new ReplicaManager(instance, zooHost, zooPort, silo);

        // Bind grpc implementations
        final String path = "/grpc/sauron/silo/" + instance;
        final BindableService impl = new SiloServerImpl(replicaManager, silo);
        final BindableService impl_2 = replicaManager;

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(serverPort).addService(impl).addService(impl_2).build();
        ZKNaming zkNaming = null;
        try {
            zkNaming = new ZKNaming(zooHost, zooPort);

            // publish
            zkNaming.rebind(path, serverHost, serverPort.toString());

            // start gRPC server
            server.start();

            // Server threads are running in the background.
            System.out.println("Server started");

            // Create new thread where we wait for the user input
            new Thread(() -> {
                System.out.println("<Press enter to shutdown>");
                new Scanner(System.in).nextLine();

                server.shutdownNow();
            }).start();

            // Do not exit the main thread. Wait until server is terminated.
            server.awaitTermination();
            System.out.println("Server stopped");
        } finally {
            System.out.println("Unbinding zoo node");
            if (zkNaming != null) {
                // remove
                zkNaming.unbind(path, serverHost, serverPort.toString());
            }

        }
    }

}
