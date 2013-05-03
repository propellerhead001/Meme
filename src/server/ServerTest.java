package server;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ServerTest {
	private Server server;
	private List<VideoFile> serverList;
	@Test
	public void serverGetsList(){
		server = new Server();
		serverList = server.getList();
		
		// Check serverList is a list
		assertTrue(serverList instanceof List);		
		// Check first video is returned
		VideoFile videoFile = serverList.get(0);
		assertEquals("20120213a2", videoFile.getID());
		assertEquals("Monsters Inc.", videoFile.getTitle());
		assertEquals("monstersinc_high.mpg", videoFile.getFilename());
		// Check second video is returned
		videoFile = serverList.get(1);
		assertEquals("20120102b7", videoFile.getID());
		assertEquals("Avengers", videoFile.getTitle());
		assertEquals("avengers-featurehp.mp4", videoFile.getFilename());
		// Check third video is returned
		videoFile = serverList.get(2);
		assertEquals("20120102b7", videoFile.getID());
		assertEquals("Prometheus", videoFile.getTitle());
		assertEquals("prometheus-featureukFhp.mp4", videoFile.getFilename());
	}

}
