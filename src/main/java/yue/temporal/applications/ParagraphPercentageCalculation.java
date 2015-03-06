package yue.temporal.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yue.temporal.page.Paragraph;
import yue.temporal.utils.TaggedPageReader;
import yue.temporal.utils.FileProcess;

public class ParagraphPercentageCalculation {

	public static void main(String[] args) throws IOException {
		
		String taggedPageFolderPath = args[0];
		String outputFilePath = args[1];
		int maxNumOfTimestamps = Integer.parseInt(args[2]);
		
		File taggedPageFolder = new File(taggedPageFolderPath);
		File[] taggedPageList = taggedPageFolder.listFiles();
		
		for (File taggedPage: taggedPageList) {
			TaggedPageReader taggedPageReader = new TaggedPageReader(taggedPage.getAbsolutePath());
			if (taggedPageReader.numOfTimestamps > 1 && taggedPageReader.numOfTimestamps <= maxNumOfTimestamps) {
				
				int totalLength = 0;
				List<Integer> count = new ArrayList<Integer>();
				for(int i=0; i<taggedPageReader.numOfTimestamps; i++)
					count.add(0);
				
				List<String> paraTimestamps = taggedPageReader.timestampList;
				
				for(Paragraph paragraph: taggedPageReader.paragraphs) {
					int length = paragraph.getContent().length();
					totalLength = totalLength + length;
					
					String timestamp = paragraph.getTimestamp();
					//	TODO word count based on timestamp
					int index = paraTimestamps.indexOf(timestamp);
					count.set(index, count.get(index) + length);
				}
				//	TODO write file
				String writeDown = taggedPage.getName() + "," + taggedPageReader.numOfTimestamps +  ",";				
				for (Integer lenTemp: count) {
					double percentageTemp = (double)lenTemp/totalLength;
					writeDown = writeDown + percentageTemp + " ";
				}
				
				System.out.println(writeDown);
				FileProcess.addLinetoaFile(writeDown, outputFilePath);
			}
		}		
	}
}
