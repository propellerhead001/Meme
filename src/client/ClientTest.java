package client;

import static org.junit.Assert.*;

import java.util.List;

import javax.swing.JComboBox;

import org.junit.Before;
import org.junit.Test;
import server.VideoFile;
public class ClientTest {
	private Client client;
	private Thread serverThread = new Thread("server"){
		public void run(){
			server.Server.main(null);
		}
	};
	@Before
	public void setUp() throws Exception {
		serverThread.start();
		client = new Client();
	}
	@Test
	public void videoFileReturnsCorrectValue() {
		if(!serverThread.isAlive()){
			serverThread.start();
		}
		VideoFile videoFile = client.getList(0);
		assertEquals("20120213a2", videoFile.getID());
		assertNotNull("Monsters Inc.", videoFile.getTitle());
		assertNotNull("monstersinc_high.mpg", videoFile.getFilename());
	}
	@Test
	public void checkSelectedVideoInList() {
		if(!serverThread.isAlive()){
			serverThread.start();
		}
		JComboBox<String> comboBox = client.selectionBox;
		comboBox.setSelectedIndex(2);
		assertEquals("Prometheus", comboBox.getSelectedItem());
	}
}

