package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tobias Hotz
 */
public class Main {
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	static {
		logger.setLevel(Level.ALL);
	}
	
	/**
	 * Verwaltet das Logging-System und gibt dann an den ArgParser ab
	 * @param args
	 */
	public static void main(String[] args) {
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
			logger.severe("Fehler beim Erstellen der Log-Datei!");
			e.printStackTrace();
		}

	    logger.info("Logger gestartet");
		ArgParser.parseArgs(args);
	}
}
