package yue.temporal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CluewebFileProcess {

	/**
	 * readURLFromCluewebFiles
	 * @param folder
	 * @return List<String> listURLS
	 * @throws IOException
	 */	
	public static List<String> readURLFromCluewebFolder(final File folder) throws IOException{
		//	Get the list of files in the folder.
		List<String> listFilePath = new ArrayList<String>();
		
		//	Use to store URLs extracted from the files
		List<String> listURLS = new ArrayList<String>();
		
		listFilePath = FileProcess.listFilesForFolder(folder);
		for (String filePath: listFilePath){
			listURLS.add(readURLFromCluewebFile(filePath));
		}
		
		return listURLS;
	}
	
	/**
	 * readURLFromCluewebFiles
	 * @param sourceFolder
	 * @return List<String> listURLS
	 * @throws IOException
	 */
	public static List<String> readURLFromCluewebFolder(String sourceFolder) throws IOException{
		final File sourceFolderPath = new File(sourceFolder);
		return readURLFromCluewebFolder(sourceFolderPath);
	}
	
	public static String readURLFromCluewebFile(String filePath) throws IOException {
		String url = "";
		
		//	Read the file line by line to find the URL
		InputStream fis;
		BufferedReader br;
		String line;
		
		fis = new FileInputStream(filePath);
		br = new BufferedReader(new InputStreamReader(fis));
		
		while ((line = br.readLine()) != null) {
		    //	Deal with the line
			if ((line.contains("WARC-Target-URI:"))&&(line.length() > 20)) {
				url = line.substring(17);
				break;
			}
		}

		//	Done with the file
		br.close();
		br = null;
		fis = null;
		
		return url;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static String readTimeFromCluewebFile(String filePath_TargetPage) throws IOException {
		
		String timestamp = "";
		
		//	Read the file line by line to find the URL
		InputStream fis;
		BufferedReader br;
		String line;
		
		fis = new FileInputStream(filePath_TargetPage);
		br = new BufferedReader(new InputStreamReader(fis));
		
		while ((line = br.readLine()) != null) {
		    //	Deal with the line to find the time stamp of a file in Clueweb 12
			if ((line.contains("WARC-Date:"))&&(line.length() > 15)) {
				timestamp = line.substring(11, 21);
				break;
			}
		}

		//	Done with the file
		br.close();
		br = null;
		fis = null;
		
		return timestamp;
	}

}
