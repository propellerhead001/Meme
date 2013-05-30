package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import server.ClientPort;
import server.VideoFile;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.LibVlcConst;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.test.basic.PlayerControlsPanel;

public class Client implements ActionListener, ChangeListener {
	// Connection Elements
	private Socket serverSocket;
	private int port = 1139;
	private String host = "127.0.0.1";
	private ObjectInputStream inputFromServer;
	private ObjectOutputStream outputToServer;
	private ClientPort serverComm;
	
	// Internal Elements
	private List<VideoFile> videoList;
	public JComboBox<String> selectionBox;
	private VideoFile videoFile;
	
	// GUI Elements
	private JFrame frame;
	private JPanel panel;
	private JPanel contPanel;
	private EmbeddedMediaPlayer mediaPlayer;
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	// -- Control Panels Buttons
	private JButton fullScreenButton;
	private JButton playButton;
	private JButton pauseButton;	
	private JButton rewindButton;
	private JButton ffwButton;
	private JButton stopButton;
	private JButton videoSettingsButton;
	
	// pertaining to volume settings
	private JButton muteVolumeButton;
	private JSlider volumeSlider;	
	private ImageIcon soundMuteIcon;
	private ImageIcon soundIcon;	
	private boolean isMute = false;
	private int soundLevel = 1;
	
	public Client(){
		//String vlcLibraryPath = "C:/Program Files (x86)/VideoLAN/VLC";
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		connectToServer();

		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		mediaPlayer = mediaPlayerComponent.getMediaPlayer();
		setupGUI();
		setupMediaPlayer();
	}
	
