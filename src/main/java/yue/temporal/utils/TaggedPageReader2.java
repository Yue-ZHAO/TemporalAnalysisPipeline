package yue.temporal.utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wcohen.ss.JaroWinklerTFIDF;

import yue.temporal.page.HistoricalPage;
import yue.temporal.page.Paragraph2;

public class TaggedPageReader2 {
	
	public String url = "";
	public String MD5 = "";
	public String timestamp = "";
	public String originalFileName = "";
	public int numOfPara = 0;
	public List<Paragraph2> paragraphs = new ArrayList<Paragraph2>();
	public int numOfTimestamps = 0;
	//public List<String> timestampList = new ArrayList<String>();
	public String topicID = "";
	
	// for string compare
	static JaroWinklerTFIDF distanceJaroWinklerTFIDF = new JaroWinklerTFIDF();
	
	//	Add more elements if needed.............
	
	public TaggedPageReader2 (String pagePath) throws IOException {
		
		File file_taggedPage = new File(pagePath);
		String filename_taggedPage = file_taggedPage.getName();
		String[] filename_split = filename_taggedPage.split("_");
		if (filename_split.length > 1)
			MD5 = filename_split[1].trim();
		
		// Filter the files which are not the tagged page
		int flagForTaggedPage = 0;
		List<String> linesFile = FileProcess.readFileLineByLine(pagePath);
		for (String lineFile: linesFile) {
			if (lineFile.startsWith("#")) {
        		//	2.1	Extract file info
				if (lineFile.startsWith("#URL: ")) {
					url = lineFile.substring(6);
					flagForTaggedPage++;
        		} else if (lineFile.startsWith("#Page Timestamp: ")) {
        			timestamp = lineFile.substring(17);
        			flagForTaggedPage++;
        		} else if (lineFile.startsWith("#Original File: ")) {
        			originalFileName = lineFile.substring(16);
        			flagForTaggedPage++;
        		} else if (lineFile.startsWith("#Number of Paragraphs: ")) {
        			numOfPara = Integer.parseInt(lineFile.substring(23));
        			flagForTaggedPage++;
        		} else if (lineFile.startsWith("#Number of Paragraph Timestamps: ")) {
        			numOfTimestamps = Integer.parseInt(lineFile.substring(33));
        			flagForTaggedPage++;
        		} else if (lineFile.startsWith("#Topic ID: ")) {
        			topicID = lineFile.substring(11);
        		} else
        			continue;
        	} else {
        		if (flagForTaggedPage < 4)
        			continue;
        		
				String[] lineContents = lineFile.split("\t", 4);
				if (lineContents.length < 4) {
					System.out.println(lineFile);
					continue;
				}
				
				String relevance = lineContents[0].trim();
				String position = lineContents[1].trim();
				String timestamp = lineContents[2].trim();
				String content = lineContents[3].trim();
				
				String startPos = position.split("-")[0].trim();
				String endPos = position.split("-")[1].trim();
				
				String earliestTimestamp = timestamp.split(",")[0].trim();
				String latestTimestamp = timestamp.split(",")[1].trim();
				
				Paragraph2 paragraph = new Paragraph2();
				paragraph.setRelevant(Boolean.parseBoolean(relevance));
				paragraph.setStartPoint(Integer.parseInt(startPos));
				paragraph.setEndPoint(Integer.parseInt(endPos));
				paragraph.setEarliestTimestamp(earliestTimestamp);
				paragraph.setLatestTimestamp(latestTimestamp);
				paragraph.setContent(content);
				
				paragraphs.add(paragraph);				
        	}
		}		
	}
	
