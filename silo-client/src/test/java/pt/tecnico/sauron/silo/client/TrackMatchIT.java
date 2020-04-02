package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.*;
import static org.junit.Assert.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

public class TrackMatchIT extends BaseIT {
    private static SiloFrontend frontend;
    private static int port;
    private static String host;

    @BeforeAll
    public static void oneTimeSetUp() {
    }

    @AfterAll
    public static void oneTimeTearDown() {
    }

}