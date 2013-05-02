package server;

import java.util.*;

public class Server {
	private List<VideoFile> videoList;
	private XMLReader reader;
	
	public Server(){
		reader = new XMLReader();
		videoList = reader.getList("videolist.xml");
	}
	
	public List<VideoFile> getList() {
		return videoList;
	}

}
