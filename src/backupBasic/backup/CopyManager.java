package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */
import backupBasic.gui.GuiCreator;

import java.awt.GraphicsEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
/**
 * @author Tobias Hotz
 */
public class CopyManager {
	private static final Logger logger = Logger.getLogger(CopyManager.class.getName());
	private static final ResourceBundle messages = Main.getMessages();
	static {
		logger.setLevel(Level.ALL);
	}
	
	//Parameter für ShutdownHook
	public static boolean CopyingFiles;
	private static boolean StopCopy = false;
	private static boolean CopyStopped = false;
	private static short Wait = 0;
	
	//Zeit ermittlen, auf Minuten kürzen und : durch Punkt ersetzen, da z.B. NTFS keinen : in Ordnern unterstützt
	//Wert wird auch in ThreadedBackup verwendet, deshalb public
	public static final String time = LocalDateTime.now().toString().substring(0, 16).replaceAll(":", ".");

	//Werden von ActionListenerCheckBox gesetzt
	public static boolean calcCheckSumOldDir = true ;
	public static boolean calcCheckSumOnFinish = true;
	
	/**
	 * Wert nicht public, um ihn read-only von extern zu machen, um Fehler zu vermeiden
	 * @return CopyStopped
	 */
	public static boolean getCopyStopped() {
		return CopyStopped;
	}
	
