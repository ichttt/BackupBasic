package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import backupBasic.gui.GuiCreator;
import backupBasic.util.i18n;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.awt.*;
import java.util.logging.Logger;

/**
 * @author Tobias Hotz
 */
public class ArgParser {	
	
	//TODO Jopt-simple implementieren
	private static final Logger logger = i18n.getLogger(ArgParser.class);
	
	/**
	 * Fügt, falls nötig, ein Slash an das Ende eines Strings
	 */
	public static String setLastCharSlash(@NotNull String check) {
		if(!check.endsWith("/") && !check.endsWith("\\")) {
			check = check + "/";
		}
		return check;
	}
	
	/**
	 * Verwaltet Kommandozeilenargumente und gibt an GUICreator oder CopyManager ab
	 * @param args Argumente zum Parsen
	 */
	public static void parseArgs(@Nullable String[] args) {
		final boolean CanUseGui = !GraphicsEnvironment.isHeadless();
		//Standardpfade für Anno
		String SourceDir = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\saves\\";
		String OutDirRaw = System.getProperty("user.home") + "\\Desktop\\BackupMinecraft\\";

		if(args==null) {
            if (CanUseGui) {
                GuiCreator.createGui(OutDirRaw, SourceDir);
            } else {
                logger.severe(i18n.translate("NotEnoughArgs"));
                logger.severe(i18n.translate("Usage"));
            }
            return;
        }
		//Argumente verwalten und Unterfunktionen aufrufen
		switch(args.length) {		
		
		//Quellverzeichnis, Zielverzeichnis, ChecksumOldDir, ChecksumFinished
		case 4:
			if(args[3].equals("false")) {
				CopyManager.checkFilesOnFinish = false;
			}
			else if(args[3].equals("true")) {
				//Eigentlich nicht nötig
				CopyManager.checkFilesOnFinish = true;
			}
			else {
				logger.severe(i18n.translate("Usage"));
				System.exit(0);
			}
		//Quellverzeichnis, Zielverzeichnis, ChecksumOldDir
		case 3:
			if(args[2].equals("false")) {
				CopyManager.checkFilesOldDir = false;
			}
			else if(args[2].equals("true")) {
				//Eigentlich nicht nötig
				CopyManager.checkFilesOnFinish = true;
			}
			else {
				logger.severe(i18n.translate("Usage"));
				System.exit(0);
			}
		//Quellverzeichnis, Zielverzeichnis
		case 2:
			if(!args[1].equals("default")) {
				SourceDir = setLastCharSlash(args[1]);
			}
			else {
				logger.fine(i18n.translate("DefaultOut"));
			}
		case 1:
			//Quellverzeichnis
			if(!args[0].equals("default")) {
				SourceDir = setLastCharSlash(args[0]);
			}
			else {
				logger.fine(i18n.translate("DefaultSource"));
			}
			CopyManager.copyDir(OutDirRaw, SourceDir);
			break;
			
		case 0:
			if(CanUseGui){
				GuiCreator.createGui(OutDirRaw, SourceDir);
			}
			else {
				logger.severe(i18n.translate("NotEnoughArgs"));
				logger.severe(i18n.translate("Usage"));
			}
			break;
			
		//Zu viele Parameter
		default:
			logger.severe(i18n.translate("Usage"));
			break;
		}
		
		
	}
}