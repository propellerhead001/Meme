package server;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ServerTest {
	private Server	server;
	private List<VideoFile> serverList;
	@Test
	public void serverGetsList(){
		server = new Server();
		serverList = server.getList();
		assertTrue(serverList instanceof List);
		VideoFile videoFile = serverList.get(0);
		assertEquals("20120213a2", videoFile.getID());
		assertNotNull("Monsters Inc.", videoFile.getTitle());
		assertNotNull("monstersinc_high.mpg", videoFile.getFilename());
		videoFile = serverList.get(1);
		assertEquals("20120102b7", videoFile.getID());
		assertNotNull("Avengers", videoFile.getTitle());
		assertNotNull("avengers-featurehp.mp4", videoFile.getFilename());
	}

}
