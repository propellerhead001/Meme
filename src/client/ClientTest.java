package client;

import static org.junit.Assert.*;

import java.util.List;

import javax.swing.JComboBox;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import server.Server;
import server.VideoFile;
public class ClientTest {
	private Client client;
	private static Thread serverThread;
	private static Server server;
	@BeforeClass
	public static void setUp() throws Exception {
		serverThread = new Thread("server"){
			public void run(){
				server = new Server(true);
			}
		};
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
	@Test
	public void checkSelectedVideoInList() {
		client = new Client();
		JComboBox<String> comboBox = client.selectionBox;
		comboBox.setSelectedIndex(2);
		assertEquals("Prometheus", comboBox.getSelectedItem());
	}
}

