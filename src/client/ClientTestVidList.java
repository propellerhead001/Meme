package client;

import static org.junit.Assert.*;

import javax.swing.JComboBox;

import org.junit.Before;
import org.junit.Test;

import server.Server;

public class ClientTestVidList {
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
	public void checkSelectedVideoInList() {
		client = new Client();
		JComboBox<String> comboBox = client.selectionBox;
		comboBox.setSelectedIndex(2);
		assertEquals("Prometheus", comboBox.getSelectedIndex());
	}
}
