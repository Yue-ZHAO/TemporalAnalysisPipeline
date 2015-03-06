package yue.temporal.applications;

import java.io.File;
import java.io.IOException;

import yue.temporal.utils.TaggedPageReader;
import yue.temporal.utils.FileProcess;

public class TimestampCount {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		String taggedPageFolderPath = args[0];
		String outputFilePath = args[1];
		
		File taggedPageFolder = new File(taggedPageFolderPath);
		File[] taggedPageList = taggedPageFolder.listFiles();
		
		for (File taggedPage: taggedPageList) {
			TaggedPageReader taggedPageReader = new TaggedPageReader(taggedPage.getAbsolutePath());
			if (taggedPageReader.numOfTimestamps > 0) {				
				String writeDown = taggedPageReader.originalFileName + " " + taggedPageReader.numOfTimestamps;				
				System.out.println(writeDown);
				FileProcess.addLinetoaFile(writeDown, outputFilePath);
			}
		}		
	}

}
