package yue.temporal.pipeline;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
//	import java.util.ArrayList;
//	import java.util.List;

import java.util.Properties;

//import de.l3s.boilerpipe.BoilerpipeProcessingException;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import yue.temporal.page.TargetPage;
import yue.temporal.utils.FileProcess;
import yue.temporal.utils.CluewebFileProcess;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, NoSuchAlgorithmException, URISyntaxException, InterruptedException
    {
        System.out.println( "Hello World! This is yue's pipeline for timestamping sub-documents of clueweb files." );
        
        //	Initialization for GLOBAL
        String folderPath_Clueweb = args[0];	//	The path of the folder which contains clueweb files.
        File folder_Clueweb = new File(folderPath_Clueweb);
        if (!folder_Clueweb.exists()) {
        	System.out.println("Error: The input folder of Clueweb files does not exist!");
        	return;
        }
        else if (!folder_Clueweb.isDirectory()) {
        	System.out.println("Error: The input folder of Clueweb files does not exist!");
        	return;
        } else 
        	System.out.println("The input folder of Clueweb files is: " + folder_Clueweb.getAbsolutePath());
        
        String folderPath_Output = args[1];		//	The path of the folder which contains all kinds of the output result (downloading file)
        
        File folder_Output = new File(folderPath_Output);
        if (!folder_Output.exists() || !folder_Output.isDirectory())
        	folder_Output.mkdir();
        System.out.println("The output folder of the pipeline is: " + folder_Output.getAbsolutePath());
        
        int threshold_Length = Integer.parseInt(args[2]);			//	The threshold of the paragraphs' length
        double threshold_Similarity = Double.parseDouble(args[3]);	//	The threshold of the similarity above which we thinks the two strings are similar.
        String time_EarliestRecord = args[4];						//	The start year of the timespan
        String time_LatestRecord = args[5];							//	The end year of the timespan
        
        //	Initialization for STEP 2.
        int numRecord = 0;
        int numOriginal = 0;
        File file_DownloadLog = new File(folderPath_Output, "historicalPagesDownload.log");
        File folder_HistoricalPages = new File(folderPath_Output, "historicalPages");
        folder_HistoricalPages.mkdir();
        
        //	Initialization for STEP 3
        File folder_TaggedPages = new File(folderPath_Output, "taggedPages");
        folder_TaggedPages.mkdir();
        
        //	Initialization for STEP 4
        File file_ParagraphFeatures = new File(folderPath_Output, "paragraphFeatures.arff");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, sutime");        
        props.put("customAnnotatorClass.sutime", "edu.stanford.nlp.time.TimeAnnotator");
        props.put("sutime.rules", "sutimeRules/defs.sutime.txt, sutimeRules/english.sutime.txt");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        File[] fileList_Clueweb = folder_Clueweb.listFiles();
        for (File file_Clueweb: fileList_Clueweb) {
        	if (CluewebFileProcess.isCluewebFile(file_Clueweb)) {
        		//	STEP 1: reading the url from the clueweb file.
        		System.out.println( "STEP 1: Reading url from clueweb file " + file_Clueweb.getAbsolutePath());
        		String url = CluewebFileProcess.readURLFromCluewebFile(file_Clueweb);
        		System.out.println( "STEP 1 is finished!! The URL is " + url);
        		long size_file_Clueweb = file_Clueweb.length();
        		if (size_file_Clueweb > 1000000) {
            		System.out.println( "Warn: The size of the clueweb file is too large (> 1MB). Skip to the next clueweb file" );
            		System.out.println();
            		System.out.println();
            		continue;
        		}
        		
        		//	STEP 2: downloading the historical pages from Internet Archive based on the url extracted in STEP 1.
        		System.out.println( "STEP 2: downloading the historical pages from Internet Archive based on the url " + url);
        		int flag = IADownloader.downloadAllVersions(url, folder_HistoricalPages.getAbsolutePath(), time_EarliestRecord, time_LatestRecord);
            	if (flag == 1 || flag == 0){
            		numRecord++;
            		numOriginal++;
            	} else if (flag == -1) {
            		numOriginal++;
            	}
            	FileProcess.addLinetoaFile("URL: " + url, file_DownloadLog.getAbsolutePath());
            	FileProcess.addLinetoaFile("Status: " + flag + ".\tThe percent of record: " + numRecord + " / " + numOriginal, file_DownloadLog.getAbsolutePath());
            	System.out.println("Status: " + flag);
            	System.out.println("The percent of record: " + numRecord + " / " + numOriginal);
            	System.out.println( "STEP 2 is finished!!" );
            	
            	if (flag == -1) {
            		System.out.println( "Warn: No historical pages recorded, skip to next clueweb file." );
            		System.out.println();
            		System.out.println();
            		continue;
            	} else {
            		//	STEP 3: timestamping the clueweb file based on the historical pages downloaded in STEP 2.
            		System.out.println( "STEP 3: timestamping the clueweb file based on the historical pages downloaded in STEP 2." );
            		TargetPage taggedPage = PageTimestamper.timestampFile(file_Clueweb.getAbsolutePath(), folder_HistoricalPages.getAbsolutePath(), folder_TaggedPages.getAbsolutePath(), threshold_Length, threshold_Similarity);
            		System.out.println( "STEP 3 is finished!!" );
                
            		//	STEP 4: extracting the features of paragraphs timestamped in STEP 3.
            		System.out.println( "STEP 4: extracting the features of paragraphs timestamped in STEP 3." );
            		ParagraphFeatureExtractor.extractFeatureToFile(file_ParagraphFeatures.getAbsolutePath(), taggedPage, pipeline);
            		System.out.println( "STEP 4 is finished!!" );
            		System.out.println();
            		System.out.println();
            	}
        	}
        }
        
        /*
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
        //	System.out.println( "STEP 5: using WEKA to classify the paragraphs based on the features extracted in STEP 4." );        
        //	System.out.println( "STEP 5 is finished!!" );
        	*/
    }
}
