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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
