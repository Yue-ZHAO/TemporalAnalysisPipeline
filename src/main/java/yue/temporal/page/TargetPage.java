package yue.temporal.page;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wcohen.ss.JaroWinklerTFIDF;
import yue.temporal.utils.CluewebFileProcess;
import yue.temporal.utils.FileProcess;
import yue.temporal.utils.HTMLParser;

//import de.l3s.boilerpipe.BoilerpipeProcessingException;

public class TargetPage extends Page {
	
	// for string compare
	static JaroWinklerTFIDF distanceJaroWinklerTFIDF = new JaroWinklerTFIDF();
	
	//	order the time stamps from min to max
	public List<String> orderedTimestamps = new ArrayList<String>();
	
	//	number of different time stamps
	public int num_Timestamps;
	
	//	earliest time stamp
	public String earliestTimestamp;
	
	//	latest time stamp
	public String latestTimestamp;
	
	//	the name of the original target file
	public String filename_TargetPage;
	
	//	implement the Class Paragraph
	public List<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	public int threshold_length = 50;
	
	public double threshold_similarity = 0.7;
	
	public TargetPage(String absPath_Page, String urlString, String timestamp, int lenThreshold, double simThreshold) throws NoSuchAlgorithmException, IOException {
		File file_historicalPage = new File(absPath_Page);
		pageInit(file_historicalPage, urlString, timestamp, lenThreshold, simThreshold);
	}
	
	public TargetPage(File file_Page, String urlString, String timestamp, int lenThreshold, double simThreshold) throws NoSuchAlgorithmException, IOException {
		pageInit(file_Page, urlString, timestamp, lenThreshold, simThreshold);
	}
	
	private void pageInit(File file_Page, String urlString, String timestamp, int lenThreshold, double simThreshold) throws NoSuchAlgorithmException, IOException {
		
		this.URL = urlString;
		this.currentTimestamp = timestamp;
		this.MD5Code = FileProcess.stringTrans_MD5(urlString);
		this.filename_TargetPage = file_Page.getName();
		
		this.threshold_length = lenThreshold;
		this.threshold_similarity = simThreshold;
		
		List<String> ps_FromHTML = HTMLParser.getPfromHTML_br2nl(file_Page, lenThreshold);
		//	1. self compare to filter meaningless or repeated paragraphs
		List<String> ps_Filtered = psFilter_JaroWinklerTFIDF(ps_FromHTML, simThreshold);
		
		//	2. init List<Paragraphs>
		//		for each paragraph, find the position of it in the original file
		for (String p_Filtered: ps_Filtered) {
			
			Paragraph paragraph = new Paragraph();
			int startPos = HTMLParser.findPosFromHTML_br2nl(p_Filtered, file_Page);
			if (startPos != -1) {
				paragraph.setContent(p_Filtered);
				paragraph.setStartPoint(startPos);
				paragraph.setEndPoint(startPos + p_Filtered.length() - 1);
				paragraph.setTimestamp(timestamp);
				paragraphs.add(paragraph);
			}
		}
		
		this.num_Paragraphs = paragraphs.size();
	}
	
	public List<String> psFilter_JaroWinklerTFIDF (List<String> orgPs, double simThreshold) {
		List<String> filPs = new ArrayList<String>();
		for (String p_FromOrg: orgPs) {
			//	self compare to filter meaningless strings 
			double score = distanceJaroWinklerTFIDF.score(p_FromOrg, p_FromOrg);
			//	filter repeated strings and meaningless strings
			if (score > simThreshold && !filPs.contains(p_FromOrg))
				filPs.add(p_FromOrg);
		}
		return filPs;
	}
	
	public String featureToString() {
		
		//	Calculate the time stamps and find the real value of these features
		featureUpDate();
		
		//	output the feature
		String feature;
		feature = this.num_Paragraphs + " " 
				+ this.num_Timestamps + " "
				+ this.earliestTimestamp + " "
				+ this.latestTimestamp + " "
				+ this.MD5Code;
		return feature;
	}
	
