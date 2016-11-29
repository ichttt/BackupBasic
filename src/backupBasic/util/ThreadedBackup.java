package backupBasic.util;

import backupBasic.backup.CopyManager;
import backupBasic.gui.GuiCreator;
import com.sun.istack.internal.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
/**
 * @author Tobias Hotz
 */
public class ThreadedBackup implements Runnable {
	private static final Logger logger = i18n.getLogger(ThreadedBackup.class);
	
	public static void startThreadedBackup() {

        Thread myThread = new Thread(new ThreadedBackup());
        myThread.setDaemon(true);
        myThread.setName("Copy Thread");
        myThread.start();
        
	}
	
	public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
            public void run() {
                shutdownManager();
            }
        });
        logger.finer(i18n.translate("ThreadStart"));
		String OutDir = GuiCreator.OutDir;
		String SourceDir = GuiCreator.SourceDir;
		CopyManager.copyDir(OutDir, SourceDir);
	}
	
	/**
	 * Schreibt den Status, mit dem das Backup abgeschlossen wurde
	 * @param stringToWrite
	 * @param filename
	 */
	private static void writeExitStatus(@NotNull String stringToWrite, @NotNull String filename) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(GuiCreator.OutDir + "/" + CopyManager.time + "/" + filename);
			writer.write(stringToWrite);
		} catch (IOException e) {
			logger.warning(i18n.translate("CannotCreateFile") + filename);
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
		if (CopyManager.CopyingFiles) {
			logger.info(i18n.translate("RegisterStop"));
			//Timeout von 2 Sekunden, sonst wird der Vorgang abgebrochen
			CopyManager.stopBackup(20);
			//Falls das Kopieren nicht gestoppt wurde, abbrechen
			if (CopyManager.getCopyStopped()) {
				writeExitStatus(i18n.translate("CopyInterrupted"), "INTERRUPTED.txt");
			} else {
				writeExitStatus(i18n.translate("CopyUnfinished"), "UNFINISHED.txt");
			}
		}
	}

}