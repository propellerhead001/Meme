package server;

import java.io.Serializable;

/**
 * Contains the data to be passed back and forth between the client and server
 * in order to facilitate play back
 * @author rjm529 sc900
 * @param isplaying this parameter is set true when a change has been made
 * to the client of which the server needs to be notified
 */
public class ClientPort implements Serializable {
	private boolean inUse, isplaying, play, pause, ffwd, rwd, stop;
	private String address;
	private int commPort;
	private int videoPort;
	private VideoFile video;
	//Constructor
	public ClientPort(String string, int i, int j) {
		address = string;
		commPort = i;
		videoPort = j;
		inUse = false;
		isplaying = false;// set true if changes have been made
		play = false;
		pause = false;
		ffwd = false;
		rwd = false;
		stop = false;
	}
	//setters and getters
	public void clearButtons(){
		play = false;
		pause = false;
		ffwd = false;
		rwd = false;
		stop = false;
	}
	public boolean isPlayB(){
		return play;
	}
	public void setPlayB(boolean play){
		this.play = play;
	}
	public boolean isPause() {
		return pause;
	}
	public void setPause(boolean pause) {
		this.pause = pause;
	}
	public boolean isFfwd() {
		return ffwd;
	}
	public void setFfwd(boolean ffwd) {
		this.ffwd = ffwd;
	}
	public boolean isRwd() {
		return rwd;
	}
	public void setRwd(boolean rwd) {
		this.rwd = rwd;
	}
	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public void setVideo(VideoFile v){
		video = v;
	}
	public void setInUse(boolean b) {
		inUse = b;
	}
	public void setPlay(boolean b){
		isplaying = b;
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
		return isplaying;
	}
	public VideoFile getVideo(){
		return video;
	}

}
