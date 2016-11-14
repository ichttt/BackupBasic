package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import backupBasic.gui.GuiCreator;

/**
 * @author Tobias Hotz
 */
public class ArgParser {	
	
	//TODO Jopt-simple implementieren
	private static final Logger logger = Logger.getLogger(ArgParser.class.getName());
	private static final ResourceBundle messages = Main.getMessages();
	static {
		logger.setLevel(Level.ALL);
	}
	
	/**
	 * Fügt, falls nötig, ein Slash an das Ende eines Strings
	 */
	public static String setLastCharSlash(String check) {
		if(!check.endsWith("/") && !check.endsWith("\\")) {
			check = check + "/";
		}
		return check;
	}
	
	/**
	 * Verwaltet Kommandozeilenargumente und gibt an GUICreator oder CopyManager ab
	 * @param args Argumente zum Parsen
	 */
	public static void parseArgs(String[] args) {
		final boolean CanUseGui = !GraphicsEnvironment.isHeadless();
		//Standardpfade für Anno
		String SourceDir = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\saves\\";
		String OutDirRaw = System.getProperty("user.home") + "\\Desktop\\BackupMinecraft\\";
		
		//Argumente verwalten und Unterfunktionen aufrufen
		switch(args.length) {		
		
		//Quellverzeichnis, Zielverzeichnis, ChecksumOldDir, ChecksumFinished
		case 4:
			if(args[3].equals("false")) {
				CopyManager.calcCheckSumOnFinish = false;
			}
			else if(args[3].equals("true")) {
				//Eigentlich nicht nötig
				CopyManager.calcCheckSumOnFinish = true;
			}
			else {
				logger.severe(messages.getString("Usage"));
				System.exit(0);
			}
		//Quellverzeichnis, Zielverzeichnis, ChecksumOldDir
		case 3:
			if(args[2].equals("false")) {
				CopyManager.calcCheckSumOldDir = false;
			}
			else if(args[2].equals("true")) {
				//Eigentlich nicht nötig
				CopyManager.calcCheckSumOnFinish = true;
			}
			else {
				logger.severe(messages.getString("Usage"));
				System.exit(0);
			}
		//Quellverzeichnis, Zielverzeichnis
		case 2:
			if(!args[1].equals("default")) {
				SourceDir = setLastCharSlash(args[1]);
			}
			else {
				logger.fine(messages.getString("DefaultOut"));
			}
		case 1:
			//Quellverzeichnis
			if(!args[0].equals("default")) {
				SourceDir = setLastCharSlash(args[0]);
			}
			else {
				logger.fine(messages.getString("DefaultSource"));
			}
			CopyManager.copyDir(OutDirRaw, SourceDir);
			break;
			
		case 0:
			if(CanUseGui){
				GuiCreator.createGui(OutDirRaw, SourceDir);
			}
			else {
				logger.severe(messages.getString("NotEnoughArgs"));
				logger.severe(messages.getString("Usage"));
			}
			break;
			
		//Zu viele Parameter
		default:
			logger.severe(messages.getString("Usage"));
			break;
		}
		
		
	}
}