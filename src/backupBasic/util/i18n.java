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
    private static ResourceBundle messages, fallback;

    public static Logger getLogger(@NotNull Class c) {
        Logger log = Logger.getLogger(c.getName());
        log.setLevel(Level.ALL);
        return log;
    }

    public static String translate(@NotNull String string) {
        try {
            return messages.getString(string);
        }
        catch (MissingResourceException e) {
            return fallback.getString(string);
        }
    }

    /**
     * Verwaltet das Logging-System
     */
    public static void initLogging() {
        logger.setLevel(Level.ALL);
        Locale currentLocale;
        String userLanguage, userCountry, userdir;
        Handler fileHandler;

        userdir = System.getProperty("user.home");
        userCountry = System.getProperty("user.country");
        userLanguage = System.getProperty("user.language");

        currentLocale = new Locale(userLanguage, userCountry);
        //If this isn't found, we can't continue as no fallback is defined
        fallback = ResourceBundle.getBundle("BackupBasic", new Locale("en", "US"));
        try {
            messages = ResourceBundle.getBundle("BackupBasic", currentLocale);
        }
        catch(MissingResourceException e) {
            System.out.println("Fallback: English");
            messages = fallback;
        }

        try {
            if(System.getProperty("os.name").startsWith("Windows")) {
                //Ordner erstellen
                new File(userdir + "/AppData/Local/BackupBasic").mkdirs();
                fileHandler = new FileHandler(userdir + "/AppData/Local/BackupBasic/LogBackupBasic.xml");
            }
            else {
                //Ordner erstellen
                new File(userdir + "BackupBasic").mkdirs();
                fileHandler = new FileHandler(userdir+ "/BackupBasic/LogBackupBasic.xml");
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
