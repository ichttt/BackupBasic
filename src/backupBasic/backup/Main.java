package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import backupBasic.util.i18n;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * @author Tobias Hotz
 */
public class Main implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = i18n.getLogger(Main.class);
	/**
	 * Verwaltet das Logging-System und gibt dann an den ArgParser ab
	 */
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new Main());
        i18n.initLogging();
        ArgParser.parseArgs(args);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.severe(i18n.translate("UncaughtException")+ "\t Stacktrace:");
		e.printStackTrace();
		if(!GraphicsEnvironment.isHeadless()) {
			JOptionPane.showMessageDialog(null, i18n.translate("UncaughtException") + "\n" + i18n.translate("Error") + ": "+ e +"\n" +  i18n.translate("Details"), "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		System.exit(-1);
	}
}