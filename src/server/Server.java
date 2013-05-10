package server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Server {
	private List<VideoFile> videoList;
	private XMLReader reader;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	ObjectOutputStream outputToClient;
	private int port = 1135;

	public Server(){
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		reader = new XMLReader();
		videoList = reader.getList("videolist.xml");
		socketThread.start();
		streamVideo(videoList.get(1).getFilename().toString());
	}

	public List<VideoFile> getList() {
		return videoList;
	}

	public static void main(String[] args) {
		new Server();
	}
	Thread socketThread = new Thread("Socket") {
		public void run() {
			try {
				openSocket();
				writeListToSocket();
				clientSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("ERROR on socket connection.");
				e.printStackTrace();
			}
		}
	};

	private void openSocket() throws IOException{
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port : " + port);
			System.exit(-1);
		}
		System.out.println("Opened socket on : " + port + ", waiting for client.");

		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.out.println("Could not accept client.");
			System.exit(-1);
		}
		outputToClient = new ObjectOutputStream(clientSocket.getOutputStream());
	}
	private void writeListToSocket() throws IOException {
		outputToClient.writeObject(videoList);
	}
	private void streamVideo(String media){
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(media);
		HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
		
		String serverAddress = "127.0.0.1";
		String options = formatRtpStream(serverAddress, 5555);
		mediaPlayer.playMedia(media, options, ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
		// Continue running - "join" waits for current executing thread to finish
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			System.out.println("Exception thrown whilst streaming.");
			e.printStackTrace();
		}
	}
	private String formatRtpStream(String serverAddress, int serverPort) {
		StringBuilder sb = new StringBuilder(60);
		sb.append(":sout=#rtp{dst=");
		sb.append(serverAddress);
		sb.append(",port=");
		sb.append(serverPort);
		sb.append(",mux=ts}");
		return sb.toString();
	}
}
