package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Server {
	private List<VideoFile> videoList;
	private XMLReader reader;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	ObjectOutputStream outputToClient;
	private int port = 1139;
	private ArrayList<ObjectInputStream> videoToStream = new ArrayList(0);
	private VideoFile fileToStream;
	private ClientPort clientPort = new ClientPort("127.0.0.1", 1140, 1141);
	private ArrayList<ServerSocket> playerSocket= new ArrayList(0);
	private ArrayList<Socket> clientCommSocket= new ArrayList(0);
	private ObjectInputStream clientUpdate;

	public Server(){
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		reader = new XMLReader();
		videoList = reader.getList("videolist.xml");
		socketThread.start();
		playerThread.start();
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
				openSocket(port);
				writeListToSocket();
				writePortToSocket();
				clientSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("ERROR on socket connection.");
				e.printStackTrace();
			}
		}
	};
	Thread playerThread = new Thread("Player"){
		int clientNumber;
		public void run(){
			clientPort.setInUse(true);
			clientNumber = openCommSocket(clientPort.getCommPort());
			while(clientPort.getInUse()){
				try {
					getStatusFromSocket(clientNumber);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(!clientPort.getPlay()){
					try {
						getStatusFromSocket(clientNumber);
						fileToStream = clientPort.getVideo();
						if(!play.isAlive()){
							play.run();
						}
						else{
							play.interrupt();
						}
						System.out.println(fileToStream.getTitle().toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				playerSocket.get(clientNumber).close();
				clientCommSocket.get(clientNumber).close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	Thread play = new Thread("Play"){
		private HeadlessMediaPlayer mediaPlayer;
		String serverAddress = "127.0.0.1";
		String options = formatRtpStream(serverAddress, clientPort.getVideoPort());
		public void run(){
			MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(fileToStream.getFilename().toString());
			mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
			String serverAddress = "127.0.0.1";
			String options = formatRtpStream(serverAddress, clientPort.getVideoPort());
			mediaPlayer.playMedia(fileToStream.getFilename().toString(), options, ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
			// Continue running - "join" waits for current executing thread to finish
			try {
				Thread.currentThread().join();
			} catch (InterruptedException e) {
				System.out.println("Exception thrown whilst streaming.");
				e.printStackTrace();
			}
			System.out.println("finished playing video");
		}
		public  void interrupt(){
			mediaPlayer.playMedia(fileToStream.getFilename().toString(), options, ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
		}
	};

	private int openCommSocket(int commPort) {
		try {
			playerSocket.add(new ServerSocket(commPort));
		} catch (IOException e) {
			System.out.println("Could not listen on port : " + commPort);
			System.exit(-1);
		}
		System.out.println("Opened socket on : " + commPort + ", waiting for client.");
		int index = playerSocket.size()-1;
		try {
			clientCommSocket.add(playerSocket.get(index).accept());
		} catch (IOException e) {
			System.out.println("Could not accept client.");
			System.exit(-1);
		}

		try {
			clientUpdate = new ObjectInputStream(clientCommSocket.get(index).getInputStream());
			videoToStream.add(clientUpdate);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return index;
	}

	protected void getStatusFromSocket(int clientNumber) throws ClassNotFoundException, IOException {
		clientPort = (ClientPort) clientUpdate.readObject();
	}

	private void openSocket(int port) throws IOException{
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
	protected void writePortToSocket() throws IOException {
		outputToClient.writeObject(clientPort);		
	}

	private void writeListToSocket() throws IOException {
		outputToClient.writeObject(videoList);
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
