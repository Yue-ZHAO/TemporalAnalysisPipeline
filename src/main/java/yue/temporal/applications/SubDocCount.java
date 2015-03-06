package yue.temporal.applications;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yue.temporal.page.Paragraph;
import yue.temporal.page.TargetPage;
import yue.temporal.utils.CluewebFileProcess;
import yue.temporal.utils.FileProcess;
import yue.temporal.utils.HTMLParser;

import de.l3s.boilerpipe.BoilerpipeProcessingException;

public class SubDocCount {
    
	public static void subDocCount( String[] args ) throws NoSuchAlgorithmException, IOException, BoilerpipeProcessingException
    {
    	//	1.	get the folder of target pages
    	String folderPath_TargetPage = args[0];
    	File folder_TargetPage = new File(folderPath_TargetPage);
        
    	//	2. 	process each file in the folder of target pages
    	File[] fileList_TargetPage = folder_TargetPage.listFiles();
    	for (File file_TargetPage: fileList_TargetPage) {

    		//	TO MAKE SURE THE FILE IS THE CLUEWEB PAGE WE NEED
    		if (!file_TargetPage.getName().startsWith("clueweb12"))
    			continue;
    		
    		//	2.0	output the signal to screen
    		System.out.println();
			System.out.println( "Start: " + file_TargetPage.getAbsolutePath());
    	
    		//	2.1	process the file to get path, url, timestamp
    		String filePath_TargetPage = file_TargetPage.getAbsolutePath();
    		String url = CluewebFileProcess.readURLFromCluewebFile(filePath_TargetPage);

    		//	implement the function to extract the timestamp
    		String timestamp_TargetPage = CluewebFileProcess.readTimeFromCluewebFile(filePath_TargetPage);
    		
    		//	2.2 generate TargetPage targetPage(path, url, timestamp)
    		TargetPage targetPage = new TargetPage(filePath_TargetPage, url, timestamp_TargetPage, 50, 0.7);
    			    		
    		//	2.3 Calculate the total count
    		int paraLength_total = 0;
    		int lengthTotal1 = replaceBlank(HTMLParser.extractTextFromHTML_jsoup(file_TargetPage)).length();
    		int lengthTotal2 = replaceBlank(HTMLParser.extractTextFromHTML_br2nl(file_TargetPage)).length();
    		for(Paragraph paragraph: targetPage.paragraphs) {
    			int paraLength = replaceBlank(paragraph.getContent()).length();
    			paraLength_total = paraLength_total + paraLength;    			
    		}
    		double percentage = (double)paraLength_total/lengthTotal1;
    		
    		System.out.println(percentage);
    		FileProcess.addLinetoaFile(percentage + " " + paraLength_total + " " + lengthTotal1 + " " + lengthTotal2 + " " + targetPage.filename_TargetPage, args[1]);
    		
    		//	2.4	output the signal to screen
    		System.out.println( "Finish: " + file_TargetPage.getAbsolutePath());
    	
    	}
    	//	3.	output "finished"    	
    	System.out.println( "All Finish!" );
    }
    
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
