package edu.jach.qt.utils;


import java.io.*;
import java.lang.*;
import java.util.*;


public class LockFile
{

    private static boolean _exists;
    private static String  _owner;
    private static String tmpFileDirName = 
	File.separator +
	System.getProperty("telescope") + 
	"data" +
	File.separator +
	System.getProperty("deferredDir");
    private static String lockFileDirName = tmpFileDirName.toLowerCase();
    private static File lockFileDir = new File (lockFileDirName);

    private LockFile()
    {
	_exists = false;
	_owner  = "";
	File lockFileDir = new File (lockFileDirName);
	if (!lockFileDir.exists()) {
	    lockFileDir.mkdir();
	}
	else if (lockFileDir.isDirectory() &&
		 lockFileDir.canRead() ) {
	    String [] fileList = lockFileDir.list();
	    // Now loop over hidden files looking for one
	    // starting with .lock
	    for (int fileCounter=0; fileCounter<fileList.length; fileCounter++) {
		if (fileList[fileCounter].startsWith(".lock")) {
		    _exists = true;
		    StringTokenizer st = new StringTokenizer(fileList[fileCounter],
							     "_");
		    while (st.hasMoreTokens()) {
			_owner = st.nextToken();
		    }
		}
	    }
	}
    }

    public static boolean exists() {
	LockFile l = new LockFile();
	return l._exists;
    }

    public static String owner() {
	LockFile l = new LockFile();
	return _owner;
    }

    public static void createLock() {
	String lockFileName = lockFileDirName+
	    File.separator+
	    ".lock_"+
	    System.getProperty("user.name");
	File lockFile = new File(lockFileName);
	try {
	    lockFile.createNewFile();
	}
	catch (IOException ioe) {System.out.println("Unable to create lock file "+lockFileName);}
	lockFile.deleteOnExit();
    }
}
