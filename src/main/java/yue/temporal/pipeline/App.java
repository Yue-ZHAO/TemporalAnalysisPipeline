package yue.temporal.pipeline;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import yue.temporal.utils.FileProcess;
import yue.temporal.utils.CluewebFileProcess;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, NoSuchAlgorithmException, URISyntaxException, InterruptedException, BoilerpipeProcessingException
    {
        System.out.println( "Hello World! This is yue's pipeline for timestamping sub-documents of clueweb files." );
        
        //	INPUT
        String folderPath_Clueweb = args[0];	//	The path of the folder which contains clueweb files.
        String folderPath_Output = args[1];		//	The path of the folder which contains all the output result
        int threshold_Length = Integer.parseInt(args[2]);			//	The threshold of the paragraphs' length
        double threshold_Similarity = Double.parseDouble(args[3]);	//	The threshold of the similarity above which we thinks the two strings are similar.
        String time_EarliestRecord = args[4];
        String time_LatestRecord = args[5];
        
        //	STEP 1: reading urls from clueweb files.
        System.out.println( "STEP 1: reading urls from clueweb files." );
        List<String> list_url = new ArrayList<String>();
        list_url = CluewebFileProcess.readURLFromCluewebFolder(folderPath_Clueweb);
        System.out.println( "STEP 1 is finished!!" );
        
        //	STEP 2: downloading the historical pages from Internet Archive based on the urls extracted in STEP 1.
        System.out.println( "STEP 2: downloading the historical pages from Internet Archive based on the urls extracted in STEP 1." );
        int numRecord = 0;
        int numOriginal = 0;
        File file_DownloadLog = new File(folderPath_Output, "historicalPagesDownload.log");
        File folder_HistoricalPages = new File(folderPath_Output, "historicalPages");
        folder_HistoricalPages.mkdir();
        for(String url: list_url) {
        	int flag = IADownloader.downloadAllVersions(url, folder_HistoricalPages.getAbsolutePath(), time_EarliestRecord, time_LatestRecord);
        	if (flag == 1 || flag == 0){
        		numRecord++;
        		numOriginal++;
        	} else if (flag == -1) {
        		numOriginal++;
        	}
        	FileProcess.addLinetoaFile("URL: " + url, file_DownloadLog.getAbsolutePath());
        	FileProcess.addLinetoaFile("Status: " + flag + ".\tThe percent of record: " + numRecord + " / " + numOriginal, file_DownloadLog.getAbsolutePath());
        	System.out.println("URL: " + url);
        	System.out.println("Status: " + flag);
        	System.out.println("The percent of record: " + numRecord + " / " + numOriginal);
        	System.out.println();
        }
        System.out.println( "STEP 2 is finished!!" );
        
        //	STEP 3: timestamping the clueweb files based on the historical pages downloaded in STEP 2.
        System.out.println( "STEP 3: timestamping the clueweb files based on the historical pages downloaded in STEP 2." );
        File folder_TaggedPages = new File(folderPath_Output, "taggedPages");
        folder_TaggedPages.mkdir();
        PageTimestamper.timestamp(folderPath_Clueweb, folder_HistoricalPages.getAbsolutePath(), folder_TaggedPages.getAbsolutePath(), threshold_Length, threshold_Similarity);
        System.out.println( "STEP 3 is finished!!" );
        
        //	STEP 4: extracting the features of paragraphs timestamped in STEP 3.
        System.out.println( "STEP 4: extracting the features of paragraphs timestamped in STEP 3." );
        File file_ParagraphFeatures = new File(folderPath_Output, "paragraphFeatures.arff");
        ParagraphFeatureExtractor.extractFeatureToARFF(folder_TaggedPages.getAbsolutePath(), file_ParagraphFeatures.getAbsolutePath());
        System.out.println( "STEP 4 is finished!!" );
        
        //	TODO STEP 5: using WEKA to classify the paragraphs based on the features extracted in STEP 4.
        System.out.println( "STEP 5: using WEKA to classify the paragraphs based on the features extracted in STEP 4." );
        
        System.out.println( "STEP 5 is finished!!" );
    }
}
