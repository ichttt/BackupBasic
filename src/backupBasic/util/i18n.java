package backupBasic.util;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import backupBasic.backup.Main;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tobias Hotz
 */
public class i18n {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static ResourceBundle messages;

    public static Logger getLogger(@NotNull Class c) {
        Logger log = Logger.getLogger(c.getName());
        log.setLevel(Level.ALL);
        return log;
    }

    public static String translate(@NotNull String string) {
        return messages.getString(string);
    }

    /**
     * Verwaltet das Logging-System
     */
    public static void initLogging() {
        logger.setLevel(Level.ALL);
        Locale currentLocale;
        String userLanguage, userCountry;
        Handler fileHandler;

        userCountry = System.getProperty("user.country");
        userLanguage = System.getProperty("user.language");

        currentLocale = new Locale(userLanguage, userCountry);
        try {
            messages = ResourceBundle.getBundle("backupBasic\\BackupBasic", currentLocale);
        }
        catch(MissingResourceException e) {
            System.out.println("Fallback: English");
            messages = ResourceBundle.getBundle("backupBasic\\BackupBasic", new Locale("en", "US"));
        }

        try {
            if(System.getProperty("os.name").startsWith("Windows")) {
                //Ordner erstellen
                new File(System.getProperty("user.home") + "/AppData/Local/BackupBasic").mkdirs();
                fileHandler = new FileHandler(System.getProperty("user.home") + "/AppData/Local/BackupBasic/LogBackupBasic.xml");
            }
            else {
                //Ordner erstellen
                new File(System.getProperty("user.home") + "BackupBasic").mkdirs();
                fileHandler = new FileHandler(System.getProperty("user.home")+ "/BackupBasic/LogBackupBasic.xml");
            }
            //Fügt FileHandler global hinzu
            logger.getParent().addHandler(fileHandler);
        }
        catch (IOException e) {
            logger.severe(messages.getString("LogError"));
            e.printStackTrace();
        }
        logger.info(messages.getString("LogStart"));
    }

}
