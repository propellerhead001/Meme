package server;

import java.io.Serializable;

public class ClientPort implements Serializable {
	private boolean inUse, isplaying, play, pause, ffwd, rwd, stop;
	private String address;
	private int commPort;
	private int videoPort;
	private VideoFile video;
	public ClientPort(String string, int i, int j) {
		address = string;
		commPort = i;
		videoPort = j;
		inUse = false;
		isplaying = false;//checks for changes to video
		play = false;
		pause = false;
		ffwd = false;
		rwd = false;
		stop = false;
	}
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
