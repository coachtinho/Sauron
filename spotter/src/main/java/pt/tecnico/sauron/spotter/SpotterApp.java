package pt.tecnico.sauron.spotter;

import java.util.NoSuchElementException;
import java.util.Scanner;

import pt.tecnico.sauron.silo.client.SiloFrontendException;
import pt.tecnico.sauron.silo.grpc.Silo.ObservationType;

public class SpotterApp {

	public static void main(String[] args) {

		System.out.println(SpotterApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);

		// check arguments
		if (args.length < 2) {
			System.out.println("Incorrect amount of arguments!");
			System.out.printf("Usage: java %s host port <instance>", SpotterApp.class.getName());
			return;
		}

		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// create the spotter object
		Spotter spotter;
		try {
			spotter = new Spotter(args[0], args[1], args[2]);
		} catch (SiloFrontendException e) {
			System.out.println("FATAL: " + e.getMessage());
			System.out.println("Exiting...");
			return;
		}		
		String[] arguments;
		String keyword, line;

		// Main cycle
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				ObservationType type;
				keyword = scanner.next();

				switch (keyword) {
					case "spot":
						// spot command
						line = scanner.nextLine();
						if (line.matches(" [a-z]+ [a-zA-Z0-9*]+")) {
							// checks if it's valid command
							arguments = line.split(" ");
							switch (arguments[1]) {
								// translates string to observation type
								case "person":
									type = ObservationType.PERSON;
									break;
								case "car":
									type = ObservationType.CAR;
									break;
								default:
									type = ObservationType.UNKNOWN;

							}
							spotter.spot(type, arguments[2]);
						} else {
							System.out.println("Invalid arguments");
						}
						break;
					case "trail":
						// trail command
						line = scanner.nextLine();
						if (line.matches(" [a-z]+ [a-zA-Z0-9]+")) {
							// checks if it's valid command
							arguments = line.split(" ");
							switch (arguments[1]) {
								// translates string to observation type
								case "person":
									type = ObservationType.PERSON;
									break;
								case "car":
									type = ObservationType.CAR;
									break;
								default:
									type = ObservationType.UNKNOWN;

							}
							spotter.trail(type, arguments[2]);
						} else {
							System.out.println("Invalid arguments");
						}
						break;
					case "ping":
						// ignores rest of the line
						scanner.nextLine();
						spotter.ping();
						break;
					case "clear":
						// ignores rest of the line
						scanner.nextLine();
						spotter.clear();
						break;
					case "init":
						// ignores rest of the line
						scanner.nextLine();
						spotter.init();
						break;
					case "help":
						// ignores rest of the line
						scanner.nextLine();
						spotter.help();
						break;
					case "exit":
						spotter.exit();
						return;
					default:
						// ignores rest of the line
						scanner.nextLine();
						System.out.println("Unsupported command: " + keyword);
				}
			}
		} catch (NoSuchElementException e) {
			spotter.exit();
		}
	}
}
