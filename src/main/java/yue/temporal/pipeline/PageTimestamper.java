package yue.temporal.pipeline;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import yue.temporal.page.*;
import yue.temporal.utils.CluewebFileProcess;
import yue.temporal.utils.FileProcess;

public class PageTimestamper {
	public static void timestamp (String folderPath_TargetPage, String rootFolderPath_HistoricalPage, String folderPath_TaggedPage, int threshold_Length, double threshold_Similarity) throws NoSuchAlgorithmException, IOException, BoilerpipeProcessingException {
    	//	1.	get the folder of target pages
    	//String folderPath_TargetPage = args[0];
    	File folder_TargetPage = new File(folderPath_TargetPage);
    	
    	//	2. 	get the root folder of historical pages
    	//String rootFolderPath_HistoricalPage = args[1];
    	File rootFolder_HistoricalPage = new File(rootFolderPath_HistoricalPage);
    	
		// select the feature file path
		//String folderPath_TaggedPage = args[2];
		File folder_TaggedPage = new File(folderPath_TaggedPage);
		if (!folder_TaggedPage.exists() || !folder_TaggedPage.isDirectory())
			folder_TaggedPage.mkdir();
		
		File file_TargetPageFeatures = new File(folder_TaggedPage, "TargetPageFeatures");
        
    	//	3. 	process each file in the folder of target pages
    	File[] fileList_TargetPage = folder_TargetPage.listFiles();
    	for (File file_TargetPage: fileList_TargetPage) {

    		//	TO MAKE SURE THE FILE IS THE CLUEWEB PAGE WE NEED
    		if (!file_TargetPage.getName().startsWith("clueweb12"))
    			continue;
    		
    		//	3.0	output the signal to screen
    		System.out.println();
			System.out.println( "Start: " + file_TargetPage.getAbsolutePath());
    	
    		//	3.1	process the file to get path, url, timestamp
    		String filePath_TargetPage = file_TargetPage.getAbsolutePath();
    		String url = CluewebFileProcess.readURLFromCluewebFile(filePath_TargetPage);

    		//		implement the function to extract the timestamp
    		String timestamp_TargetPage = CluewebFileProcess.readTimeFromCluewebFile(filePath_TargetPage);
    		
    		//	3.2 generate TargetPage targetPage(path, url, timestamp)
    		TargetPage targetPage = new TargetPage(filePath_TargetPage, url, timestamp_TargetPage, threshold_Length, threshold_Similarity);
    		
    		//	3.3	find the historical folder of the target page, if not, continue, else    		
			File folder_HistoricalPage = new File(rootFolder_HistoricalPage, targetPage.MD5Code);		    		
    		    		
    		//File folder_HistoricalPage = new File(folderPath_HistoricalPage);
    		if (!folder_HistoricalPage.exists())
    			continue;
    		else { 		
    			//	3.3.1	for each file in the historical folder
    			File[] fileList_HistoricalPage = folder_HistoricalPage.listFiles();
    			
    			for (File file_HistoricalPage: fileList_HistoricalPage) {
    				//  For the html file
    				//	String Suffix_HistoricalPage = file_HistoricalPage.getName().substring(file_HistoricalPage.getName().lastIndexOf(".")+1);
    				//	Suffix_HistoricalPage.endsWith("html");
    			
    				//	Make sure the file we check is a historical file
    				if (!file_HistoricalPage.getName().endsWith("html"))
    					continue;
    				
    				//	3.3.1.1	process to get path, timestamp
    				String filePath_HistoricalPage = file_HistoricalPage.getAbsolutePath();
    				//		for historical page, their time stamp = their name
    				//		change the format of the timestamp to year-month-day
    				String timeTemp = file_HistoricalPage.getName().substring(0, 4) + "-"
    								+ file_HistoricalPage.getName().substring(4, 6) + "-"
    								+ file_HistoricalPage.getName().substring(6, 8);
    				String timestamp_HistoricalPage = timeTemp;
    				
    				//	3.3.1.2	generate HistoricalPage historicalPage(path, url, timestamp)
    				HistoricalPage historicalPage = new HistoricalPage(filePath_HistoricalPage, url, timestamp_HistoricalPage, threshold_Length);   				
    	
    				//	3.3.1.3	targetPage.pageTag(historicalPage, threshold_Similarity)
    				targetPage.pageTag(historicalPage, threshold_Similarity);
    			}
    		
    			//	3.3.2	output the features of targetPage to the feature file
    			String feature_TargetPage = targetPage.featureToString();
    			FileProcess.addLinetoaFile(feature_TargetPage, file_TargetPageFeatures.getAbsolutePath());
    			
    			//	3.3.3	output the tagged target page to a file
    			String filename_TaggedTargetPage = file_TargetPage.getName() + "_" + targetPage.MD5Code;
    			File file_TaggedTargetPage = new File (folder_TaggedPage, filename_TaggedTargetPage);
    			targetPage.toFile(file_TaggedTargetPage);
    	
    			//	3.3.4	output the signal to screen
    			System.out.println( "Finish: " + file_TargetPage.getAbsolutePath());
    		}    	
    	}
	}
}
