package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;

public class EyeIT extends BaseIT {
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
}