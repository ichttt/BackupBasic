package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */
import java.awt.GraphicsEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
/**
 * @author Tobias Hotz
 */
public class CopyManager {
	private static final Logger logger = Logger.getLogger(CopyManager.class.getName());
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
				logger.finer("Letzte Datei wurde erfolgreich kopiert");
				CopyStopped = true;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(Dateiliste[i].isFile()) {
				logger.fine("Kopiere Datei " + sourceDir + Dateiliste[i].getName()+ "...");
				String Save = Dateiliste[i].getName();
				File Source = new File(sourceDir + Save);
				File Out = new File(outDir + Save);
				try {
					Files.copy(Source.toPath(), Out.toPath());
				}
				catch (IOException e) {
					logger.severe("Fehler beim Kopieren der Dateien, breche ab!");
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
			logger.info("Berechne Prüfsumme der kopierten Dateien");
			Calc.collectStreams(outDir);
			String OutDirMD5 = Calc.calcMD5HashForDir(outDir);
		
			if(OutDirMD5.equals(sourceDirMD5)) {
				logger.info("Prüfsummen der kopierten Dateien stimmen überein.");
			}
			else {
				logger.severe("Prüfsummen der kopierten Dateien stimmen nicht überein!");
				if(canUseGui) {
					JOptionPane.showMessageDialog(null, "Die Kopierten Dateien sind Fehlerhaft! Bitte versuchen Sie es erneut!", "Prüfsummenfehler" ,JOptionPane.ERROR_MESSAGE);
				}
				System.exit(954);
			}
		}
		logger.info("Backup erfolgreich erstellt!");
		if(canUseGui) {
			JOptionPane.showMessageDialog(null, "Backup erfolgreich erstellt!");
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
		long timeTakenCollectStreamsStart;
		float timeTakenTotal;
		long timeTakenCalcMD5;
		
		outDirRaw = new File(OutDirRawString);
		sourceDir = new File(SourceDirString);	
		outDirString = outDirRaw +"/" + time + "/";	
		outDir = new File(outDirString);
		
		if(!sourceDir.exists()||!sourceDir.isDirectory()) {
			logger.warning("Quellverzeichnis nicht gefunden, Abbruch!");
			if(canUseGui) {
				JOptionPane.showMessageDialog(null, "Quellverzeichnis nicht gefunden, Abbruch!", "Verzeichnis nicht gefunden" , JOptionPane.WARNING_MESSAGE);
			}
			System.exit(0);
		}
		logger.fine("Das Quellverzeichnis ist "+ sourceDir);
		logger.fine("Das Zielverzeichnis ist " + outDirRaw);
		
		//Versuche, Save Verzeichnis zur Erstellen. Falls es schon vorhanden ist, geschieht nichts
		outDirRaw.mkdir();
		
		if(outDir.exists()) {
			logger.warning("Save wurde in der letzen Minute schon erstellt");
			if(canUseGui) {
				JOptionPane.showMessageDialog(null, "Save wurde in der letzen Minute schon erstellt");
			}
			System.exit(0);
		}
		
		savesList = outDirRaw.listFiles();
		savesCount = savesList.length;
		
		logger.info("Berechne Prüfsumme der Quelldateien");
		CalcCheckSum Calc = new CalcCheckSum();

		if(calcCheckSumOldDir|| calcCheckSumOnFinish) {
			timeTakenCollectStreamsStart = System.nanoTime();
			sourceDirFilesCount = Calc.collectStreams(sourceDir);
			timeTakenTotal = System.nanoTime() - timeTakenCollectStreamsStart;
			logger.finer(sourceDirFilesCount + " Dateien im Quellverzeichnis");
			logger.fine("collectStreams dauerte " + timeTakenTotal/1000000000 + " Sekunden");


			timeTakenCalcMD5 = System.nanoTime();
			sourceDirMD5 = Calc.calcMD5HashForDir(sourceDir);
			timeTakenTotal = System.nanoTime() - timeTakenCalcMD5;
			logger.fine("calcMD5HashForDir dauerte " + timeTakenTotal/1000000000 + " Sekunden");
		}
		
		if(savesCount>=1 && calcCheckSumOldDir) {
			assert sourceDirMD5 != "";
			assert sourceDirFilesCount != -1;
			//Wir brauchen die Prüfsumme der alten Dateien erst gar nicht zu überprüfen, wenn die Ordner verschieden viele Dateien enthalten
			saveDirFilesCount = Calc.collectStreams(savesList[savesCount -1]);
			logger.finer(saveDirFilesCount + " Dateien im altem Backup");
			if(saveDirFilesCount != sourceDirFilesCount) {
				logger.info("Überspringe Berechnung der Prüfsummen des alten Backups. Grund: Anzahl der Dateien ist unterschiedlich");
			}
			
			else {
				//Prüfsummenbrechnung des alten Verzeichnisses
				logger.info("Berechne Prüfsumme des alten Backups");
				oldDirMD5 = Calc.calcMD5HashForDir(savesList[savesCount -1]);
				logger.fine("Berechnete Prüfsumme für altes Verzeichnis: " + oldDirMD5);
				logger.fine("Berechnete Prüfsumme für neues Verzeichnis: " + sourceDirMD5);
				if(sourceDirMD5.equals(oldDirMD5)) {
					//Falls das Backup nicht erledigt werden musst, sollte dies erfüllt sein
					logger.info("Kein Backup erstellt, da alle Dateien identisch waren!");
					if(canUseGui) {
						JOptionPane.showMessageDialog(null, "Kein Backup erstellt, da alle Dateien identisch waren!");
					}
					System.exit(0);
				}
			}
		}
		
		outDir.mkdir();
		logger.finer("Erstelle Verzeichnis: " + outDir);
		logger.info("Starte Kopieren");
		CopyMgr.executeCopyDir(SourceDirString, outDirString);
		doWhenCopyDone(outDir, sourceDirMD5, canUseGui);
	}
	
}