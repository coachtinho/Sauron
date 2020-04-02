package pt.tecnico.sauron.spotter;

import java.util.Scanner;

public class SpotterApp {

	public static void main(String[] args) {

		System.out.println(SpotterApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);

		// check arguments
		if (args.length < 2) {
			System.out.println("Incorrect amount of arguments!");
			System.out.printf("Usage: java %s host port%n", SpotterApp.class.getName());
			return;
		}

		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		Spotter spotter = new Spotter(args[0], Integer.parseInt(args[1]));
		String[] arguments;
		String keyword, line;

		// Main cycle
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				keyword = scanner.next();

				switch (keyword) {
					case "spot":
						line = scanner.nextLine();
						if (line.matches(" [a-z]+ [a-zA-Z0-9*]+")) {
							arguments = line.split(" ");
							spotter.spot(arguments[1], arguments[2]);
						} else {
							System.out.println("Invalid arguments");
						}
						break;
					case "trail":
						line = scanner.nextLine();
						if (line.matches(" [a-z]+ [a-zA-Z0-9]+")) {
							arguments = line.split(" ");
							spotter.spot(arguments[1], arguments[2]);
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
						System.out.println("Unsupported command: " + keyword);
				}
			}
		}
	}
}
