package yue.temporal.pipeline;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import yue.temporal.utils.FileProcess;

public class IADownloader {
	
	
	/**
	 * 
	 * @param inputURL
	 * @return JSONArray urlList
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public static JSONArray wayback(String inputURL, String startTime, String endTime) throws ClientProtocolException, IOException, URISyntaxException{
		String cdxURL = "http://web.archive.org/cdx/search/cdx?url=";
		String originURL = inputURL;
		JSONArray urlList = null;
		
		//	Add some figures about our requests
		String filterURL = "&from=" + startTime.trim() + "&to=" + endTime.trim() + "&fl=timestamp,original,digest&output=json";
		String formatURL = cdxURL + originURL + filterURL;
		//	format URL
		URL url = new URL(formatURL);
		String nullFragment = null;
		URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
		//	System.out.println("URI " + uri.toString() + " is OK");
		//	Use http Get to get feedback from Internet Archive
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(uri);
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
		try{
		    HttpEntity httpEntity = httpResponse.getEntity();
	    
		    InputStream inSm = httpEntity.getContent();  
	        Scanner inScn = new Scanner(inSm);
	        String responseString = "";
	        while (inScn.hasNextLine()) {  
	        	responseString = responseString + inScn.nextLine();	        	
	        }
	        inScn.close();
	        
	        //	If the response is error information, or no information
	        //	Change it to 0 content.
	        if (!responseString.startsWith("["))
	        	responseString = "[]";
	        urlList = new JSONArray(responseString);		    
		    EntityUtils.consume(httpEntity);			
		} finally {
			httpResponse.close();
		}
		return urlList;
	}
	
	
	public static List<String> generateIAurls(JSONArray feedback){
		String preURL = "https://web.archive.org/web/";
		List<String> urls = new ArrayList<String>();
		
		if (feedback.length() < 1 || feedback == null)
			return null;
		
		// 	For the JSON array we get from Internet Archive
		// 	The first element is the field name of the later data.
		// 	So we use it to initialize our indexes
		int index_Timestamp = 0;
		int index_Original = 0;
		int index_Digest = 0;
		for(int i=0; i<feedback.getJSONArray(0).length(); i++){
			String temp = feedback.getJSONArray(0).get(i).toString();
			if (temp.equals("timestamp")){
				index_Timestamp = i;
			} else if (temp.equals("original")){
				index_Original = i;
			} else if (temp.equals("digest")){
				index_Digest = i;
			}
		}
		
		// 	For the rest elements of JSON array
		//	If the current element has different content (digest) with the former one
		//	We transform them into URLs which we will use to get content from Internet Archive
		//	And put them in the list called urls
		String tempDigest = "";
		for(int j=1; j<feedback.length(); j++){
			String currentTimestamp = feedback.getJSONArray(j).get(index_Timestamp).toString();
			String currentOriginal = feedback.getJSONArray(j).get(index_Original).toString();
			String currentDigest = feedback.getJSONArray(j).get(index_Digest).toString();
			
			if (!currentDigest.equals(tempDigest)){
				String tempURL = preURL + currentTimestamp + "/" + currentOriginal;
				urls.add(tempURL);
			}
			tempDigest = currentDigest;
		}				
		return urls;		
	}
	
	public static void downloadPages(List<String> urls, String subTargetFolder) throws ClientProtocolException, IOException, URISyntaxException, InterruptedException{
		int i = 0;
		for (String url: urls){
			//	format URL
			long begintime = System.currentTimeMillis();
			URL url1 = new URL(url);
			String nullFragment = null;
			URI uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), nullFragment);
			//	http Client
			CloseableHttpClient httpClient = HttpClients.createDefault();
			RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build(); 
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(config);
			//	httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 1.7; .NET CLR 1.1.4322; CIBA; .NET CLR 2.0.50727)");
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			//	int statusCode = httpResponse.getStatusLine().getStatusCode();
			try {
				HttpEntity httpEntity = httpResponse.getEntity();
			    InputStream inSm = httpEntity.getContent(); 
			    // 	Read the input stream of the entity
			    //	Transfer it in to the file with html format
			    String fileName = url.substring(27, 40);
			    //	Now it runs well, but I am not sure is the path like subTargetFolder + fileName + ".html" is good enough.
			    String htmlFilePath = subTargetFolder + fileName + ".html";
			   	BufferedInputStream bis = new BufferedInputStream(inSm);
			   	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(htmlFilePath)));
			   	int inByte;
			   	while((inByte = bis.read()) != -1) 
			   		bos.write(inByte);
			    bis.close();
			    bos.close();
			    System.out.print("Historical version: " + url.substring(28, 40) + ". ");
			} finally {
				httpResponse.close();
				long endtime = System.currentTimeMillis();
				System.out.print("Time cost is: " + (endtime - begintime) + ". ");
				i++;
				System.out.println(i + "/" + urls.size() + " Completed.");
				// TimeUnit.SECONDS.sleep(1);
			}
			httpClient.close();
		}	
	}
	
	public static int downloadAllVersions(String originalURL,
			String targetFolder, String startTime, String endTime) throws ClientProtocolException, IOException, URISyntaxException, NoSuchAlgorithmException, InterruptedException {
		// 	Use wayback machine get feedback from IA
		JSONArray feedback = wayback(originalURL, startTime, endTime);
		
		//	If there is no feedback, return
		if (feedback.length() < 1 || feedback == null) {
			return -1;
		} else {		
			//	Generate urls belongs to IA based on the feedback
			List<String> urlsIA = generateIAurls(feedback);
			
			//	Generate the sub-folder of this url in the target folder
			File subTargetFolder = FileProcess.generateSubFolder(originalURL, targetFolder);
			//	null means that the historical pages have been downloaded
			if (subTargetFolder == null)
				return 0;
			String subTargetFolderPath = subTargetFolder.getAbsolutePath();
			
			//	Download Pages from IA based on the URLs we generate.
			downloadPages(urlsIA, subTargetFolderPath);
			
			//	Write Down the features of the URL
			writeDownFeatures(subTargetFolder.getName(), originalURL, feedback.length(), urlsIA.size(), feedback, targetFolder);
			return 1;
		}
	}


	private static void writeDownFeatures(String folderName, String originalURL, int length,
			int size, JSONArray feedback, String targetFolder) {
		// 	For the JSON array we get from Internet Archive
		// 	The first element is the field name of the later data.
		// 	So we use it to initialize our indexes
		int index_Timestamp = 0;

		for(int i=0; i<feedback.getJSONArray(0).length(); i++){
			String temp = feedback.getJSONArray(0).get(i).toString();
			if (temp.equals("timestamp")){
				index_Timestamp = i;
			} 
		}		
		
		// Generate features
		String features = 
				folderName + " " + 
				length + " " + 
				size + " " + 
				feedback.getJSONArray(1).get(index_Timestamp).toString() + " " + 
				feedback.getJSONArray(length-1).get(index_Timestamp).toString() + " " + 
				originalURL;
		
		//	Write Features to the targetFolder
		File featureFile = new File(targetFolder, "HistoricalPageFeatures");
		FileProcess.addLinetoaFile(features, featureFile.getAbsolutePath());
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
