package pt.tecnico.sauron.eye;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationRequest;
import pt.tecnico.sauron.silo.grpc.Silo.CameraRegistrationResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest;
import pt.tecnico.sauron.silo.grpc.Silo.ReportResponse;
import pt.tecnico.sauron.silo.grpc.Silo.ReportRequest.ReportItem;
import pt.tecnico.sauron.silo.grpc.Silo.ReportResponse.FailureItem;

import java.util.ArrayList;
import java.util.List;

import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;

public class Eye {

    SiloFrontend _frontend;
    List<Item> _reports;
    String _name;
    double _longitude;
    double _latitude;

    public Eye(final String host, final int port, final String name, final double longitude, final double latitude) {
        _frontend = new SiloFrontend(host, port);
        _reports = new ArrayList<>();
    }

    public void addToReport(final String type, final String id) {
        // Check arguments
        if (type == null) // Check if type exists
            System.out.println("Type must be specified");
        else if (id == null) // Check if id exists
            System.out.println("Id must be specified");

        switch (type) {
            case "car":
                if (!type.matches("[A-Z0-9]{6}"))
                    System.out.println("Id " + type + "is illegal for type car");
                break;
            case "person":
                if (!type.matches("[0-9]+"))
                    System.out.println("Id " + type + "is illegal for type person");
                break;
            default:
                System.out.println("Type not recognized");
                break;
        }

        // Add item to list
        final Item item = new Item(type, id);
        _reports.add(item);
    }

    public void sendReport() {
        // Check if there are items to report
        if (_reports.isEmpty())
            return;

        // Create report message
        final ReportRequest.Builder requestBuilder = ReportRequest.newBuilder();
        for (final Item item : _reports) {
            final ReportItem report = ReportItem.newBuilder() //
                    .setId(item.getId()) //
                    .setType(item.getType()) //
                    .build();
            requestBuilder.addReports(report);
        }
        // Send report message
        try {
            final ReportResponse response = _frontend.report(requestBuilder.build());
            final List<FailureItem> failures = response.getFailuresList();
            if (failures.isEmpty())
                System.out.println("Sucessfully reported " + _reports.size() + " items");
            else
                failures.forEach((failure) -> System.out.println(failure.getMessage()));
        } catch (final StatusRuntimeException exception) {
            handleException(exception);
        } finally { // Clear report list
            _reports.clear();
        }
    }

    public void register() {
        try {
            CameraRegistrationResponse response = _frontend.camJoin(_name, _longitude, _latitude);
            System.out.println("Camera was sucessfully registered");
        } catch (final StatusRuntimeException exception) {
            handleException(exception);
        }

    }

    public void help() {
        System.out.println("Eye: submits observations of the objects with identifiers\n" //
                + "\tUsage: objectType,id\n" //
                + "Help: shows commands supported by application\n" + "   Usage: help\n"//
                + "Exit: exits the application\n" + "   Usage: exit");
    }

    public void sleep(String time) {
        try {
            long sleep = Long.parseLong(time);
            Thread.sleep(sleep);
        } catch (NumberFormatException e) {
            System.out.println("Not a valid sleep time: " + time);
        } catch (InterruptedException e) {
            System.out.println("System was interrupted, try again later");
        }

    }

    // Add more custom messages if needed
    public void handleException(final StatusRuntimeException exception) {
        final Code statusCode = exception.getStatus().getCode();
        switch (statusCode) {
            case UNAVAILABLE:
                System.out.println("Server is currently unavailable");
                break;
            case ALREADY_EXISTS:
                System.out.println("Camera with this name already exists");
                break;
            default:
                System.out.println(
                        "Caught exception with code " + statusCode + " and description: " + exception.getMessage());
        }
    }

    public void exit() {
        System.out.println("Exiting...");
        _frontend.close();
    }

    class Item {
        private final String _type;
        private final String _id;

        Item(final String type, final String id) {
            _type = type;
            _id = id;
        }

        String getId() {
            return _id;
        }

        String getType() {
            return _type;
        }

    }

}