package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import static org.junit.Assert.*;

import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TraceRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TraceResponse;
import pt.tecnico.sauron.silo.grpc.Silo.TrackResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;


public class TraceIT extends BaseIT {

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
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("person").setId("1111").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("person").setId("1112").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1111").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1112").build());
        reportRequest1 = reportRequest1Builder.build();
        frontend.report(reportRequest1);
        Thread.sleep(1000);
        ReportRequest.Builder reportRequest2Builder = ReportRequest.newBuilder();
        reportRequest2Builder.setCameraName(camRequest2.getName());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("1111").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("1111").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("1113").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("1114").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1111").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1112").build());   
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1113").build());
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
    public void tracePersonTest() {
        String reportType = "person";
        String reportId = "1111";
        TraceRequest request = TraceRequest.newBuilder().setType(reportType).setId(reportId).build();
        List<TrackResponse> observationList = frontend.trace(request).getObservationList();
        assertEquals(3, observationList.size());
        observationList.forEach((observation) -> {
            assertEquals(reportType, observation.getType());
            assertEquals(reportId, observation.getId());
            long seconds = observation.getTimestamp().getSeconds();
            long secondsNow = System.currentTimeMillis() / 1000l;
            assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        });
        assertEquals(camRequest2.getName(), observationList.get(0).getName());
        assertEquals(camRequest2.getName(), observationList.get(1).getName());
        assertEquals(camRequest1.getName(), observationList.get(2).getName());
    }

    @Test
    public void traceCarTest() {
        String reportType = "car";
        String reportId = "AA1111";
        TraceRequest request = TraceRequest.newBuilder().setType(reportType).setId(reportId).build();
        List<TrackResponse> observationList = frontend.trace(request).getObservationList();
        assertEquals(2, observationList.size());
        observationList.forEach((observation) -> {
            assertEquals(reportType, observation.getType());
            assertEquals(reportId, observation.getId());
            long seconds = observation.getTimestamp().getSeconds();
            long secondsNow = System.currentTimeMillis() / 1000l;
            assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        });
        assertEquals(camRequest2.getName(), observationList.get(0).getName());
        assertEquals(camRequest1.getName(), observationList.get(1).getName());
    }

    @Test
    public void TraceEmptyTypeTest() {
        String reportId = reportRequest1.getReports(0).getId();
        TraceRequest request = TraceRequest.newBuilder().setId(reportId).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.trace(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Type cannot be empty!", exception.getMessage());
    }

    @Test
    public void TraceEmptyIdTest() {
        String reportType = reportRequest1.getReports(0).getType();
        TraceRequest request = TraceRequest.newBuilder().setType(reportType).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.trace(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Id cannot be empty!", exception.getMessage());
    }
    
    @Test
    public void TraceInvalidTypeTest() {
        String reportId = reportRequest1.getReports(0).getId();
        TraceRequest request = TraceRequest.newBuilder().setType("a").setId(reportId).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.trace(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Type is not a valid observation!", exception.getMessage());
    }

    @Test
    public void TraceNonExistingPersonTest() {
        String reportType = reportRequest1.getReports(0).getType();
        TraceRequest request = TraceRequest.newBuilder().setType(reportType).setId("123").build();
        TraceResponse response = frontend.trace(request);
        assertEquals(0, response.getObservationList().size());
    }

    @Test
    public void TraceNonExistingCarTest() {
        String reportType = reportRequest1.getReports(2).getType();
        TraceRequest request = TraceRequest.newBuilder().setType(reportType).setId("AA12AA").build();
        TraceResponse response = frontend.trace(request);
        assertEquals(0, response.getObservationList().size());
    }

}