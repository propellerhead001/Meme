package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JComboBox;

import server.VideoFile;

public class Client {
	private Socket serverSocket;
	private int port = 1138;
	private String host = "127.0.0.1";
	private ObjectInputStream inputFromServer;
	private List<VideoFile> videoList;
	public JComboBox selectionBox;
	public VideoFile getList(int i) {
		try {
			openSocket();
			getListFromSocket();
			serverSocket.close();
		} catch (UnknownHostException e) {
			System.out.println("Don't know about host : " + host);
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Couldn't open I/O connection : " + host + ":" + port);
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			System.out.println("Class definition not found for incoming object.");
					e.printStackTrace();
					System.exit(-1);
		}
		return videoList.get(i);
	}
	private void openSocket() throws UnknownHostException, IOException {
		serverSocket = new Socket(host, port);
		System.out.println("Connected to: " + host + " on port: "+port);
		inputFromServer = new ObjectInputStream(serverSocket.getInputStream());
	}
	private void getListFromSocket() throws IOException, ClassNotFoundException {
		videoList = (List<VideoFile>) inputFromServer.readObject();
		System.out.println("List retrieved");
	}
}
