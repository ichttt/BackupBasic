package backupBasic.util;
import java.awt.GraphicsEnvironment;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import backupBasic.backup.CopyManager;
import backupBasic.gui.GuiCreator;
/**
 * @author Tobias Hotz
 */
public class ThreadedBackup implements Runnable, Thread.UncaughtExceptionHandler {
	private static final Logger logger = Logger.getLogger(ThreadedBackup.class.getName());
	static {
		logger.setLevel(Level.ALL);
	}
	
	public static void startThreadedBackup() {
		Thread.UncaughtExceptionHandler eh = new ThreadedBackup();
		
        Thread myThread = new Thread(new ThreadedBackup());
        myThread.setDaemon(true);
        myThread.setName("Copy Thread");
        myThread.setUncaughtExceptionHandler(eh);
        myThread.start();
        
	}
	
	public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
            public void run() {
                shutdownManager();
            }
        });
        logger.finer("Starte Backup in neuem Thread");
		String OutDir = GuiCreator.OutDir;
		String SourceDir = GuiCreator.SourceDir;
		CopyManager.copyDir(OutDir, SourceDir);
	}
	
	/**
	 * Schreibt den Status, mit dem das Backup abgeschlossen wurde
	 * @param stringToWrite
	 * @param filename
	 */
	private static void writeExitStatus(String stringToWrite, String filename) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(GuiCreator.OutDir + "/" + CopyManager.time + "/" + filename);
			writer.write(stringToWrite);
		} catch (IOException e) {
			logger.warning("Fehler beim Erstellen von " + filename);
			e.printStackTrace();
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void shutdownManager() {
		if(CopyManager.CopyingFiles == true) {
			logger.info("Fordere Bendigung des Kopiervorgangs an...");
			//Timeout von 2 Sekunden, sonst wird der Vorgang abgebrochen
			CopyManager.stopBackup(20);
			//Falls das Kopieren nicht gestoppt wurde, abbrechen
			if(CopyManager.getCopyStopped()) {
				writeExitStatus("Es wurden nicht alle Datein kopiert, da vorher abgebrochen wurde. Auﬂerdem wurde mindestends eine Datei nicht komplett kopiert, diese ist wahrscheinlich unbrauchbar.", "INTERRUPTED.txt");
			}
			else {
				writeExitStatus("Es wurden nicht alle Datein kopiert da vorher abgebrochen wurde. Die Dateien, die kopiert worden sind, sind jedoch wahrscheinlich korrekt kopiert worden.", "UNFINISHED.txt");
			}
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
        logger.severe("Unbehandelte Ausnahme im Copy Thread.\t Stacktrace:");
        e.printStackTrace();
        if(!GraphicsEnvironment.isHeadless()) {
        	JOptionPane.showMessageDialog(null, "Unbehandelte Ausnahme im Copy Thread. Beende\nFehler: "+ e +"\nWeitere Details in der Console"
        								  , "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(-1);
	}

}