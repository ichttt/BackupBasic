package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */
import java.awt.GraphicsEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
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
	
	public static boolean getStopCopy() {
		return StopCopy;
	}
	
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
		if(CopyStopped == false && Wait <Timeout) {
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
		for (int i = 0; i < Dateiliste.length; i++) {
			if(StopCopy == true) {
				logger.finer(messages.getString("CopyCancelSuccess"));
				CopyStopped = true;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(Dateiliste[i].isFile()) {
				logger.fine(messages.getString("CopyingFile") + sourceDir + Dateiliste[i].getName()+ "...");
				String Save = Dateiliste[i].getName();
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
				File CreateSubDir = new File(outDir + Dateiliste[i].getName());
				CreateSubDir.mkdir();
				executeCopyDir(Dateiliste[i].toString() + "/", outDir + Dateiliste[i].getName() + "/");
			}
		}
		CopyingFiles = false;
	}
	
	/**
	 * Wird aufgerufen, wenn der Kopiervorgang erfolgreich abgeschlossen wurde
	 * @param outDir
	 * @param sourceDirMD5
	 * @param canUseGui
	 */
	private static void doWhenCopyDone(File outDir, String sourceDirMD5, boolean canUseGui) {
		
		if(calcCheckSumOnFinish) {
			CalcCheckSum Calc = new CalcCheckSum();
			logger.info(messages.getString("CheckSumCopiedFiles"));
			Calc.collectStreams(outDir);
			String OutDirMD5 = Calc.calcMD5HashForDir(outDir);
		
			if(OutDirMD5.equals(sourceDirMD5)) {
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
		File outDirRaw;
		File sourceDir;	
		File outDir;
		File[] savesList;
		String outDirString;
		//Init, da sonst Fehler(wird immer noch später gesetzt)
		String sourceDirMD5 = "";
		String oldDirMD5;
		int savesCount;
		//Init, da sonst Fehler(wird immer noch später gesetzt)
		int sourceDirFilesCount = -1;
		int saveDirFilesCount;
		
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
		
		CalcCheckSum Calc = new CalcCheckSum();

		if(calcCheckSumOldDir|| calcCheckSumOnFinish) {
			logger.info(messages.getString("CheckSumSourceDir"));
			sourceDirFilesCount = Calc.collectStreams(sourceDir);
			logger.finer(sourceDirFilesCount + messages.getString("FilesInSourceDir"));
			sourceDirMD5 = Calc.calcMD5HashForDir(sourceDir);
		}
		
		if(savesCount>=1 && calcCheckSumOldDir) {
			assert sourceDirMD5 != "";
			assert sourceDirFilesCount != -1;
			//Wir brauchen die Prüfsumme der alten Dateien erst gar nicht zu überprüfen, wenn die Ordner verschieden viele Dateien enthalten
			saveDirFilesCount = Calc.collectStreams(savesList[savesCount -1]);
			logger.finer(saveDirFilesCount + messages.getString("FilesInOutDir"));
			if(saveDirFilesCount != sourceDirFilesCount) {
				logger.info(messages.getString("SkipCheckSum"));
			}
			
			else {
				//Prüfsummenbrechnung des alten Verzeichnisses
				logger.info(messages.getString("CheckSumOldDir"));
				oldDirMD5 = Calc.calcMD5HashForDir(savesList[savesCount -1]);
				if(sourceDirMD5.equals(oldDirMD5)) {
					//Falls das Backup nicht erledigt werden musst, sollte dies erfüllt sein
					logger.info(messages.getString("SkipCopy"));
					if(canUseGui) {
						JOptionPane.showMessageDialog(null, messages.getString("SkipCopy"));
					}
					System.exit(0);
				}
			}
		}
		
		outDir.mkdir();
		logger.finer(messages.getString("CreatingDir") + outDir);
		logger.info(messages.getString("CopyStarted"));
		CopyMgr.executeCopyDir(SourceDirString, outDirString);
		doWhenCopyDone(outDir, sourceDirMD5, canUseGui);
	}
	
}