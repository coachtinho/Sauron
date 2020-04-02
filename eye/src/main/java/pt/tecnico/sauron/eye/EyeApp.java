
package pt.tecnico.sauron.eye;

import java.util.Scanner;

public class EyeApp {

	public static void main(final String[] args) {

		System.out.println(EyeApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);

		// check arguments
		if (args.length < 2) {
			System.out.println("Incorrect amount of arguments!");
			System.out.printf("Usage: java %s host port%n", EyeApp.class.getName());
			return;
		}

		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		final Eye eye = new Eye(args[0], Integer.parseInt(args[1]), //
				args[2], // camera name
				Double.parseDouble(args[3]), // longitude
				Double.parseDouble(args[4])); // latitude

		String[] arguments;
		String keyword;

		// Main cycle
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				keyword = scanner.next();

				// TODO: does next line restart line?
				// TODO: coment - regex
				// TODO: empty line - maybe switch below?
				// TODO: EOF - send report, exit loop

				switch (keyword) {
					case "":
						eye.sendReport();
						break;
					case "car":
					case "person":
						arguments = scanner.nextLine().split(",");
						eye.addToReport(arguments[0], arguments[1]);
						break;
					case "zzz":
						arguments = scanner.nextLine().split(",");
						eye.sleep(arguments[1]);
						break;
					default:
						System.out.println("Unsupported command: " + keyword);
				}
			}
		}
	}

}
