package client;

import static org.junit.Assert.*;

import java.util.List;

import javax.swing.JComboBox;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import server.Server;
import server.VideoFile;
public class ClientTestVidFile {
	private Client client;
	private static Server server;
	private static Thread serverThread = new Thread("server"){
		public void run(){
			server = new Server(true);
		}
	};
	@Before
	public void setUp() throws Exception {
		serverThread.start();
	}
	@Test
	public void videoFileReturnsCorrectValue() {
		client = new Client();
		VideoFile videoFile = client.getList(0);
		assertEquals("20120213a2", videoFile.getID());
		assertNotNull("Monsters Inc.", videoFile.getTitle());
		assertNotNull("monstersinc_high.mpg", videoFile.getFilename());
	}
}

