package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import server.ClientPort;
import server.VideoFile;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.test.basic.PlayerControlsPanel;

public class Client implements ActionListener {
	private Socket serverSocket;
	private int port = 1139;
	private String host = "127.0.0.1";
	private ObjectInputStream inputFromServer;
	private ObjectOutputStream outputToServer;
	private List<VideoFile> videoList;
	public JComboBox<String> selectionBox;
	private VideoFile videoFile;
	private static JFrame mainFrame;
	private JFrame frame;
	private JPanel panel;
	private ClientPort serverComm;
	
	public Client(){
		connectToServer();
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		setupGUI();
	}
	private void connectToServer() {
		try {
			openSocket();
			getListFromSocket();
			getPortsFromSocket();
			serverSocket.close();
			openCommSocket(serverComm.getCommPort());
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
	}
	private void openCommSocket(int commPort) throws IOException {
		
		serverSocket = new Socket(host, commPort);
		System.out.println("Connected to: " + host + " on port: "+port);
		outputToServer = new ObjectOutputStream(serverSocket.getOutputStream());
	}
	private void getPortsFromSocket() {
		try {
			serverComm = (ClientPort) inputFromServer.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ports retrieved");
	}
	public static void main(String[] args) {
		new Client();
	}

	public void setupGUI()
	{
		String [] selectionListData = new String[videoList.size()];
		for(int i = 0; i < videoList.size(); i++)
		{
			selectionListData[i] = videoList.get(i).getTitle().toString();
		}
		frame = new JFrame();
		panel = new JPanel(new BorderLayout());

		selectionBox = new JComboBox<String>(selectionListData);
		selectionBox.setSelectedIndex(0);

		frame.add(panel);
		frame.setVisible(true);
		frame.setSize(600,400);
		frame.setTitle("A Client");

		panel.add(selectionBox, BorderLayout.NORTH);		
		selectionBox.addActionListener((ActionListener) this);
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(){
				try {
					outputToServer.writeObject(serverComm);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				serverComm.setInUse(false);
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
		});
	}
	
	public void actionPerformed(ActionEvent e) {
		JComboBox<String>comboBox = (JComboBox<String>)e.getSource();
		String selectedTitle = (String)comboBox.getSelectedItem();
		videoFile = videoList.get(comboBox.getSelectedIndex());
		serverComm.setVideo(videoFile);
		System.out.println("Selected title : " + serverComm.getVideo().getTitle());
		if(mainFrame != null){
			mainFrame.dispose();
		}
		requestVideoFromServer();
		try {
			outputToServer.writeObject(serverComm);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	private void requestVideoFromServer() {
		try {
			serverComm.setPLay(false);
			outputToServer.reset();
			outputToServer.writeObject(serverComm);
		} catch (IOException e1) {
			System.out.println("Failed to send selection");
			e1.printStackTrace();
		}
		serverComm.setPLay(true);
		try {
			outputToServer.writeObject(serverComm);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		playVideo("rtp://@127.0.0.1:"+ Integer.toString(serverComm.getVideoPort()));
	}


	public VideoFile getList(int i) {
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
	private void playVideo(String media){
		serverComm.setPLay(true);
		final EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		panel.add(mediaPlayerComponent, BorderLayout.CENTER);
		EmbeddedMediaPlayer mediaPlayer = mediaPlayerComponent.getMediaPlayer();
		PlayerControlsPanel controlsPanel = new PlayerControlsPanel(mediaPlayer);
		panel.add(controlsPanel, BorderLayout.SOUTH);
		mediaPlayer.playMedia(media);
	}
}
