
package ui;

import java.io.IOException;

import javax.swing.JOptionPane;

import logic.AccountService;
import logic.Scheduler;
import logic.SimulationClock;
import logic.ValidationException;
import model.AppData;
import storage.DataStore;

// Starts the application in normal or demo mode.
public class Main {

    public static AppData data;
    public static AccountService accounts;
    public static Scheduler scheduler;
    public static SimulationClock clock = new SimulationClock();

    private static DataStore store = new DataStore();

    public static void main(String[] args) {

        // Normal mode uses data/.
        // Demo mode uses demo-data/.
        boolean demoMode = args != null
                && args.length > 0
                && "--demo".equals(args[0]);

        store = demoMode
                ? new DataStore("demo-data")
                : new DataStore("data");

        clock = new SimulationClock();

        try {
            data = store.load(clock);
        } catch (ValidationException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "File error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        accounts = new AccountService(data);
        scheduler = new Scheduler(clock, data);

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> save())
        );

        System.out.println(
                demoMode
                ? "DEMO MODE - Loading demo-data/"
                : "NORMAL MODE - Loading data/"
        );

        new LoginFrame(data, accounts, scheduler);
    }

    // Save to the folder selected when the app started.
    public static void save() {

        if (data == null || clock == null) {
            return;
        }

        try {
            store.save(data, clock);
        } catch (IOException e) {
            System.out.println(
                    "Could not save: " + e.getMessage());
        }
    }
}
