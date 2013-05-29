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
	private ClientObject clients;

	public Server(){
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		reader = new XMLReader();
		videoList = reader.getList("videolist.xml");
		socketThread.run();
	}


	public List<VideoFile> getList() {
		return videoList;
	}

	public static void main(String[] args) {
		new Server();
	}
	Thread socketThread = new Thread("Socket") {
		public void run() {
			while(true){
				try {
					openSocket(port);
					writeListToSocket();
					writePortToSocket();
					clients = new ClientObject(clientPort);
					clientSocket.close();
					serverSocket.close();
				} catch (IOException e) {
					System.out.println("ERROR on socket connection.");
					e.printStackTrace();
				}
			}
		}
	};

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
}
