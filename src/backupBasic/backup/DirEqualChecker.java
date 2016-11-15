package backupBasic.backup;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Tobias Hotz
 */
public class DirEqualChecker
{
	private static final Logger logger = Logger.getLogger(DirEqualChecker.class.getName());
	private static final ResourceBundle messages = Main.getMessages();
	static {
		logger.setLevel(Level.ALL);
	}

	
	/**
	 * Muss vor printInfo aufgerufen werden, da calcMD5HashForDir die Dateien, die hier gefunden werden, benutzt
	 * Diese Methode ist nicht thread-safe
	 * @param dirToCollect
	 * @return fileStreamsLenght
	 */
	public Vector<File> listAllFiles(File dirToCollect) {
	    assert (dirToCollect.isDirectory());
	    Vector<File> fileVector = new Vector<File>();
		logger.finest(messages.getString("ListFiles"));
	    collectInputStreams(dirToCollect, fileVector);
	    return fileVector;
	}
	
	/**
	 * Checks if a dir is equal to another
	 * @return true if the entire dir is equal
	 */
	public boolean checkFiles(Vector<File> dirVec, Vector<File> dir2Vec) {
		for(int i = 0;i<dirVec.size();i++) {
			File dir = dirVec.get(i);
			File dir2 = dir2Vec.get(i);
			try {
				if (FileUtils.contentEquals(dir, dir2))
					return true;
			} catch (IOException e) {
				throw new RuntimeException(messages.getString("ReadErrorDir") + dir.getAbsolutePath(), e);
			}
		}
		return false;
	}

	private void collectInputStreams(File dir,
	                                 List<File> foundFiles) {

	    File[] fileList = dir.listFiles();
	    Arrays.sort(fileList,               // Need in reproducible order
				(f1, f2) -> f1.getName().compareTo(f2.getName()));

	    for (File f : fileList) {
	    	if (f.isDirectory()) {
	            collectInputStreams(f, foundFiles);
	        }
	        else {
				foundFiles.add(f);
	        }
	    }

	}
}