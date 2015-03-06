package yue.temporal.applications;

import java.io.File;
//	import java.io.FileNotFoundException;
//	import java.io.FileReader;
import java.io.IOException;
//	import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import yue.temporal.utils.HTMLParser;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
//	import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;

public class MainContentExtraction {
		
    public static String content(String url) {
        try {
            final HTMLDocument htmlDoc = HTMLFetcher.fetch(new URL(url));
            final TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
            // String title = doc.getTitle(); 
            String content = ArticleExtractor.INSTANCE.getText(doc);

            return content;
        } catch (Exception e) {
            return null;
        } 
    }
    
	public static void main(String[] args) throws BoilerpipeProcessingException, IOException {
		//	URL url = new URL("http://blogs.miaminewtimes.com/riptide/2012/02/fidel_castro_has_found_jesus_w.php");
		//	String text = ArticleExtractor.INSTANCE.getText(url);
		//	String text2 = KeepEverythingExtractor.INSTANCE.getText(url);
		//	String[] textList = text2.split("\n");
		
		//	FileReader fr = new FileReader("/Users/yuezhao/Dropbox/TempInfoAnalysis/Data/Clueweb12Pages/clueweb12-relevant-docids-output/clueweb12-relevant-docids-output/clueweb12-0000tw-01-09567");
		//	String text3 = KeepEverythingExtractor.INSTANCE.getText(fr);
		//	String[] textList3 = text3.split("\n");
		
		//	System.out.println(text);
		//	System.out.println();
		//	for(String testLine: textList3) {
		//		if(testLine.length()>10) {
		//		System.out.println(testLine);
		//		System.out.println();
		//		}
		//	}
		
		File file = new File("/Users/yuezhao/Desktop/clueweb12-0201wb-13-30583.html");
		String allContent = HTMLParser.extractTextFromHTML_jsoup(file);
		String allContent2 = HTMLParser.extractTextFromHTML_br2nl(file);
		System.out.println(allContent);
		System.out.println();
		System.out.println("1-----------------------------------------------------------------");
		System.out.println();
		System.out.println(allContent2);
		System.out.println();
		System.out.println("2-----------------------------------------------------------------");
		System.out.println();
		List<String> textLineList = HTMLParser.getPfromHTML_br2nl(file, 1);
		for (String textLine: textLineList) {
			System.out.println(textLine.trim());
			System.out.println();
		}
	}
}
