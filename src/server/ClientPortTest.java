package server;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ClientPortTest {
	private ClientPort portSettings = new ClientPort("127.0.0.1", 1140, 1141);
	@Before
	public void setPortSetting(){
		portSettings.setInUse(true);
	}
	@Test
	public void getPortSettings() {
		assertEquals("127.0.0.1", portSettings.getAddress());
		assertEquals(1140, portSettings.getCommPort());
		assertEquals(1141, portSettings.getVideoPort());
		assertEquals(true, portSettings.getInUse());
		
	}

}
