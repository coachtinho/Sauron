package pt.tecnico.sauron.silo.client;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.*;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationResponse;
import pt.tecnico.sauron.silo.grpc.Silo.CameraInfoRequest;
import pt.tecnico.sauron.silo.grpc.Silo.CameraInfoResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.InitRequest;
import pt.tecnico.sauron.silo.grpc.Silo.InitResponse;

public class CamIT extends BaseIT {
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
    public void camJoinOKTest() {
        CameraRegistrationRequest request = CameraRegistrationRequest.newBuilder()
                .setName("Camera2") //
                .setLatitude(123.45) //
                .setLongitude(678.91) //
                .build();
        CameraRegistrationResponse response = frontend.camJoin(request);
        assertNotEquals(response, null);
    }

    @Test
    public void camJoinNoNameTest() {
        CameraRegistrationRequest request = CameraRegistrationRequest.newBuilder()
                .setLatitude(123.45) //
                .setLongitude(678.91) //
                .build();
        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.camJoin(request));        
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());        
        String expectedMessage = "Name cannot be null!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void camJoinShortNameTest() {
        CameraRegistrationRequest request = CameraRegistrationRequest.newBuilder()
                .setName("ab") //
                .setLatitude(123.45) //
                .setLongitude(678.91) //
                .build();
        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.camJoin(request));        
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());        
        String expectedMessage = "Name length is illegal!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void camJoinLongNameTest() {
        CameraRegistrationRequest request = CameraRegistrationRequest.newBuilder()
                .setName("abcdefghijklmnop") //
                .setLatitude(123.45) //
                .setLongitude(678.91) //
                .build();
        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.camJoin(request));        
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());        
        String expectedMessage = "Name length is illegal!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void camJoinDuplicateTest() {
        String name = testProps.getProperty("camera.name");
        CameraRegistrationRequest request = CameraRegistrationRequest.newBuilder()
                .setName(name) //
                .setLatitude(123.45) //
                .setLongitude(678.91) //
                .build();
        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.camJoin(request));        
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());        
        String expectedMessage = "Camera already exists!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}