	/**
	 * Endet, wenn
	 * a) Eine Datei fertig geschrieben wurde
	 * b) Keine Datei geschrieben wird
	 * c) Das Timeout erreicht wurde
	 * TODO: Iterativ?
	 * @param Timeout in Zentelsekunden
	 */
	public static void stopBackup(int Timeout) {
		StopCopy = true;
		Wait++;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
			//TODO Was sollten wir hier tun?
		}
		//Nach Timeout einfach abbrechen
		if(!CopyStopped && Wait <Timeout) {
			stopBackup(Timeout);
		}
	}
	
	/**
	 * Interne Methode zum eigentlichen kopieren.
	 * @param sourceDir
	 * @param outDir
	 */
	private void executeCopyDir(String sourceDir, String outDir) {
		File[] Dateiliste = new File(sourceDir).listFiles();
		CopyingFiles = true;
		for (File file : Dateiliste) {
			if(StopCopy) {
				logger.finer(messages.getString("CopyCancelSuccess"));
				CopyStopped = true;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(file.isFile()) {
				logger.fine(messages.getString("CopyingFile") + sourceDir + file.getName()+ "...");
				String Save = file.getName();
				File Source = new File(sourceDir + Save);
				File Out = new File(outDir + Save);
				try {
					Files.copy(Source.toPath(), Out.toPath());
				}
				catch (IOException e) {
					logger.severe(messages.getString("CopyErrorAbort"));
					e.printStackTrace();
					System.exit(1);
				}
			}
			else {
				File CreateSubDir = new File(outDir + file.getName());
				CreateSubDir.mkdir();
				executeCopyDir(file.toString() + "/", outDir + file.getName() + "/");
			}
		}
		CopyingFiles = false;
	}
	
	/**
	 * Wird aufgerufen, wenn der Kopiervorgang erfolgreich abgeschlossen wurde
	 * @param outDir
	 * @param sourceDirFilesList
	 * @param canUseGui
	 */
	private static void doWhenCopyDone(File outDir,Vector<File> sourceDirFilesList ,boolean canUseGui) {
		GuiCreator.progress.setValue(3);
		if(calcCheckSumOnFinish) {
			DirEqualChecker dirChecker = new DirEqualChecker();
			logger.info(messages.getString("CheckSumCopiedFiles"));
			Vector<File> outDirFilesList = dirChecker.listAllFiles(outDir);
			if(dirChecker.checkFiles(outDirFilesList, sourceDirFilesList)) {
				logger.info(messages.getString("CheckSumCopiedFilesSuccess"));
			}
			else {
				logger.severe(messages.getString("CheckSumCopiedFilesError"));
				if(canUseGui) {
					JOptionPane.showMessageDialog(null, messages.getString("CheckSumCopiedFilesError"), "CheckSumError" ,JOptionPane.ERROR_MESSAGE);
				}
				System.exit(954);
			}
		}
		logger.info(messages.getString("CopySuccess"));
		if(canUseGui) {
			JOptionPane.showMessageDialog(null, messages.getString("CopySuccess"));
		}
		System.exit(0);
	}
	
	
	
	/**
	 * Prüft, ob die Datein mit einem voherigen Backup übereinstimmen und kopiert ggf. das gesamte Verzeichnis samt Unterverzeichnis
	 */
	public static void copyDir(String OutDirRawString, String SourceDirString) {	
		
		CopyManager CopyMgr = new CopyManager();
		
		final boolean canUseGui = !GraphicsEnvironment.isHeadless();
		File outDirRaw, outDir, sourceDir;
		File[] savesList;
		String outDirString;
		int savesCount, saveDirFilesCount;
		Vector<File> sourceDirFileList = null;
		
		outDirRaw = new File(OutDirRawString);
		sourceDir = new File(SourceDirString);	
		outDirString = outDirRaw +"/" + time + "/";	
		outDir = new File(outDirString);
		
		if(!sourceDir.exists()||!sourceDir.isDirectory()) {
			logger.warning(messages.getString("SourceDirNotFound"));
			if(canUseGui) {
				JOptionPane.showMessageDialog(null, messages.getString("SourceDirNotFound"), messages.getString("SourceDirNotFoundTitle") , JOptionPane.WARNING_MESSAGE);
			}
			System.exit(0);
		}
		logger.fine(messages.getString("SourceDir")+ sourceDir);
		logger.fine(messages.getString("OutDir") + outDirRaw);
		
		//Versuche, Save Verzeichnis zur Erstellen. Falls es schon vorhanden ist, geschieht nichts
		outDirRaw.mkdir();
		
		if(outDir.exists()) {
			logger.warning(messages.getString("SaveDoneThisMinute"));
			if(canUseGui) {
				JOptionPane.showMessageDialog(null, messages.getString("SaveDoneThisMinute"));
			}
			System.exit(0);
		}
		
		savesList = outDirRaw.listFiles();
		savesCount = savesList.length;
		
		DirEqualChecker dirChecker = new DirEqualChecker();
		GuiCreator.progress.setValue(0);
		if(calcCheckSumOldDir|| calcCheckSumOnFinish) {
			logger.info(messages.getString("CheckSumSourceDir"));
			sourceDirFileList = dirChecker.listAllFiles(sourceDir);
			logger.finer(sourceDirFileList.size() + messages.getString("FilesInSourceDir"));
			GuiCreator.progress.setValue(1);

			if (savesCount >= 1 && calcCheckSumOldDir) {
				// We don't need to check if there are not the same number of files
				Vector<File> oldDirFilesList = dirChecker.listAllFiles(savesList[savesCount - 1]);
				saveDirFilesCount = oldDirFilesList.size();
				logger.finer(saveDirFilesCount + messages.getString("FilesInOutDir"));
				if (oldDirFilesList.size() != sourceDirFileList.size()) {
					logger.info(messages.getString("SkipCheckSum"));
				} else {

					//Check all Files
					logger.info(messages.getString("CheckSumOldDir"));
					if (dirChecker.checkFiles(sourceDirFileList, oldDirFilesList)) {
						//We don't need to copy any files
						logger.info(messages.getString("SkipCopy"));
						if (canUseGui) {
							JOptionPane.showMessageDialog(null, messages.getString("SkipCopy"));
						}
						System.exit(0);
					}
				}
			}
		}
		
		outDir.mkdir();
		logger.finer(messages.getString("CreatingDir") + outDir);
		logger.info(messages.getString("CopyStarted"));
		GuiCreator.progress.setValue(2);
		CopyMgr.executeCopyDir(SourceDirString, outDirString);
		doWhenCopyDone(outDir, sourceDirFileList,canUseGui);
	}
	
}