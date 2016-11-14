package backupBasic.backup;

/*
 * Grundgerüst von http://stackoverflow.com/questions/3010071/how-to-calculate-md5-checksum-on-directory-with-java-or-groovy
 * viele Modifikationen und Vereinfachungen von mir
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * @author Stuart Rossiter, Modfication by Tobias
 */
public class CalcCheckSum
{
	private static final Logger logger = Logger.getLogger(CalcCheckSum.class.getName());
	private static final ResourceBundle messages = Main.getMessages();
	static {
		logger.setLevel(Level.ALL);
	}
	
	private static Vector<FileInputStream> finishedInputStreams;
	private static File dir;
	
	/**
	 * Muss vor printInfo aufgerufen werden, da calcMD5HashForDir die Dateien, die hier gefunden werden, benutzt
	 * Diese Methode ist nicht thread-safe
	 * @param dirToCollect
	 * @return fileStreamsLenght
	 */
	public int collectStreams(File dirToCollect) {
	    assert (dirToCollect.isDirectory());
	    dir = dirToCollect;
	    Vector<FileInputStream> fileStreams = new Vector<FileInputStream>();
		logger.finest(messages.getString("ListFiles"));
	    collectInputStreams(dirToCollect, fileStreams);
	    int fileStreamsLength = fileStreams.toArray().length;
	    finishedInputStreams = fileStreams;
	    return fileStreamsLength;
	}
	
	/**
	 * Collect Stream hiervor aufrufen, selben Parameter übergeben
	 * Diese Methode ist nicht thread-safe
	 * @param dirToHash
	 * @return Prüfsumme als String
	 */
	public String calcMD5HashForDir(File dirToHash) {
		assert finishedInputStreams !=null;
		assert dir == dirToHash;
		Vector<FileInputStream> fileStreams = finishedInputStreams;
		//Null fileStreams after reading
		finishedInputStreams = null;
		
	    SequenceInputStream seqStream = new SequenceInputStream(fileStreams.elements());

	    try {
	        String md5Hash = DigestUtils.md5Hex(seqStream);
	        seqStream.close();
	        return md5Hash;
	    }
	    catch (IOException e) {
	        throw new RuntimeException(messages.getString("ReadErrorDir") + dirToHash.getAbsolutePath(), e);
	    }

	}

	private void collectInputStreams(File dir,
	                                 List<FileInputStream> foundStreams) {

	    File[] fileList = dir.listFiles();
	    Arrays.sort(fileList,               // Need in reproducible order
				(f1, f2) -> f1.getName().compareTo(f2.getName()));

	    for (File f : fileList) {
	    	if (f.isDirectory()) {
	            collectInputStreams(f, foundStreams);
	        }
	        else {
	            try {
	                foundStreams.add(new FileInputStream(f));
	            }
	            catch (FileNotFoundException e) {
	                throw new AssertionError(e.getMessage() + ": file should never not be found!");
	            }
	        }
	    }

	}
}