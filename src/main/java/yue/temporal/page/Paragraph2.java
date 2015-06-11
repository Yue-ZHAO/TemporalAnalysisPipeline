package yue.temporal.page;

import java.util.ArrayList;
import java.util.List;

public class Paragraph2 {

	//	the relevant tag of the paragraph
	private boolean relevant;
	
	//	the start position of the paragraph
	private int startPoint;
	
	//	the end position of the paragraph
	private int endPoint;
	
	//	the content
	private String content;
	
	//	the time stamp
	private String earliestTimestamp;
	private String latestTimestamp;
	
	//	The list of the time stamps about the appearance and the disappearance of the paragraph
	private List<String> appearList = new ArrayList<String>();
	
	public String featureToString() {
		String feature = getStartPoint() + " " + getEndPoint() + " " + getEarliestTimestamp() + " " +getLatestTimestamp();
		return feature;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(int startPoint) {
		this.startPoint = startPoint;
	}

	public int getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(int endPoint) {
		this.endPoint = endPoint;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}

	public List<String> getAppearList() {
		return appearList;
	}

	public void setAppearList(List<String> appearList) {
		this.appearList = appearList;
	}

	public String getEarliestTimestamp() {
		return earliestTimestamp;
	}

	public void setEarliestTimestamp(String earliestTimestamp) {
		this.earliestTimestamp = earliestTimestamp;
	}

	public String getLatestTimestamp() {
		return latestTimestamp;
	}

	public void setLatestTimestamp(String latestTimestamp) {
		this.latestTimestamp = latestTimestamp;
	}

}