	//	pageCompare(rootFolder)
	public boolean pageComepare(String rootFolderPath_historicalPages, int threshold_Length, double threshold_Similarity) throws NoSuchAlgorithmException, IOException {
		File rootFolder_historicalPages = new File(rootFolderPath_historicalPages);
		if (!MD5.equals("")) {
			File folder_historicalPages = new File(rootFolder_historicalPages, MD5);
			if (!folder_historicalPages.exists())
				return false;
			else {
				System.out.println(folder_historicalPages.getAbsolutePath());
				File[] fileList_historicalPages = folder_historicalPages.listFiles();
				System.out.println(fileList_historicalPages.length);
				for (File file_historicalPage: fileList_historicalPages) {
					if (file_historicalPage.getName().endsWith("html")) {
						String filePath_historicalPage = file_historicalPage.getAbsolutePath();
						String timeTemp_historicalPage = file_historicalPage.getName().substring(0, 4) + "-"
							+ file_historicalPage.getName().substring(4, 6) + "-"
							+ file_historicalPage.getName().substring(6, 8);
						HistoricalPage historicalPage = new HistoricalPage(filePath_historicalPage, url, timeTemp_historicalPage, threshold_Length);
						if(this.timestamp.compareTo(historicalPage.currentTimestamp) > 0) {
							for(Paragraph2 paragraph: paragraphs) {
								boolean flag_appearance = false;
								String paraContent_taggedPage = paragraph.getContent();
								//	Look for similar paragraphs
								for (String paraContent_historicalPage: historicalPage.paragraphs) {
									double score = distanceJaroWinklerTFIDF.score(paraContent_taggedPage, paraContent_historicalPage);
									if (score >= threshold_Similarity) {
										flag_appearance = true;
										break;
									}
								}
								List<String> appList = paragraph.getAppearList();
								appList.add(timeTemp_historicalPage + ": " + flag_appearance);
								paragraph.setAppearList(appList);
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	
	//	toFile(String)
	public void toFile(String filePath) throws IOException {
		File file_taggedPageReader = new File(filePath);
		if (file_taggedPageReader.exists()) {
			file_taggedPageReader.delete();
		}
		file_taggedPageReader.createNewFile();
		
		//	Header
		FileProcess.addLinetoaFile("#URL: " + this.url, filePath);
		FileProcess.addLinetoaFile("#MD5: " + this.MD5, filePath);
		FileProcess.addLinetoaFile("#Original File: " + this.originalFileName, filePath);
		FileProcess.addLinetoaFile("#Page Timestamp: " + this.timestamp, filePath);
		FileProcess.addLinetoaFile("#Length Threshold: 50", filePath);
		FileProcess.addLinetoaFile("#Similarity Threshold: JaroWinklerTFIDF 0.7", filePath);
		FileProcess.addLinetoaFile("#Number of Paragraph Timestamps: " + this.numOfTimestamps, filePath);
		FileProcess.addLinetoaFile("#Number of Paragraphs: " + this.numOfPara, filePath);
		FileProcess.addLinetoaFile("#Topic ID: " + this.topicID, filePath);
		
		FileProcess.addLinetoaFile("#Timestamps of Paragraphs (Relecance, Character Count, Timestamps, Content)", filePath);
		for(Paragraph2 paragraph: paragraphs) {
			FileProcess.addLinetoaFile(paragraph.isRelevant() + "\t" +
					paragraph.getStartPoint() + "-" + paragraph.getEndPoint() + "\t" +
					paragraph.getEarliestTimestamp() + "," + paragraph.getLatestTimestamp() + "\t" +
					paragraph.getContent(), filePath);
			List<String> appearList = paragraph.getAppearList();
			if (appearList.size() > 0) {
				Collections.sort(appearList);
				for (String appearance: appearList)
					FileProcess.addLinetoaFile(appearance, filePath);
			}
			FileProcess.addLinetoaFile("", filePath);
		}		
	}
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		
		File folder_test = new File(args[0]);
		File[] fileList_test = folder_test.listFiles();
		for (File file_test: fileList_test) {
			TaggedPageReader2 taggedPageReader = new TaggedPageReader2(file_test.getAbsolutePath());
			if (taggedPageReader.pageComepare(args[1], 50, 0.7)) {
				File folder_result = new File(args[2]);
				File file_result = new File(folder_result, file_test.getName());
				taggedPageReader.toFile(file_result.getAbsolutePath());
			} else
				System.out.println(taggedPageReader.originalFileName);
		}
		
	}
}
