package backupBasic.util;
import java.awt.GraphicsEnvironment;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import backupBasic.backup.CopyManager;
import backupBasic.backup.Main;
import backupBasic.gui.GuiCreator;
/**
 * @author Tobias Hotz
 */
public class ThreadedBackup implements Runnable, Thread.UncaughtExceptionHandler {
	private static final Logger logger = Logger.getLogger(ThreadedBackup.class.getName());
	private static final ResourceBundle messages = Main.getMessages();
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
        logger.finer(messages.getString("ThreadStart"));
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
			logger.warning(messages.getString("CannotCreateFile") + filename);
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
			logger.info(messages.getString("RegisterStop"));
			//Timeout von 2 Sekunden, sonst wird der Vorgang abgebrochen
			CopyManager.stopBackup(20);
			//Falls das Kopieren nicht gestoppt wurde, abbrechen
			if(CopyManager.getCopyStopped()) {
				writeExitStatus(messages.getString("CopyInterrupted"), "INTERRUPTED.txt");
			}
			else {
				writeExitStatus(messages.getString("CopyUnfinished"), "UNFINISHED.txt");
			}
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
        logger.severe(messages.getString("UncaughtExceptionCopyThread") + "\t Stacktrace:");
        e.printStackTrace();
        if(!GraphicsEnvironment.isHeadless()) {
        	JOptionPane.showMessageDialog(null, messages.getString("UncaughtExceptionCopyThread") + "\n" + messages.getString("Error") + ": "+ e +"\n" +  messages.getString("Details"), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(-1);
	}

}