	public int pageTag (HistoricalPage historicalPage, double simThreshold) {
		
		int flag = 0;
		//	1.	Compare the time stamps: if historical page> target page, return 0
		if (this.currentTimestamp.compareTo(historicalPage.currentTimestamp) <= 0) {
			return 0;
		} else {
			for(Paragraph targetPagePara: this.paragraphs) {
				//	2. 	if the paragraph time stamp earlier than the historical page
				//		we do not to compare this paragraph for this historical page
				if (targetPagePara.getTimestamp().compareTo(historicalPage.currentTimestamp) <= 0)
					continue;
				else {
					String paraString_Target = targetPagePara.getContent();
					for (String paraString_History: historicalPage.paragraphs) {
						double score = distanceJaroWinklerTFIDF.score(paraString_Target, paraString_History);
						if (score >= simThreshold) {
							targetPagePara.setTimestamp(historicalPage.currentTimestamp);
							flag = 1;
						}
					}
				}
			}		
		}
		return flag;
	}
	
	public void toFile(String filePath) throws IOException {
		//	store the targetPage to file
		File file_TaggedTargetPage = new File(filePath);
		toFile(file_TaggedTargetPage);
		
	}

	private void featureUpDate() {
		// Calculate the time stamps and find the real value of these features
		if (paragraphs.size() == 0) {
			
			num_Timestamps = 0;
			earliestTimestamp = "0";
			latestTimestamp = "0";
			
		} else {
			
			for(Paragraph paragraph: paragraphs) {
				String pTimestamps = paragraph.getTimestamp();
				if (!orderedTimestamps.contains(pTimestamps))
					orderedTimestamps.add(pTimestamps);
			}
			Collections.sort(orderedTimestamps);
		
			num_Timestamps = orderedTimestamps.size();
			earliestTimestamp = orderedTimestamps.get(0);
			latestTimestamp = orderedTimestamps.get(num_Timestamps-1);
		}
	}

	public void toFile(File file_TaggedTargetPage) throws IOException {
		
		featureUpDate();
		//	write the file line by line
		String filePath_TaggedTargetPage = file_TaggedTargetPage.getAbsolutePath();
		
		if (file_TaggedTargetPage.exists()) {
			file_TaggedTargetPage.delete();
			file_TaggedTargetPage.createNewFile();
		}
				
		//	URL
		FileProcess.addLinetoaFile("#URL: " + this.URL, filePath_TaggedTargetPage);
		
		//	MD5 CODE
		FileProcess.addLinetoaFile("#MD5: " + this.MD5Code, filePath_TaggedTargetPage);
		
		//	CURRENT TIMESTAMP
		FileProcess.addLinetoaFile("#Page Timestamp: " + this.currentTimestamp, filePath_TaggedTargetPage);
		
		//	ORIGINAL FILE NAME
		FileProcess.addLinetoaFile("#Original File: " + this.filename_TargetPage, filePath_TaggedTargetPage);
		
		//	LENGTH THRESHOLD
		FileProcess.addLinetoaFile("#Length Threshold: " + this.threshold_length, filePath_TaggedTargetPage);
		
		//	SIMILARITY THRESHOLD
		FileProcess.addLinetoaFile("#Similarity Threshold: " + "JaroWinklerTFIDF " + this.threshold_similarity, filePath_TaggedTargetPage);
		
		//	NUM OF TIMESTAMPS
		FileProcess.addLinetoaFile("#Number of Paragraph Timestamps: " + this.num_Timestamps, filePath_TaggedTargetPage);
		
		//	LIST OF TIMESTAMPS
		String listTimestamps = "";
		for (String temp_Timestamps: orderedTimestamps) {
			listTimestamps = listTimestamps + temp_Timestamps + " ";
		}
		FileProcess.addLinetoaFile("#List of Paragraph Timestamps: " + listTimestamps, filePath_TaggedTargetPage);
		
		//	NUM OF PARAGRAPHS
		FileProcess.addLinetoaFile("#Number of Paragraphs: " + this.num_Paragraphs, filePath_TaggedTargetPage);
		
		//	PARA WITH TIME LINE BY LINE
		FileProcess.addLinetoaFile("#Timestamps of Paragraphs (Character Count, Timestamps, Content)", filePath_TaggedTargetPage);
		for(Paragraph paragraph: paragraphs) {
			FileProcess.addLinetoaFile(paragraph.getStartPoint() + "-" + paragraph.getEndPoint() + "\t" 
					+ paragraph.getTimestamp() + "\t" + paragraph.getContent(), filePath_TaggedTargetPage);
		}		
	}
	
