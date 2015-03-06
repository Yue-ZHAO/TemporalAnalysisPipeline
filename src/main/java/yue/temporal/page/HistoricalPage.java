package yue.temporal.page;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import yue.temporal.utils.FileProcess;
import yue.temporal.utils.HTMLParser;

public class HistoricalPage extends Page{
	
	//	List of paragraphs
	public List<String> paragraphs = new ArrayList<String>();
	
	public HistoricalPage(String absPath_Page, String urlString, String timestamp, int lenThreshold) throws NoSuchAlgorithmException, IOException {
		File file_historicalPage = new File(absPath_Page);
		pageInit(file_historicalPage, urlString, timestamp, lenThreshold);
	}
	
	public HistoricalPage(File file_Page, String urlString, String timestamp, int lenThreshold) throws NoSuchAlgorithmException, IOException {
		pageInit(file_Page, urlString, timestamp, lenThreshold);
	}
		
	private void pageInit(File file_Page, String urlString, String timestamp, int lenThreshold) throws NoSuchAlgorithmException, IOException {
	
		this.URL = urlString;
		this.currentTimestamp = timestamp;
		this.MD5Code = FileProcess.stringTrans_MD5(urlString);
		
		//	Parser the html file
		this.paragraphs = HTMLParser.getPfromHTML_br2nl(file_Page, lenThreshold);
		this.num_Paragraphs = paragraphs.size();
	}

	//	Main for test
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		
		String folderPath_Test = args[0];
		String url = args[1];
		File folder_Test = new File(folderPath_Test);
		File[] filelist_Test = folder_Test.listFiles();
		
		for(File file_Test: filelist_Test) {
			//  For the html file
			String Suffix_HistoricalPage = file_Test.getName().substring(file_Test.getName().lastIndexOf(".")+1);
			
			//	Make sure the file we check is a historical file
			if (!Suffix_HistoricalPage.equals("html"))
				continue;
			
			String temp_Timestamps = file_Test.getName().substring(0, 4) + "-"
					+ file_Test.getName().substring(4, 6) + "-"
					+ file_Test.getName().substring(6, 8);
			System.out.println(file_Test.getAbsolutePath());
			System.out.println(temp_Timestamps);
			HistoricalPage historicalPage = new HistoricalPage(file_Test.getAbsolutePath(), url, temp_Timestamps, 50);
			System.out.println(historicalPage.URL);
			System.out.println(historicalPage.MD5Code);
			System.out.println(historicalPage.currentTimestamp);
			System.out.println(historicalPage.num_Paragraphs);
			//	print paragraphs
			for(String paragraph: historicalPage.paragraphs) {
				System.out.println(paragraph);
				System.out.println();
			}
			System.out.println("----------------------------------------------");
			System.out.println();
		}
	}

}
