package server;

import java.io.Serializable;

public class ClientPort implements Serializable {
	private boolean inUse;
	private String address;
	private int commPort;
	private int videoPort;
	public ClientPort(String string, int i, int j) {
		address = string;
		commPort = i;
		videoPort = j;
		inUse = false;
	}

	public void setInUse(boolean b) {
		inUse = b;
	}

	public String getAddress() {
		return address;
	}

	public int getCommPort() {
		return commPort;
	}

	public boolean getInUse() {
		return inUse;
	}

	public int getVideoPort() {
		return videoPort;
	}

}
