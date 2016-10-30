package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tobias Hotz
 */
public class Main {
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	private static ResourceBundle messages;
	static {
		logger.setLevel(Level.ALL);
	}
	
	/**
	 * Verwaltet das Logging-System
	 */
	private static void initLogging() {
		Handler fileHandler = null;
		try {
			if(System.getProperty("os.name").startsWith("Windows")) {
				//Ordner erstellen
				new File(System.getProperty("user.home") + "/AppData/Local/BackupBasic").mkdirs();
				fileHandler = new FileHandler(System.getProperty("user.home") + "/AppData/Local/BackupBasic/LogBackupBasic.xml");
			}
			else {
				//Ordner erstellen
				new File(System.getProperty("user.home") + "BackupBasic").mkdirs();
				fileHandler = new FileHandler(System.getProperty("user.home")+ "/BackupBasic/LogBackupBasic.xml");
			}
			//Fügt FileHandler global hinzu
		    logger.getParent().addHandler(fileHandler);
		}
		catch (IOException e) {
			logger.severe(messages.getString("LogError"));
			e.printStackTrace();
		}

	    logger.info(messages.getString("LogStart"));
	}
	
	public static ResourceBundle getMessages() {
		return messages;
	}
	
	
	/**
	 * Verwaltet das Logging-System und gibt dann an den ArgParser ab
	 * @param args
	 */
	public static void main(String[] args) {
		Locale currentLocale;
		String userLanguage;
		String userCountry;
		
		userCountry = System.getProperty("user.country");
		userLanguage = System.getProperty("user.language");
		
		currentLocale = new Locale(userLanguage, userCountry);
		try {
			messages = ResourceBundle.getBundle("BackupBasic", currentLocale);
		}
		catch(MissingResourceException e) {
			System.out.println("Fallback: English");
			messages = ResourceBundle.getBundle("BackupBasic", new Locale("en", "US"));
		}
		initLogging();
		ArgParser.parseArgs(args);
	}
}
