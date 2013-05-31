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

/**
 * This class is designed to allow clients to make an initial connection
 * on port 1139, retrieve the video list and then be passed a second set
 * of ports for communication and videos
 * @author rjm529 sc900
 */
public class Server {
	private List<VideoFile> videoList;
	private XMLReader reader;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	ObjectOutputStream outputToClient;
	private int port = 1139;
	private ClientPort clientPort = new ClientPort("127.0.0.1", 1140, 1141);
	private ArrayList <ClientObject> clients = new ArrayList(0);

	/**
	 * Creator
	 * @param multi allows the user to specify single or multi-client server on start up
	 * usually multi-client would be used but there may be exceptions
	 */
	public Server(boolean multi){
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		
		//String vlcLibraryPath = "C:/Program Files (x86)/VideoLAN/VLC";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		reader = new XMLReader();
		videoList = reader.getList("videolist.xml");
		//ensure that at least one client can connect
		do{
			if(!socketThread.isAlive()){
				socketThread.run();
			}
		}while(multi);
	}


	/**
	 * @return returns the video list, used for test purposes
	 */
	public List<VideoFile> getList() { return videoList; }

	public static void main(String[] args) {
		new Server(true);
	}
	
	/**
	 * The thread allowing clients to connect to the server
	 */
	Thread socketThread = new Thread("Socket") {
		public void run() {
			try {
				openSocket(port);
				writeListToSocket();
				writePortToSocket();
				clients.add(new ClientObject(clientPort));
				clientPort = new ClientPort(clientPort.getAddress(),
						clientPort.getCommPort()+2,clientPort.getVideoPort()+2);
				clientSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("ERROR on socket connection.");
				e.printStackTrace();
			}

		}
	};

	/**
	 * opens the initial communication port
	 * @param port the port to open
	 * @throws IOException a problem has occured with the set up of teh socket
	 */
	private void openSocket(int port) throws IOException{
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port : " + port);
			e.printStackTrace();
		}
		System.out.println("Opened socket on : " + port + ", waiting for client.");

		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.out.println("Could not accept client.");
			e.printStackTrace();
		}
		outputToClient = new ObjectOutputStream(clientSocket.getOutputStream());
	}
	/**
	 * Push the new set of ports to the client
	 * @throws IOException an error has occurred with the output stream 
	 */
	protected void writePortToSocket() throws IOException {
		outputToClient.writeObject(clientPort);		
	}

	/**
	 * push the list of videos to the client
	 * @throws IOException an error has occurred with the output stream
	 */
	private void writeListToSocket() throws IOException {
		outputToClient.writeObject(videoList);
	}
}
