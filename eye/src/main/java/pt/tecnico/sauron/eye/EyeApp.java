
package pt.tecnico.sauron.eye;

import java.util.Scanner;

public class EyeApp {

	public static void main(String[] args) {

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

		Eye spotter = new Eye(args[0], Integer.parseInt(args[1]));
		String[] arguments;
		String keyword;

		// Main cycle
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				keyword = scanner.next();

				switch (keyword) {
					case "spot":
						arguments = scanner.nextLine().split(" ");
						spotter.spot(arguments[0], arguments[1]);
						break;
					case "trail":
						arguments = scanner.nextLine().split(" ");
						spotter.spot(arguments[0], arguments[1]);
						break;
					case "clear":
						spotter.clear();
						break;
					case "init":
						spotter.init();
						break;
					case "help":
						spotter.help();
						break;
					case "exit":
						spotter.exit();
						return;
					default:
						System.out.println("Unsupported command: " + keyword);
				}
			}
		}
	}

}
