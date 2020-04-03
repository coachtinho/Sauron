package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.Assert.*;

import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackMatchRequest;
import pt.tecnico.sauron.silo.grpc.Silo.TrackMatchResponse;
import pt.tecnico.sauron.silo.grpc.Silo.TrackResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

public class TrackMatchIT extends BaseIT {
    private static SiloFrontend frontend;
    private static int port;
    private static String host;
    private static int offset;
    private static CameraRegistrationRequest camRequest1;
    private static CameraRegistrationRequest camRequest2;
    private static ReportRequest reportRequest1;
    private static ReportRequest reportRequest2; 

    @BeforeAll
    public static void oneTimeSetUp() throws StatusRuntimeException, InterruptedException {
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
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("person").setId("1111").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("person").setId("1112").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("car").setId("11AA11").build());
        reportRequest1Builder.addReports(ReportItem.newBuilder().setType("car").setId("BA1111").build());
        reportRequest1 = reportRequest1Builder.build();
        frontend.report(reportRequest1);
        Thread.sleep(1000);
        ReportRequest.Builder reportRequest2Builder = ReportRequest.newBuilder();
        reportRequest2Builder.setCameraName(camRequest2.getName());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("1111").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("2113").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("person").setId("2114").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("11AA11").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("AA1112").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("BA1111").build());
        reportRequest2Builder.addReports(ReportItem.newBuilder().setType("car").setId("BA1112").build());   
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
    public void trackMatchPerfectMatchPersonTest() {
        String reportType = "person";
        String reportId = "1111";
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).setId(reportId).build();
        List<TrackResponse> observationList = frontend.trackMatch(request).getObservationList();
        assertEquals(1, observationList.size());
        TrackResponse trackResponse = observationList.get(0);
        assertEquals(reportId, trackResponse.getId());
        assertEquals(reportType, trackResponse.getType());
        long seconds = trackResponse.getTimestamp().getSeconds();
        long secondsNow = System.currentTimeMillis() / 1000l;
        assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        assertEquals(camRequest2.getName(), trackResponse.getName());
        assertEquals(camRequest2.getLongitude(), trackResponse.getLongitude(), 0.000001);
        assertEquals(camRequest2.getLatitude(), trackResponse.getLatitude(), 0.000001);
    }

    @Test
    public void trackMatchPerfectMatchCarTest() {
        String reportType = "car";
        String reportId = "11AA11";
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).setId(reportId).build();
        List<TrackResponse> observationList = frontend.trackMatch(request).getObservationList();
        assertEquals(1, observationList.size());
        TrackResponse trackResponse = observationList.get(0);
        assertEquals(reportId, trackResponse.getId());
        assertEquals(reportType, trackResponse.getType());
        long seconds = trackResponse.getTimestamp().getSeconds();
        long secondsNow = System.currentTimeMillis() / 1000l;
        assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        assertEquals(camRequest2.getName(), trackResponse.getName());
        assertEquals(camRequest2.getLongitude(), trackResponse.getLongitude(), 0.000001);
        assertEquals(camRequest2.getLatitude(), trackResponse.getLatitude(), 0.000001);
    }

    @Test
    public void trackMatchStartsWithTest() {
        String reportType = "person";
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).setId("1*").build();
        TrackMatchResponse response = frontend.trackMatch(request);
        List<TrackResponse> trackResponses = response.getObservationList();
        assertEquals(2, trackResponses.size());
        trackResponses.forEach((trackResponse) -> {
            assertTrue(trackResponse.getId(), trackResponse.getId().startsWith("1"));
            assertEquals(reportType, trackResponse.getType());
            long seconds = trackResponse.getTimestamp().getSeconds();
            long secondsNow = System.currentTimeMillis() / 1000l;
            assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
            if (trackResponse.getId().endsWith("1")) {
                assertEquals(camRequest2.getName(), trackResponse.getName());
                assertEquals(camRequest2.getLongitude(), trackResponse.getLongitude(), 0.000001);
                assertEquals(camRequest2.getLatitude(), trackResponse.getLatitude(), 0.000001);
            } else {
                assertEquals(camRequest1.getName(), trackResponse.getName());
                assertEquals(camRequest1.getLongitude(), trackResponse.getLongitude(), 0.000001);
                assertEquals(camRequest1.getLatitude(), trackResponse.getLatitude(), 0.000001);
            }
        });
    }

    @Test
    public void trackMatchEndsWithTest() {
        String reportType = "car";
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).setId("*1").build();
        TrackMatchResponse response = frontend.trackMatch(request);
        List<TrackResponse> trackResponses = response.getObservationList();
        assertEquals(2, trackResponses.size());
        trackResponses.forEach((trackResponse) -> {
            assertTrue(trackResponse.getId(), trackResponse.getId().endsWith("1"));
            assertEquals(reportType, trackResponse.getType());
            long seconds = trackResponse.getTimestamp().getSeconds();
            long secondsNow = System.currentTimeMillis() / 1000l;
            assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
            assertEquals(camRequest2.getName(), trackResponse.getName());
            assertEquals(camRequest2.getLongitude(), trackResponse.getLongitude(), 0.000001);
            assertEquals(camRequest2.getLatitude(), trackResponse.getLatitude(), 0.000001);
        });
    }

    @Test
    public void trackMatchStartWithEndsWithTest() {
        String reportType = "car";
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).setId("B*1").build();
        List<TrackResponse> observationList = frontend.trackMatch(request).getObservationList();
        assertEquals(1, observationList.size());
        TrackResponse trackResponse = observationList.get(0);
        assertTrue(trackResponse.getId(), trackResponse.getId().endsWith("1") && trackResponse.getId().startsWith("B"));
        assertEquals(reportType, trackResponse.getType());
        long seconds = trackResponse.getTimestamp().getSeconds();
        long secondsNow = System.currentTimeMillis() / 1000l;
        assertTrue(Long.toString(seconds), (seconds >= secondsNow - offset) && (seconds <= secondsNow + offset));
        assertEquals(camRequest2.getName(), trackResponse.getName());
        assertEquals(camRequest2.getLongitude(), trackResponse.getLongitude(), 0.000001);
        assertEquals(camRequest2.getLatitude(), trackResponse.getLatitude(), 0.000001);
    }

    @Test
    public void trackMatchEmptyTypeTest() {
        String reportId = reportRequest1.getReports(0).getId();
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setId(reportId).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Type cannot be empty!", exception.getMessage());
    }

    @Test
    public void trackMatchEmptyIdTest() {
        String reportType = reportRequest1.getReports(0).getType();
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Id cannot be empty!", exception.getMessage());
    }
    
    @Test
    public void trackMatchInvalidTypeTest() {
        String reportId = reportRequest1.getReports(0).getId();
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType("a").setId(reportId).build();
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(request));
        assertEquals(INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Type is not a valid observation!", exception.getMessage());
    }

    @Test
    public void trackMatchNonExistingPersonTest() {
        String reportType = reportRequest1.getReports(0).getType();
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).setId("123").build();
        TrackMatchResponse response = frontend.trackMatch(request);
        assertEquals(0, response.getObservationList().size());
    }

    @Test
    public void trackMatchNonExistingCarTest() {
        String reportType = reportRequest1.getReports(2).getType();
        TrackMatchRequest request = TrackMatchRequest.newBuilder().setType(reportType).setId("AA12AA").build();
        TrackMatchResponse response = frontend.trackMatch(request);
        assertEquals(0, response.getObservationList().size());
    }

}