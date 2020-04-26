package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.*;
import static org.junit.Assert.*;

import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;


public class TrackIT extends BaseIT {

    private static SiloFrontend frontend;
    private static String port;
    private static String host;
    private static int instance;
    private static int offset;
    private static CameraRegistrationRequest camRequest1;
    private static CameraRegistrationRequest camRequest2;
    private static ReportRequest reportRequest1;
    private static ReportRequest reportRequest2; 

    @BeforeAll
    public static void oneTimeSetUp() throws StatusRuntimeException, InterruptedException, ZKNamingException {
        host = testProps.getProperty("zoo.host");
        port = testProps.getProperty("zoo.port");
        instance = 1;
        offset = Integer.parseInt(testProps.getProperty("time.tolerance"));
        frontend = new SiloFrontend(host, port, instance);
        
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
        Thread.sleep(1000);
        ReportRequest.Builder reportRequest2Builder = ReportRequest.newBuilder();
        reportRequest2Builder.setCameraName(camRequest2.getName());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("1").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("3").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("4").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1111").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1113").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1114").build());   
        reportRequest2 = reportRequest2Builder.build();
        frontend.report(reportRequest2);
    }

    @AfterAll
    public static void oneTimeTearDown() {
        frontend.ctrlClear(ClearRequest.newBuilder().build());
        frontend.close();
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void trackPersonTest() {
        String reportType = "person";
        String reportId = "1";
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId(reportId).build();
        TrackResponse response = frontend.track(request);
        assertEquals(reportId, response.getId());
        assertEquals(reportType, response.getType());
        long seconds = response.getTimestamp().getSeconds();
        long secondsNow = System.currentTimeMillis() / 1000l;
        assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        assertEquals(camRequest2.getName(), response.getName());
        assertEquals(camRequest2.getLongitude(), response.getLongitude(), 0.000001);
        assertEquals(camRequest2.getLatitude(), response.getLatitude(), 0.000001);
    }

    @Test
    public void trackCarTest() {
        String reportType = "car";
        String reportId = "AA1111";
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId(reportId).build();
        TrackResponse response = frontend.track(request);
        assertEquals(reportId, response.getId());
        assertEquals(reportType, response.getType());
        long seconds = response.getTimestamp().getSeconds();
        long secondsNow = System.currentTimeMillis() / 1000l;
        assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        assertEquals(camRequest2.getName(), response.getName());
        assertEquals(camRequest2.getLongitude(), response.getLongitude(), 0.000001);
        assertEquals(camRequest2.getLongitude(), response.getLatitude(), 0.000001);
    }

    @Test
    public void trackEmptyTypeTest() {
        String reportId = reportRequest1.getReports(0).getId();
        TrackRequest request = TrackRequest.newBuilder().setId(reportId).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.track(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Type cannot be empty!", exception.getMessage());
    }

    @Test
    public void trackEmptyIdTest() {
        String reportType = reportRequest1.getReports(0).getType();
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.track(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Id cannot be empty!", exception.getMessage());
    }

    @Test
    public void trackInvalidPersonIdTest() {
        String reportType = reportRequest1.getReports(0).getType();
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId("a").build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.track(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Person ID doesn't match rules", exception.getMessage());
    }

    @Test
    public void trackInvalidCarIdTest() {
        String reportType = reportRequest1.getReports(2).getType();
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId("1").build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.track(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Car ID doesn't match rules", exception.getMessage());
    }
    
    @Test
    public void trackInvalidTypeTest() {
        String reportId = reportRequest1.getReports(0).getId();
        TrackRequest request = TrackRequest.newBuilder().setType("a").setId(reportId).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.track(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Type is not a valid observation!", exception.getMessage());
    }

    @Test
    public void trackNonExistingPersonTest() {
        String reportType = reportRequest1.getReports(0).getType();
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId("123").build();
        TrackResponse response = frontend.track(request);
        assertEquals("", response.getId());
        assertEquals("", response.getType());
        assertEquals(0, response.getTimestamp().getSeconds());
        assertEquals("", response.getName());
        assertEquals(0, response.getLongitude(), 0.000001);
        assertEquals(0, response.getLatitude(), 0.000001);
    }

    @Test
    public void trackNonExistingCarTest() {
        String reportType = reportRequest1.getReports(2).getType();
        TrackRequest request = TrackRequest.newBuilder().setType(reportType).setId("AA12AA").build();
        TrackResponse response = frontend.track(request);
        assertEquals("", response.getId());
        assertEquals("", response.getType());
        assertEquals(0, response.getTimestamp().getSeconds());
        assertEquals("", response.getName());
        assertEquals(0, response.getLongitude(), 0.000001);
        assertEquals(0, response.getLatitude(), 0.000001);
    }

}