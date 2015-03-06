package yue.temporal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
//	import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
//	import org.jsoup.select.NodeVisitor;






import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;

public class HTMLParser {

	public static List<String> getPfromHTML_jsoup(String historicalPagePath, int minLength) throws IOException {
		
		List<String> paragraphs = new ArrayList<String>();
		File input = new File(historicalPagePath);
		Document doc = Jsoup.parse(input, "UTF-8");
		
		Elements phase = doc.getElementsByTag("p");
		
		for (Element link : phase) {
		  String linkText = link.text();
		  if ((linkText.length() >= minLength) && 
				(!linkText.startsWith("WARC")) && 
				(!linkText.startsWith("Content-Length:")) && 
				(!linkText.startsWith("Content-Type:"))) {
			  paragraphs.add(linkText);
		  }
		}		
		return paragraphs;
	}
	
	public static List<String> getPfromHTML_jsoup(File file_HistoricalPage, int minLength) throws IOException {
		List<String> paragraphs = new ArrayList<String>();
		
		Document doc = Jsoup.parse(file_HistoricalPage, "UTF-8");

		//Elements phase = doc.select("p");
		Elements phase = doc.getElementsByTag("p");
		
		for (Element link : phase) {
		  String linkText = link.text();
		  if ((linkText.length() >= minLength) && 
				(!linkText.startsWith("WARC")) && 
				(!linkText.startsWith("Content-Length:")) && 
				(!linkText.startsWith("Content-Type:"))) {
			  paragraphs.add(linkText);
		  }
		}		
		return paragraphs;
	}
	
	public static List<String> getPfromHTML_boilerpipe(File file_HistoricalPage, int minLength) throws IOException, BoilerpipeProcessingException {
		List<String> paragraphs = new ArrayList<String>();
		FileReader fr = new FileReader(file_HistoricalPage);
		String text = KeepEverythingExtractor.INSTANCE.getText(fr);
		String[] textList = text.trim().split("\n");
		for (String textLine: textList) {
			if((textLine.length() >= minLength) && 
					(!textLine.startsWith("WARC")) && 
					(!textLine.startsWith("Content-Length:")) && 
					(!textLine.startsWith("Content-Type:"))) {
				paragraphs.add(textLine);
			}
		}
		return paragraphs;
	}
	
	public static String extractTextFromHTML_jsoup(File file) throws IOException {
	    
		StringBuilder sb = new StringBuilder();
	    
	    InputStream fis = new FileInputStream(file);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    String line;
	    
	    while ( (line=br.readLine()) != null) {
	    	sb.append(line);
	    }
	    String s = sb.toString().replaceAll("\\\\n", "\n");
	    String textRelaxed = Jsoup.clean(s, "", Whitelist.relaxed(), new Document.OutputSettings().prettyPrint(false));
	    String textOnly = Jsoup.parse(textRelaxed).text();
	    
	    br.close();
	    br = null;
	    fis = null;
	    
	    return textOnly;
	  }
	
	public static String extractTextFromHTML_br2nl(File file) throws IOException {
	    
		String s = br2nl(file);
		String s_parse = Jsoup.parse(s.trim().toString()).text();	    
	    return s_parse;
	  }
	
	public static String extractTextFromHTML_boilerpipe(File file) throws IOException, BoilerpipeProcessingException {	    
		FileReader fr = new FileReader(file);
		String text = KeepEverythingExtractor.INSTANCE.getText(fr);
		return text;
	}
	
	public static String br2nl(String html) {
	    if(html==null)
	        return html;
	    Document document = Jsoup.parse(html);
	    document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
	    document.select("br").append("\\n");
	    document.select("p").prepend("\\n\\n");
	    document.select("div").prepend("\\n\\n");
	    String s = document.html().replaceAll("\\\\n", "\n");
	    return Jsoup.clean(s, "", Whitelist.relaxed(), new Document.OutputSettings().prettyPrint(false));
	}
	
	public static String br2nl(File htmlFile) throws IOException {

	    Document document = Jsoup.parse(htmlFile, "UTF-8");
	    document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
	    document.select("br").append("\\n");
	    document.select("p").prepend("\\n\\n");
	    document.select("div").prepend("\\n\\n");
	    String s = document.html().replaceAll("\\\\n", "\n");
	    return Jsoup.clean(s, "", Whitelist.relaxed(), new Document.OutputSettings().prettyPrint(false));	    
	}	
	
	public static List<String> getPfromHTML_br2nl(File htmlFile, int minLength) throws IOException {
		
		List<String> paragraphs = new ArrayList<String>();
		String text = br2nl(htmlFile);
		String[] textList = text.trim().split("\n");
		for (String textLine: textList) {
			String textLine2 = Jsoup.parse(textLine.trim().toString()).text();
			if((textLine2.trim().length() >= minLength) && 
					(!textLine2.startsWith("WARC")) && 
					(!textLine2.startsWith("Content-Length:")) && 
					(!textLine2.startsWith("Content-Type:"))) {
				paragraphs.add(textLine2.trim());
			}
		}
		return paragraphs;
	}
	
	
	public static int findPosFromHTML_jsoup(String paragraphContent, File file_HTML) throws IOException {
		
		String textOnly = extractTextFromHTML_jsoup(file_HTML);
		
		int startPos = textOnly.indexOf(paragraphContent);
		
		return startPos;
	}
	
	public static int findPosFromHTML_br2nl(String paragraphContent, File file_HTML) throws IOException {
		
		String textOnly = extractTextFromHTML_br2nl(file_HTML);
		
		int startPos = textOnly.indexOf(paragraphContent);
		
		return startPos;
	}

    public static void main( String[] args ) throws IOException, BoilerpipeProcessingException {
    	//getPfromHTML("/Users/yuezhao/GoogleDrive/HistoricalPagesForClueweb12/downloadPages5/ffd3c78233e9259fc1f5edbf96ba486c/201211110446.html", 50);    	
    	File file = new File("/Users/yuezhao/Desktop/clueweb12-0000wb-31-12737.html");
//    	System.out.println(br2nl(file));
//    	System.out.println();
//    	System.out.println("---------------------------------------");
//    	System.out.println();
//    	System.out.println(br2nl2(file));
//    	System.out.println();
//    	System.out.println("---------------------------------------");
//    	System.out.println();
    	System.out.println(extractTextFromHTML_jsoup(file));
    	System.out.println();
    	System.out.println("---------------------------------------");
    	System.out.println();
//    	System.out.println(extractTextFromHTML_jsoup2(file));
//    	System.out.println();
//    	System.out.println("---------------------------------------");
//    	System.out.println();
//    	System.out.println(extractTextFromHTML_boilerpipe(file));
    	
    	
    	List<String> testSrings = getPfromHTML_br2nl(file, 1);
    	for (String testString: testSrings) {
    		System.out.println(testString);
    		//System.out.println();
    	}
    	
//		File input = new File("/Users/yuezhao/Desktop/clueweb12-0000wb-31-12737.html");
//		Document doc = Jsoup.parse(input, "UTF-8");
//		doc.traverse(new NodeVisitor() {
//		    public void head(Node node, int depth) {
//		        System.out.println("Entering tag: " + node.nodeName());
//		        System.out.println("Depth: " + depth);
//		    }
//		    public void tail(Node node, int depth) {
//		        System.out.println("Exiting tag: " + node.nodeName());
//		        System.out.println("Depth: " + depth);
//		    }
//		});
    }

}
