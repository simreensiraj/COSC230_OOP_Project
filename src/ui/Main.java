package ui;

import java.io.IOException;

import javax.swing.JOptionPane;

import logic.AccountService;
import logic.Scheduler;
import logic.SimulationClock;
import logic.ValidationException;
import model.AppData;
import storage.DataStore;

// starts the app: loads the saved data, opens the login window, saves when the app closes
// there's one AppData, AccountService, Scheduler and clock for the whole app, passed into the windows
public class Main {

	public static AppData data;
	public static AccountService accounts;
	public static Scheduler scheduler;
	public static SimulationClock clock = new SimulationClock();
	private static DataStore store = new DataStore();

	public static void main(String[] args) {
		try {
			data = store.load(clock);
		} catch (ValidationException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		accounts = new AccountService(data);
		scheduler = new Scheduler(clock, data);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> save())); // save on exit

		new LoginFrame(data, accounts, scheduler);
	}

	// call this after changes too, so nothing is lost if the app crashes
	public static void save() {
		try {
			store.save(data, clock);
		} catch (IOException e) {
			System.out.println("Could not save: " + e.getMessage());
		}
	}
}