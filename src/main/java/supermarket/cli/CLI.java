package supermarket.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;

import supermarket.register.Supermarket;

/**
 * Command-line user interface for the Supermarket checkout system (Part 2).
 * Loads the initial configuration file {@code my_supermarket.ini} if present,
 * then reads commands line by line until {@code exit} or end of input.
 */
public class CLI {
    private static final String CONFIG_FILE = "my_supermarket.ini";

    public static void main(String[] args) {
        // Stable, locale-independent output (period decimals) and UTF-8 console.
        Locale.setDefault(Locale.US);
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        Supermarket market = new Supermarket();
        CommandContext context = new CommandContext(market);
        CommandDispatcher dispatcher = new CommandDispatcher(context, out);

        loadInitialConfiguration(dispatcher, out);

        out.println("Supermarket checkout system ready. Type 'help' for commands, 'exit' to quit.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (context.isRunning() && scanner.hasNextLine()) {
                out.print("> ");
                dispatcher.execute(scanner.nextLine());
            }
        }
    }

    private static void loadInitialConfiguration(CommandDispatcher dispatcher, PrintStream out) {
        Path config = Path.of(CONFIG_FILE);
        if (!Files.exists(config)) {
            return;
        }
        out.println("Loading initial configuration from " + CONFIG_FILE + " ...");
        try {
            for (String line : Files.readAllLines(config)) {
                dispatcher.execute(line);
            }
        } catch (IOException e) {
            out.println("Warning: could not read " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}
