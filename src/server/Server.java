package server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
	private List<VideoFile> videoList;
	private XMLReader reader;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	ObjectOutputStream outputToClient;
	private int port = 1135;

	public Server(){
		reader = new XMLReader();
		videoList = reader.getList("videolist.xml");
		socketThread.start();
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
}
