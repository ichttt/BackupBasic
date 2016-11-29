package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import backupBasic.gui.GuiCreator;
import backupBasic.util.i18n;
import com.sun.istack.internal.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
/**
 * @author Tobias Hotz
 */
public class CopyManager {
	private static final Logger logger = i18n.getLogger(CopyManager.class);
	
	//Parameter für ShutdownHook
	public static boolean CopyingFiles;
	private static boolean StopCopy = false;
	private static boolean CopyStopped = false;
	private static short Wait = 0;
	
	//Zeit ermittlen, auf Minuten kürzen und : durch Punkt ersetzen, da z.B. NTFS keinen : in Ordnern unterstützt
	//Wert wird auch in ThreadedBackup verwendet, deshalb public
	public static final String time = LocalDateTime.now().toString().substring(0, 16).replaceAll(":", ".");

	//Werden von ActionListenerCheckBox gesetzt
	public static boolean checkFilesOldDir = true ;
	public static boolean checkFilesOnFinish = true;
	
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
	 * @param Timeout in Zentelsekunden
	 */
	public static void stopBackup(@NotNull int Timeout) {
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
	private void executeCopyDir(@NotNull String sourceDir, @NotNull String outDir) {
		File[] Dateiliste = new File(sourceDir).listFiles();
		CopyingFiles = true;
		for (File file : Dateiliste) {
			if(StopCopy) {
				logger.finer(i18n.translate("CopyCancelSuccess"));
				CopyStopped = true;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(file.isFile()) {
				logger.fine(i18n.translate("CopyingFile") + sourceDir + file.getName()+ "...");
				String Save = file.getName();
				File Source = new File(sourceDir + Save);
				File Out = new File(outDir + Save);
				try {
					Files.copy(Source.toPath(), Out.toPath());
				}
				catch (IOException e) {
					logger.severe(i18n.translate("CopyErrorAbort"));
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
	private static void doWhenCopyDone(@NotNull File outDir, @NotNull List<File> sourceDirFilesList, @NotNull boolean canUseGui) {
		GuiCreator.progress.setValue(3);
		if(checkFilesOnFinish) {
			DirEqualChecker dirChecker = new DirEqualChecker();
			logger.info(i18n.translate("ListCopiedFiles"));
			List<File> outDirFilesList = dirChecker.listAllFiles(outDir);
			if(dirChecker.checkFiles(outDirFilesList, sourceDirFilesList)) {
				logger.info(i18n.translate("CopiedFilesEqual"));
			}
			else {
				logger.severe(i18n.translate("CopiedFileNotEqual"));
				if(canUseGui) {
					JOptionPane.showMessageDialog(null, i18n.translate("CopiedFileNotEqual"), "FileError" ,JOptionPane.ERROR_MESSAGE);
				}
				System.exit(954);
			}
		}
		logger.info(i18n.translate("CopySuccess"));
		if(canUseGui) {
			JOptionPane.showMessageDialog(null, i18n.translate("CopySuccess"));
		}
		System.exit(0);
	}
	
	
	
	/**
	 * Prüft, ob die Datein mit einem voherigen Backup übereinstimmen und kopiert ggf. das gesamte Verzeichnis samt Unterverzeichnis
	 */
	public static void copyDir(@NotNull String OutDirRawString, @NotNull String SourceDirString) {
		
		CopyManager CopyMgr = new CopyManager();
		
		final boolean canUseGui = !GraphicsEnvironment.isHeadless();
		File outDirRaw, outDir, sourceDir;
		File[] savesList;
		String outDirString;
		int savesCount, saveDirFilesCount;
		List<File> sourceDirFileList = null;
		
		outDirRaw = new File(OutDirRawString);
		sourceDir = new File(SourceDirString);	
		outDirString = outDirRaw +"/" + time + "/";	
		outDir = new File(outDirString);
		
		if(!sourceDir.exists()||!sourceDir.isDirectory()) {
			logger.warning(i18n.translate("SourceDirNotFound"));
			if(canUseGui) {
				JOptionPane.showMessageDialog(null, i18n.translate("SourceDirNotFound"), i18n.translate("SourceDirNotFoundTitle") , JOptionPane.WARNING_MESSAGE);
			}
			System.exit(0);
		}
		logger.fine(i18n.translate("SourceDir")+ sourceDir);
		logger.fine(i18n.translate("OutDir") + outDirRaw);
		
		//Versuche, Save Verzeichnis zur Erstellen. Falls es schon vorhanden ist, geschieht nichts
		outDirRaw.mkdir();
		
		if(outDir.exists()) {
			logger.warning(i18n.translate("SaveDoneThisMinute"));
			if(canUseGui) {
				JOptionPane.showMessageDialog(null, i18n.translate("SaveDoneThisMinute"));
			}
			System.exit(0);
		}
		
		savesList = outDirRaw.listFiles();
		savesCount = savesList.length;
		
		DirEqualChecker dirChecker = new DirEqualChecker();
		GuiCreator.progress.setValue(0);
		if(checkFilesOldDir || checkFilesOnFinish) {
			logger.info(i18n.translate("ListSourceDir"));
			sourceDirFileList = dirChecker.listAllFiles(sourceDir);
			logger.finer(sourceDirFileList.size() + i18n.translate("FilesInSourceDir"));
			GuiCreator.progress.setValue(1);

			if (savesCount >= 1 && checkFilesOldDir) {
				// We don't need to check if there are not the same number of files
				List<File> oldDirFilesList = dirChecker.listAllFiles(savesList[savesCount - 1]);
				saveDirFilesCount = oldDirFilesList.size();
				logger.finer(saveDirFilesCount + i18n.translate("FilesInOutDir"));
				if (oldDirFilesList.size() != sourceDirFileList.size()) {
					logger.info(i18n.translate("SkipCheckDir"));
				} else {

					//Check all Files
					logger.info(i18n.translate("ListOldDir"));
					if (dirChecker.checkFiles(sourceDirFileList, oldDirFilesList)) {
						//We don't need to copy any files
						logger.info(i18n.translate("SkipCopy"));
						if (canUseGui) {
							JOptionPane.showMessageDialog(null, i18n.translate("SkipCopy"));
						}
						System.exit(0);
					}
				}
			}
		}
		
		outDir.mkdir();
		logger.finer(i18n.translate("CreatingDir") + outDir);
		logger.info(i18n.translate("CopyStarted"));
		GuiCreator.progress.setValue(2);
		CopyMgr.executeCopyDir(SourceDirString, outDirString);
		doWhenCopyDone(outDir, sourceDirFileList,canUseGui);
	}
	
}