
package pt.tecnico.sauron.eye;

import java.util.NoSuchElementException;
import java.util.Scanner;

import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontendException;

public class EyeApp {

	public static void main(final String[] args) {

		System.out.println(EyeApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);

		// check arguments
		if (args.length < 5) {
			System.out.println("Incorrect amount of arguments!");
			System.out.printf("Usage: java %s host port%n", EyeApp.class.getName());
			return;
		}

		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

        Eye eye;
        try {
            eye = new Eye(args[0], args[1], //
            args[2], // camera name
            Double.parseDouble(args[3]), // latitude
            Double.parseDouble(args[4]), // longitude
            args[5]); // instance
        } catch (SiloFrontendException e) {
            System.out.println("FATAL: " + e.getMessage());
            System.out.println("Exiting...");
			return;
        }
		String[] input;
		String line;
		// Main cycle
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				try {
					line = scanner.nextLine();
				} catch (NoSuchElementException e) { // reached EOF
					eye.sendReport();
					eye.exit();
					break;
				}

				if (line.matches("^#.*")) // ignore comment
					continue;
				else if (line.isEmpty()) { // handle empty lines
					eye.sendReport();
					continue;
				}

				input = line.split(",");

				if (input.length != 2) // if input doesnt have arguments, mark as illegal
					input[0] = "illegal";

				switch (input[0]) {
					case "car":
					case "person":
						eye.addToReport(input[0], input[1]);
						break;
					case "zzz":
						eye.sleep(input[1]);
						break;
					case "illegal":
					default:
						System.out.println("Unsupported command: '" + line + "'");
				}
			}
		} catch (StatusRuntimeException e) {
			System.out.println(e.getMessage());
		}
	}

}
