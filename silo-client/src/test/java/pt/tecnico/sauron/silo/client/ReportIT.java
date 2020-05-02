package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import static io.grpc.Status.INVALID_ARGUMENT;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ClearRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;
import pt.tecnico.sauron.silo.grpc.Silo.ReportResponse.FailureItem;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationType;


public class ReportIT extends BaseIT {

    // static members
    private static final String host = testProps.getProperty("zoo.host");
    private static final String port = testProps.getProperty("zoo.port");
    private static final String instance = testProps.getProperty("server.instance");
    private static SiloFrontend frontend;
    private final String CAM_NAME = "TESTCAM1";
    private final String VALID_PERSON_ID = "1234";
    private final String INVALID_PERSON_ID = "123456a";
    private final String VALID_CAR_ID = "12DDLG";
    private final String INVALID_CAR_ID = "MILFS0";

    // one-time initialization and clean-up
    @BeforeAll
    public static void oneTimeSetUp() throws SiloFrontendException{
        frontend = new SiloFrontend(host, port, instance);
    }

    @AfterAll
    public static void oneTimeTearDown() {
        frontend.close();
    }

    // initialization and clean-up for each test

    @BeforeEach
    public void setUp() throws SiloFrontendException {
        CameraRegistrationRequest camRequest1 = CameraRegistrationRequest.newBuilder().setName(CAM_NAME)
                .setLatitude(123.1).setLongitude(123.412).build();
        frontend.camJoin(camRequest1);
    }

    @AfterEach
    public void tearDown() {
        ClearRequest request = ClearRequest.getDefaultInstance();
        frontend.ctrlClear(request);
    }

    // ################## TESTS ########################

    @Test
    public void reportOneCarOKTest() throws SiloFrontendException {
        // Arrange
        final ReportRequest.Builder request = makeReportRequest(ObservationType.CAR, VALID_CAR_ID, CAM_NAME);

        // Act
        ReportResponse response = frontend.report(request.build());
        final List<FailureItem> failures = response.getFailuresList();

        // Assert
        assertTrue(failures.isEmpty());
    }

