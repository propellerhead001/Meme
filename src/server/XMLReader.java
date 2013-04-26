package server;

import java.io.IOException;
import  java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

enum ProcessingElement{
	NONE, TITLE, FILENAME
}

public class XMLReader extends DefaultHandler{
	private List<VideoFile> videoList = new ArrayList<VideoFile>();
	private VideoFile currentVideo;
	private String inputFile;
	private ProcessingElement currentElement = ProcessingElement.NONE;

	public XMLReader(String filename){
		inputFile = filename;

		try {
			// use the default parser
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			// parse the input
			saxParser.parse(inputFile, this);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (SAXException saxe) {
			saxe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public List<VideoFile> getList(){
		return videoList;

	}

	// overridden method for start element callback
	public void startElement(String uri, String localName, String qName, Attributes
			attrs) throws SAXException {
		// sort out element name if (no) namespace in use
		String elementName = localName;
		if ("".equals(elementName)) {
			elementName = qName;
		}
		if(elementName.equals("videolist")){
		}
		else if(elementName.equals("video")) {
			if(currentVideo == null){
				currentVideo = new VideoFile();
			}
			currentVideo.setId(attrs.getValue(0));
		}
		else if(elementName.equals("title")){
			currentElement = ProcessingElement.TITLE;
		}
		else if(elementName.equals("filename")){
			currentElement = ProcessingElement.FILENAME;
		}
	}

	// overridden method for character data callback
	public void characters(char ch[], int start, int length) throws SAXException {
		//retrieves teh data stored in the title and filename elements
		switch (currentElement){
		case TITLE:
			currentVideo.setTitle(new String(ch, start, length));
			break;
		case FILENAME:
			currentVideo.setFilename(new String(ch, start, length));
			break;
		default:
			break;
		}
	}
	
	/**
     * Called by the parser when it encounters any end element tag.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // sort out element name if (no) namespace in use
        String elementName = localName;
        if ("".equals(elementName)) {
            elementName = qName;
        }
        if (elementName.equals("video")) {
            videoList.add(currentVideo);
            currentVideo = null;
        }
        // finished adding content from various sub-elements of student
        else if (elementName.equals("title")) {
            currentElement = ProcessingElement.NONE;
        }
        else if (elementName.equals("filename")) {
            currentElement = ProcessingElement.NONE;
        }
    }


}
