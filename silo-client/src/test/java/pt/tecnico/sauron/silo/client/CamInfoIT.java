package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.*;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.Silo.CameraInfoRequest;
import pt.tecnico.sauron.silo.grpc.Silo.CameraInfoResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.InitRequest;

public class CamInfoIT extends BaseIT {
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
        InitRequest request = InitRequest.getDefaultInstance();
        frontend.ctrlInit(request);
    }

    @AfterEach
    public void tearDown() {
        ClearRequest request = ClearRequest.getDefaultInstance();
        frontend.ctrlClear(request);
    }
	
    // tests

    @Test
    public void camInfoOKTest() {
        String name = testProps.getProperty("camera.name");
        double lat = Double.parseDouble(testProps.getProperty("camera.latitude"));
        double longi = Double.parseDouble(testProps.getProperty("camera.longitude"));
        CameraInfoRequest request = CameraInfoRequest.newBuilder()
                .setName(name) //
                .build();
        CameraInfoResponse response = frontend.camInfo(request);
        assertEquals(response.getLatitude(), lat, 0);
        assertEquals(response.getLongitude(), longi, 0);
    }

    @Test
    public void camInfoNoNameTest() {
        CameraInfoRequest request = CameraInfoRequest.newBuilder().build();
        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.camInfo(request));        
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());        
        String expectedMessage = "Name cannot be empty!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void camInfoNotFoundTest() {
        CameraInfoRequest request = CameraInfoRequest.newBuilder()
                .setName("Camera2") //
                .build();
        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.camInfo(request));        
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());        
        String expectedMessage = "No such camera!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}