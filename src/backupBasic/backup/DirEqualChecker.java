package backupBasic.backup;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import backupBasic.util.i18n;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * @author Tobias Hotz
 */
public class DirEqualChecker
{
	private static final Logger logger = i18n.getLogger(DirEqualChecker.class);
	
	/**
	 * Muss vor printInfo aufgerufen werden, da calcMD5HashForDir die Dateien, die hier gefunden werden, benutzt
	 * Diese Methode ist nicht thread-safe
	 * @param dirToCollect
	 * @return fileStreamsLenght
	 */
	public List<File> listAllFiles(File dirToCollect) {
	    assert (dirToCollect.isDirectory());
	    List<File> fileVector = new Vector<File>();
		logger.finest(i18n.translate("ListFiles"));
	    collectInputStreams(dirToCollect, fileVector);
	    return fileVector;
	}
	
	/**
	 * Checks if a dir is equal to another
	 * @return true if the entire dir is equal
	 */
	public boolean checkFiles(List<File> dirVec, List<File> dir2Vec) {
		for(int i = 0;i<dirVec.size();i++) {
			File dir = dirVec.get(i);
			File dir2 = dir2Vec.get(i);
			try {
				if (FileUtils.contentEquals(dir, dir2))
					return true;
			} catch (IOException e) {
				throw new RuntimeException(i18n.translate("ReadErrorDir") + dir.getAbsolutePath(), e);
			}
		}
		return false;
	}

	private void collectInputStreams(File dir,
	                                 List<File> foundFiles) {

	    File[] fileList = dir.listFiles();
	    Arrays.sort(fileList,               // Need in reproducible order
                Comparator.comparing(File::getName));

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