	private void connectToServer() {
		try {
			openSocket();
			getListFromSocket();
			getPortsFromSocket();
			serverSocket.close();
			openCommSocket(serverComm.getCommPort());
		} catch (UnknownHostException e) {
			System.out.println("Client: Don't know about host : " + host);
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Client: Couldn't open I/O connection : " + host + ":" + port);
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			System.out.println("Client: Class definition not found for incoming object.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	private void openCommSocket(int commPort) throws IOException {
		
		serverSocket = new Socket(host, commPort);
		System.out.println("Client: Connected to: " + serverComm.getAddress() + " on port: "+serverComm.getCommPort());
		System.out.println("Client: Ready to recieve video at " + serverComm.getAddress()+ ":" + serverComm.getVideoPort());
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
		serverComm.setInUse(true);
		System.out.println("Client: Ports retrieved");
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
		contPanel = new JPanel(new BorderLayout());
		selectionBox = new JComboBox<String>(selectionListData);
		selectionBox.setSelectedIndex(0);

		frame.add(panel);
		frame.setVisible(true);
		frame.setSize(600,400);
		frame.setTitle("Media Player");
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.add(contPanel, BorderLayout.SOUTH);
		contPanel.add(selectionBox, BorderLayout.NORTH);		
		selectionBox.addActionListener((ActionListener) this);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event){
				serverComm.setInUse(false);
				try {
					outputToServer.reset();
					outputToServer.writeObject(serverComm);
					mediaPlayer.stop();
					mediaPlayer.release();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					Thread.sleep(1000);
					} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				frame.dispose();
				System.exit(0);
			}
		});
	}
	
	public void actionPerformed(ActionEvent e) {
		// If the selection box changes, output the selection to the server
		if(e.getSource() == selectionBox){
			serverComm.setPlay(false);
			JComboBox<String>comboBox = (JComboBox<String>)e.getSource();
			videoFile = videoList.get(comboBox.getSelectedIndex());

			serverComm.setVideo(videoFile);
			System.out.println("Client: Selected title : " + serverComm.getVideo().getTitle());
			try {
				outputToServer.reset();
				outputToServer.writeObject(serverComm);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(mediaPlayer.isPlaying()){
				mediaPlayer.stop();
			}
			mediaPlayer.playMedia(("rtp://@127.0.0.1:"+ Integer.toString(serverComm.getVideoPort())));
		}
		else if(e.getSource() == fullScreenButton){
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			mediaPlayer.toggleFullScreen();
		}
		else if(e.getSource() == pauseButton){
			serverComm.setPause(true);
			playButton.setVisible(true);
			pauseButton.setVisible(false);
			serverComm.setPlay(false);
		}
		else if(e.getSource() == playButton){
			serverComm.setPlayB(true);
			playButton.setVisible(false);
			pauseButton.setVisible(true);
			serverComm.setPlay(false);
		}
		else if(e.getSource() == stopButton){
			serverComm.setStop(true);
			playButton.setVisible(true);
			pauseButton.setVisible(false);
			serverComm.setPlay(false);
		}
		else if(e.getSource() == ffwButton){
			// Insert call to server to skip 10s
			serverComm.setFfwd(true);
			serverComm.setPlay(false);
		}
		else if(e.getSource() == rewindButton){
			// Insert call to server to skip back 10
			serverComm.setRwd(true);
			serverComm.setPlay(false);
		}
		else if(e.getSource() == muteVolumeButton){
			if(!isMute){
				soundLevel = volumeSlider.getValue();
				volumeSlider.setValue(LibVlcConst.MIN_VOLUME);
				muteVolumeButton.setIcon(soundMuteIcon);
				isMute = true;
			}
			else if(isMute){
				volumeSlider.setValue(soundLevel);
				muteVolumeButton.setIcon(soundIcon);
				isMute = false;
			}
		}
		//send details to server
		try {
			outputToServer.reset();
			outputToServer.writeObject(serverComm);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		serverComm.setPlay(true);
		serverComm.clearButtons();
		try {
			outputToServer.writeObject(serverComm);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void stateChanged(ChangeEvent e){
		if(e.getSource() == volumeSlider){
			JSlider source = (JSlider)e.getSource();
			mediaPlayer.setVolume(source.getValue());
		}
	}
	private void requestVideoFromServer() {
		try {
			serverComm.setPlay(false);
			outputToServer.reset();
			outputToServer.writeObject(serverComm);
		} catch (IOException e1) {
			System.out.println("Client: Failed to send selection");
			e1.printStackTrace();
		}
		serverComm.setPlay(true);
		try {
			outputToServer.writeObject(serverComm);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(mediaPlayer.isPlaying()){
			mediaPlayer.stop();
			serverComm.setPlay(false);
		}
		mediaPlayer.playMedia(("rtp://@127.0.0.1:"+ Integer.toString(serverComm.getVideoPort())));
		serverComm.setPlay(true);	
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
		System.out.println("Client: List retrieved from Server");
	}
	

	protected void setupMediaPlayer(){
		PlayerControlsPanel controlsPanel = new PlayerControlsPanel(mediaPlayer);	
		
		// Control Panel components
		JPanel bottomPanelLeft = new JPanel();
		JPanel bottomPanelRight = new JPanel();
		JPanel bottomPanelCentre = new JPanel();
		
		EmptyBorder borderSize = new EmptyBorder(0,0,0,0);
		
		controlsPanel.setLayout(new BorderLayout());
		bottomPanelLeft.setLayout(new FlowLayout());
		bottomPanelRight.setLayout(new FlowLayout());
		bottomPanelCentre.setLayout(new FlowLayout());
		
		panel.setBorder(borderSize);
		controlsPanel.setBorder(borderSize);
		bottomPanelLeft.setBorder(borderSize);
		bottomPanelCentre.setBorder(borderSize);
		bottomPanelRight.setBorder(borderSize);
				
		// Button to show full screen video
		fullScreenButton = new JButton();
		fullScreenButton.setPreferredSize(new Dimension(25, 25));
		fullScreenButton.setIcon(new ImageIcon("icons/arrow_out.png"));
		fullScreenButton.setToolTipText("Full Screen Video");
		fullScreenButton.addActionListener((ActionListener) this);
		
		// Rewind Button	
		rewindButton = new JButton();
		rewindButton.setPreferredSize(new Dimension(25, 25));
		rewindButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_rewind_blue.png")));
		rewindButton.setToolTipText("Rewind Video");
		rewindButton.addActionListener((ActionListener) this);
		
		// Stop Button
		stopButton = new JButton();
		stopButton.setPreferredSize(new Dimension(25, 25));
		stopButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_stop_blue.png")));
		stopButton.setToolTipText("Stop Video");
		stopButton.addActionListener((ActionListener) this);
		
		// Pause Button
		pauseButton = new JButton();
		pauseButton.setPreferredSize(new Dimension(35, 35));
		pauseButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_pause_blue.png")));
		pauseButton.setToolTipText("Pause Video");
		pauseButton.addActionListener((ActionListener) this);
		pauseButton.setVisible(true);
		
		// Play Button
		playButton = new JButton();
		playButton.setPreferredSize(new Dimension(35, 35));
		playButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_play_blue.png")));
		playButton.setToolTipText("Resume Playback");
		playButton.addActionListener((ActionListener) this);
		playButton.setVisible(false);
		
		// Fast Forward Button
		ffwButton = new JButton();
		ffwButton.setPreferredSize(new Dimension(25, 25));
		ffwButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_fastforward_blue.png")));
		ffwButton.setToolTipText("Fast forward video");
		ffwButton.addActionListener((ActionListener) this);
		
		// Video Settings Button
		videoSettingsButton = new JButton();
		videoSettingsButton.setPreferredSize(new Dimension(25, 25));
		videoSettingsButton.setIcon(new ImageIcon("icons/film_edit.png"));
		videoSettingsButton.setToolTipText("Change Video Settings");
		videoSettingsButton.addActionListener((ActionListener) this);
		
		// Mute Volume Button
		soundMuteIcon = new ImageIcon("icons/sound_mute.png");
		soundIcon = new ImageIcon("icons/sound.png");
		muteVolumeButton = new JButton();
		muteVolumeButton.setPreferredSize(new Dimension(25, 25));
		muteVolumeButton.setIcon(soundIcon);
		muteVolumeButton.setToolTipText("Mute Volume");
		muteVolumeButton.addActionListener((ActionListener) this);
		
		// Volume Slider
		volumeSlider = new JSlider();
		volumeSlider.setMaximum(LibVlcConst.MAX_VOLUME);
		volumeSlider.setMinimum(LibVlcConst.MIN_VOLUME);
		volumeSlider.setOrientation(JSlider.HORIZONTAL);
		volumeSlider.setPreferredSize(new Dimension(100, 25));
		volumeSlider.setToolTipText("Adjust the volume");
		volumeSlider.setValue(mediaPlayer.getVolume());
		volumeSlider.addChangeListener((ChangeListener) this);
		
		// Add buttons to Control Panel
		controlsPanel.add(bottomPanelLeft, BorderLayout.WEST);
		controlsPanel.add(bottomPanelCentre, BorderLayout.CENTER);
		controlsPanel.add(bottomPanelRight, BorderLayout.EAST);
		
		bottomPanelLeft.add(pauseButton);
		bottomPanelLeft.add(playButton);
		bottomPanelLeft.add(Box.createRigidArea(new Dimension(2,0)));
		bottomPanelLeft.add(rewindButton);
		bottomPanelLeft.add(stopButton);
		bottomPanelLeft.add(ffwButton);		
		bottomPanelLeft.add(Box.createRigidArea(new Dimension(1,0)));
		bottomPanelLeft.add(fullScreenButton);
		bottomPanelLeft.add(videoSettingsButton);

		
		bottomPanelRight.add(muteVolumeButton);
		bottomPanelRight.add(volumeSlider);
		
		// Add panels to Main window
		panel.add(mediaPlayerComponent, BorderLayout.CENTER);
		contPanel.add(controlsPanel, BorderLayout.SOUTH);
		}


		private void videoSettingsDialog(){
			JFrame videoSettingsFrame = new JFrame();
			
		}



}
