package server;

import java.util.*;

public class Server {
	private List<VideoFile> videoList;
	private XMLReader reader;
	
	public Server(){
		reader = new XMLReader("videolist.xml");
		videoList = reader.getList();
	}
	
	public List<VideoFile> getList() {
		return videoList;
	}

}