    @Test
    public void reportOnePersonOKTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.PERSON, VALID_PERSON_ID, CAM_NAME);

        // Act
        ReportResponse response = frontend.report(request.build());
        final List<FailureItem> failures = response.getFailuresList();

        // Assert
        assertTrue(failures.isEmpty());
    }

    @Test
    public void reportNoneOKTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.UNKNOWN, "", CAM_NAME);
        request.clearReports(); // emtpy report list

        // Act
        ReportResponse response = frontend.report(request.build());
        final List<FailureItem> failures = response.getFailuresList();

        // Assert
        assertTrue(request.getReportsCount() == 0);
        assertTrue(failures.isEmpty());
    }

    @Test
    public void reportMultipleOKTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.PERSON, VALID_PERSON_ID, CAM_NAME);
        ReportItem item = makeReportItem(ObservationType.CAR, VALID_CAR_ID);
        request.addReports(item);

        // Act
        ReportResponse response = frontend.report(request.build());
        final List<FailureItem> failures = response.getFailuresList();

        // Assert        
        assertTrue(failures.isEmpty());
    }

    @Test
    public void reportCarInvalidIdFailTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.CAR, INVALID_CAR_ID, CAM_NAME);

        // Act
        System.out.println("127:" + request.getReportsCount());
        System.out.println("128:" + request.getReportsList().get(0).getId());
        ReportResponse response = frontend.report(request.build());
        List<FailureItem> failures = response.getFailuresList();

        // Assert
        System.out.println("131:" + failures.size());
        assertTrue(failures.size() == 1);
        FailureItem fail = failures.get(0);
        assertTrue(ObservationType.CAR == fail.getType());
        assertTrue(INVALID_CAR_ID.equals(fail.getId()));
        String expected = "Invalid id '" + INVALID_CAR_ID + "' for type " + ObservationType.CAR;
        assertTrue(expected.equals(fail.getMessage()));
    }

    @Test
    public void reportPersonInvalidIdFailTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.PERSON, INVALID_PERSON_ID, CAM_NAME);

        // Act
        ReportResponse response = frontend.report(request.build());
        List<FailureItem> failures = response.getFailuresList();

        // Assert
        assertTrue(failures.size() == 1);
        FailureItem fail = failures.get(0);
        assertTrue(ObservationType.PERSON == fail.getType());
        assertTrue(INVALID_PERSON_ID.equals(fail.getId()));
        String expected = "Invalid id '" + INVALID_PERSON_ID + "' for type " + ObservationType.PERSON;
        assertTrue(expected.equals(fail.getMessage()));
    }

    @Test
    public void reportIllegalCameraExceptionTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.PERSON, VALID_PERSON_ID, "CAM_THAT_DOESNT_EXIST");

        // Act & Assert
        Exception exception = assertThrows(StatusRuntimeException.class, () -> frontend.report(request.build()));
        assertEquals(INVALID_ARGUMENT.asRuntimeException().getClass(), exception.getClass());
        String expectedMessage = "No such camera";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void reportMultipleWithOneWrongFailTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.PERSON, VALID_PERSON_ID, CAM_NAME);
        ReportItem item = makeReportItem(ObservationType.CAR, VALID_CAR_ID);
        request.addReports(item);
        item = makeReportItem(ObservationType.CAR, INVALID_CAR_ID);
        request.addReports(item);

        // Act
        ReportResponse response = frontend.report(request.build());
        List<FailureItem> failures = response.getFailuresList();

        // Assert
        assertTrue(failures.size() == 1);
        FailureItem fail = failures.get(0);
        assertTrue(ObservationType.CAR == fail.getType());
        assertTrue(INVALID_CAR_ID.equals(fail.getId()));
        String expected = "Invalid id '" + INVALID_CAR_ID + "' for type " + ObservationType.CAR;
        assertTrue(expected.equals(fail.getMessage()));
    }

    @Test
    public void reportMultipleWithMultipleWrongFailTest() throws SiloFrontendException {
        // Arrange
        ReportRequest.Builder request = makeReportRequest(ObservationType.PERSON, VALID_PERSON_ID, CAM_NAME);
        ReportItem item = makeReportItem(ObservationType.CAR, VALID_CAR_ID);
        request.addReports(item);
        item = makeReportItem(ObservationType.CAR, INVALID_CAR_ID);
        request.addReports(item);
        item = makeReportItem(ObservationType.PERSON, INVALID_PERSON_ID);
        request.addReports(item);

        // Act
        ReportResponse response = frontend.report(request.build());
        List<FailureItem> failures = response.getFailuresList();

        // Assert
        assertTrue(failures.size() == 2);

        // Assert 1st failure
        FailureItem fail = failures.get(0);
        assertTrue(ObservationType.CAR == fail.getType());
        assertTrue(INVALID_CAR_ID.equals(fail.getId()));
        String expected = "Invalid id '" + INVALID_CAR_ID + "' for type " + ObservationType.CAR;
        assertTrue(expected.equals(fail.getMessage()));

        // Assert 2nd failure
        fail = failures.get(1);
        assertTrue(ObservationType.PERSON == fail.getType());
        assertTrue(INVALID_PERSON_ID.equals(fail.getId()));
        expected = "Invalid id '" + INVALID_PERSON_ID + "' for type " + ObservationType.PERSON;
        assertTrue(expected.equals(fail.getMessage()));
    }

    // ################## END OF TESTS ########################

    // AUX functions

    private ReportRequest.Builder makeReportRequest(ObservationType type, String id, String camera) {
        ReportRequest.Builder requestBuilder = ReportRequest.newBuilder();
        requestBuilder.setCameraName(camera);
        ReportItem report = makeReportItem(type, id);
        requestBuilder.addReports(report);
        return requestBuilder;
    }

    private ReportItem makeReportItem(ObservationType type, String id) {
        // Create report message
        return ReportItem.newBuilder() //
                .setType(type) //
                .setId(id)//
                .build();
    }
}
