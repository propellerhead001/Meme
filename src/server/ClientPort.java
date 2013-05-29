package server;

import java.io.Serializable;

public class ClientPort implements Serializable {
	private boolean inUse;
	private boolean play;
	private String address;
	private int commPort;
	private int videoPort;
	private VideoFile video;
	public ClientPort(String string, int i, int j) {
		address = string;
		commPort = i;
		videoPort = j;
		inUse = false;
		play = false;
	}
	public void setVideo(VideoFile v){
		video = v;
	}
	public void setInUse(boolean b) {
		inUse = b;
	}
	public void setPlay(boolean b){
		play = b;
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
	public boolean getPlay(){
		return play;
	}
	public VideoFile getVideo(){
		return video;
	}

}
