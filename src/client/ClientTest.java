package client;

import static org.junit.Assert.*;

import java.util.List;

import javax.swing.JComboBox;

import org.junit.Before;
import org.junit.Test;
import server.VideoFile;
public class ClientTest {
	private Client client;
	@Before
	public void setUp() throws Exception {
		server.Server.main(null);
		client = new Client();
	}
	@Test
	public void videoFileReturnsCorrectValue() {
		VideoFile videoFile = client.getList(0);
		assertEquals("20120213a2", videoFile.getID());
		assertNotNull("Monsters Inc.", videoFile.getTitle());
		assertNotNull("monstersinc_high.mpg", videoFile.getFilename());
	}
	@Test
	public void checkSelectedVideoInList() {
		JComboBox<String> comboBox = client.selectionBox;
		comboBox.setSelectedIndex(2);
		assertEquals("Prometheus", comboBox.getSelectedItem());
	}
}
