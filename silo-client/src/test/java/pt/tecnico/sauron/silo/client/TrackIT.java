package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import io.grpc.StatusRuntimeException;
import static org.junit.Assert.*;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;

public class TrackIT extends BaseIT {

    private static SiloFrontend frontend;
    private static int port;
    private static String host;
    private static int offset;
    private static CameraRegistrationRequest camRequest1;
    private static CameraRegistrationRequest camRequest2;
    private static ReportRequest reportRequest1;
    private static ReportRequest reportRequest2; 

    @BeforeAll
    public static void oneTimeSetUp() throws StatusRuntimeException {
        host = testProps.getProperty("server.host");
        port = Integer.parseInt(testProps.getProperty("server.port"));
        offset = Integer.parseInt(testProps.getProperty("time.tolerance"));
        frontend = new SiloFrontend(host, port);
        
        // Setting up the server
        // Cameras
        camRequest1 = CameraRegistrationRequest.newBuilder().setName("test1").setLatitude(1).setLongitude(1).build();
        frontend.camJoin(camRequest1);
        camRequest2 = CameraRegistrationRequest.newBuilder().setName("test2").setLatitude(2).setLongitude(2).build();
        frontend.camJoin(camRequest2);

        // Observations
        ReportRequest.Builder reportRequest1Builder = ReportRequest.newBuilder();
        reportRequest1Builder.setCameraName(camRequest1.getName());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("person").setId("1").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("person").setId("2").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1111").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1112").build());
        reportRequest1 = reportRequest1Builder.build();
        frontend.report(reportRequest1);
        ReportRequest.Builder reportRequest2Builder = ReportRequest.newBuilder();
        reportRequest2Builder.setCameraName(camRequest2.getName());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("3").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("4").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1113").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1114").build());   
        reportRequest2 = reportRequest2Builder.build();
        frontend.report(reportRequest2);
    }

    @AfterAll
    public static void oneTimeTearDown() {
        frontend.ctrlClear(ClearRequest.newBuilder().build());
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void trackPersonTest() {
        String reportType = reportRequest1.getReports(0).getType();
        String reportId = reportRequest1.getReports(0).getId();
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId(reportId).build();
        TrackResponse response = frontend.track(request);
        assertEquals(response.getId(), reportId);
        assertEquals(response.getType(), reportType);
        long seconds = response.getTimestamp().getSeconds();
        long secondsNow = System.currentTimeMillis() / 1000l;
        assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        assertEquals(response.getName(), camRequest1.getName());
        assertEquals(response.getLongitude(), camRequest1.getLongitude(), 0.000001);
        assertEquals(response.getLongitude(), camRequest1.getLongitude(), 0.000001);
    }

    @Test
    public void trackCarTest() {
        String reportType = reportRequest1.getReports(2).getType();
        String reportId = reportRequest1.getReports(2).getId();
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId(reportId).build();
        TrackResponse response = frontend.track(request);
        assertEquals(response.getId(), reportId);
        assertEquals(response.getType(), reportType);
        long seconds = response.getTimestamp().getSeconds();
        long secondsNow = System.currentTimeMillis() / 1000l;
        assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        assertEquals(response.getName(), camRequest1.getName());
        assertEquals(response.getLongitude(), camRequest1.getLongitude(), 0.000001);
        assertEquals(response.getLongitude(), camRequest1.getLongitude(), 0.000001);
    }

}