	//	Main for test
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		//	Test for construct
		//	1.	get the folder of target pages
    	String folderPath_TargetPage = args[0];
    	System.out.println(folderPath_TargetPage);
    	File folder_TargetPage = new File(folderPath_TargetPage);
    	//	System.out.println(folder_TargetPage.getName());
    	
    	String rootFolderPath_HistoricalPage = args[1];
    	System.out.println(rootFolderPath_HistoricalPage);
    	File rootFolder_HistoricalPage = new File(rootFolderPath_HistoricalPage);
        	
    	//  2. 	process each file in the folder of target pages
    	File[] fileList_TargetPage = folder_TargetPage.listFiles();
    	int i = 0;
    	for (File file_TargetPage: fileList_TargetPage) {
    		
    		//	2.1	process the file to get path, url, timestamp
    		String filePath_TargetPage = file_TargetPage.getAbsolutePath();
    		System.out.println(filePath_TargetPage);
    		String url = CluewebFileProcess.readURLFromCluewebFile(filePath_TargetPage);
    		System.out.println(url);
    		//		implement the function to extract the timestamp
    		String timestamp_TargetPage = CluewebFileProcess.readTimeFromCluewebFile(filePath_TargetPage);
    		System.out.println(timestamp_TargetPage);
    		
    		//	2.2 generate TargetPage targetPage(path, url, timestamp)
    		TargetPage targetPage = new TargetPage(filePath_TargetPage, url, timestamp_TargetPage, 50, 0.7);
    	
    		//	2.3	output the construct result
    		System.out.println("URL: " + targetPage.URL);
    		System.out.println("MD5: " + targetPage.MD5Code);
			System.out.println("Timestamp: " + targetPage.currentTimestamp);
			System.out.println("File Name: " + targetPage.filename_TargetPage);
			System.out.println("Num of Paragraph(s): " + targetPage.num_Paragraphs);
			
			//	2.4 Test For Compare
			File folder_HistoricalPage = new File(rootFolder_HistoricalPage, targetPage.MD5Code);		    		
    		String folderPath_HistoricalPage = folder_HistoricalPage.getAbsolutePath();
    		System.out.println(folderPath_HistoricalPage);
    		
    		if (!folder_HistoricalPage.exists()) {
    			System.out.println("Not find!");
    			i++;
    			System.out.println("------------------------" + i + "--------------------------");
    			continue;
    		}
    		else {
    			System.out.println("Find: " + folderPath_HistoricalPage);
    			//	TAG
    			File[] fileList_HistoricalPage = folder_HistoricalPage.listFiles();
    			for (File file_HistoricalPage: fileList_HistoricalPage) {
    				//  For the html file
    				String Suffix_HistoricalPage = file_HistoricalPage.getName().substring(file_HistoricalPage.getName().lastIndexOf(".")+1);
    				
    				//	Make sure the file we check is a historical file
    				if (!Suffix_HistoricalPage.equals("html"))
    					continue;
    				
    				String filePath_HistoricalPage = file_HistoricalPage.getAbsolutePath();
    				
    				//		for historical page, their time stamp = their name
    				//		change the format of the timestamp to year-month-day
    				String timeTemp = file_HistoricalPage.getName().substring(0, 4) + "-"
    								+ file_HistoricalPage.getName().substring(4, 6) + "-"
    								+ file_HistoricalPage.getName().substring(6, 8);
    				String timestamp_HistoricalPage = timeTemp;
    				
    				HistoricalPage historicalPage = new HistoricalPage(filePath_HistoricalPage, url, timestamp_HistoricalPage, 50);   				
    		    	
    				//	3.3.1.3	targetPage.pageTag(historicalPage, 0.7)
    				targetPage.pageTag(historicalPage, 0.7);
    			}
    		}
			//	2.5	Display
			for (Paragraph paragraph: targetPage.paragraphs) {
				System.out.println(paragraph.getStartPoint() + "-" + paragraph.getEndPoint() + "\t" 
									+ paragraph.getTimestamp() + "\t" + paragraph.getContent());
			}
			
			//	Test for the feature. FINISHED
			System.out.println(targetPage.featureToString());
			System.out.println("------------------------" + i + "--------------------------");
			
			i++;
    	}		
	}
}
