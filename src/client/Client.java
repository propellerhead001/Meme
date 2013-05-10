package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import server.Server;
import server.VideoFile;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.test.basic.PlayerControlsPanel;

public class Client implements ActionListener {
	private Socket serverSocket;
	private int port = 1140;
	private String host = "127.0.0.1";
	private ObjectInputStream inputFromServer;
	private ObjectOutputStream outputToServer;
	private List<VideoFile> videoList;
	public JComboBox<String> selectionBox;
	private VideoFile videoFile;
	private static JFrame mainFrame;
	
	public Client(){
		try {
			openSocket();
			getListFromSocket();
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
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		setupGUI();
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
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(new BorderLayout());

		selectionBox = new JComboBox<String>(selectionListData);
		selectionBox.setSelectedIndex(0);

		frame.add(panel);
		frame.setVisible(true);
		frame.setSize(600,400);
		frame.setTitle("A Client");

		panel.add(selectionBox, BorderLayout.NORTH);		
		selectionBox.addActionListener((ActionListener) this);
	}
	
	public void actionPerformed(ActionEvent e) {
		JComboBox<String>comboBox = (JComboBox<String>)e.getSource();
		String selectedTitle = (String)comboBox.getSelectedItem();
		System.out.println("Selected title : " + selectedTitle);
		videoFile = videoList.get(comboBox.getSelectedIndex());
		if(mainFrame != null){
			mainFrame.dispose();
		}
		
		try {
			outputToServer.writeObject(videoFile);
			serverSocket.close();
		} catch (IOException e1) {
			System.out.println("Failed to send selection / close socket");
			e1.printStackTrace();
		}
		playVideo("rtp://@127.0.0.1:5555");
	}


	public VideoFile getList(int i) {
		return videoList.get(i);
	}
	private void openSocket() throws UnknownHostException, IOException {
		serverSocket = new Socket(host, port);
		System.out.println("Connected to: " + host + " on port: "+port);
		inputFromServer = new ObjectInputStream(serverSocket.getInputStream());
		outputToServer = new ObjectOutputStream(serverSocket.getOutputStream());
	}
	private void getListFromSocket() throws IOException, ClassNotFoundException {
		videoList = (List<VideoFile>) inputFromServer.readObject();
		System.out.println("List retrieved");
	}
	private void playVideo(String media){
		mainFrame = new JFrame();
		JPanel panel = new JPanel(new BorderLayout());
		
		mainFrame.add(panel);
		mainFrame.setVisible(true);
		mainFrame.setSize(600,400);
		mainFrame.setTitle("Player");
		
		final EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		panel.add(mediaPlayerComponent, BorderLayout.CENTER);
		EmbeddedMediaPlayer mediaPlayer = mediaPlayerComponent.getMediaPlayer();
		PlayerControlsPanel controlsPanel = new PlayerControlsPanel(mediaPlayer);
		panel.add(controlsPanel, BorderLayout.SOUTH);
		mediaPlayer.playMedia(media);
	}
}
