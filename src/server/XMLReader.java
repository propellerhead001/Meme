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

/**
 * Parses the XML file for Server
 * @author sc900 rjm529
 */
public class XMLReader extends DefaultHandler{
	private List<VideoFile> videoList = new ArrayList<VideoFile>();
	private VideoFile currentVideo;
	private String inputFile;
	private ProcessingElement currentElement = ProcessingElement.NONE;

	public XMLReader(){
	}

	/**
	 * @param filename the filename of the XML file to be parsed
	 * @return the list of VideoFiles which have been parsed
	 */
	public List<VideoFile> getList(String filename){
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
		return videoList;

	}

	/* overridden method for start element callback
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
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

	
	/* overridden method for character data callback
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
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
	
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
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
