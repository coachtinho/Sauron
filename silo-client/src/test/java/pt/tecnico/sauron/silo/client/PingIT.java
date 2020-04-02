package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.Silo.PingRequest;
import pt.tecnico.sauron.silo.grpc.Silo.PingResponse;

public class PingIT extends BaseIT {

    // static members
    private static final String host = testProps.getProperty("server.host");
    private static final int port = Integer.parseInt(testProps.getProperty("server.port"));
    private static SiloFrontend frontend;

    // one-time initialization and clean-up
    @BeforeAll
    public static void oneTimeSetUp() {
        frontend = new SiloFrontend(host, port);
    }

    @AfterAll
    public static void oneTimeTearDown() {
        frontend.close();
    }

    // initialization and clean-up for each test

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {

    }

    // tests

    @Test
    public void pingOKTest() {
        PingRequest request = PingRequest.newBuilder().setMessage("friend").build();
        PingResponse response = frontend.ctrlPing(request);
        assertEquals("Hello friend!", response.getMessage());
    }

    @Test
    public void emptyPingTest() {
        PingRequest request = PingRequest.newBuilder().setMessage("").build();

        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.ctrlPing(request));        
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());        
        String expectedMessage = "Message cannot be empty!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

}
