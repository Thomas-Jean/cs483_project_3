package src;

import java.io.FilenameFilter;
import java.io.File;


public class NoTags implements FilenameFilter {
	     
	    @Override
	    public boolean accept(File directory, String fileName) {
	        if (!fileName.endsWith(".tag")) {
	         	 return true;
	        }
	        return false;
	    }
	}
