package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
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
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
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

/**
 * The client side of the software, makes initial contact
 * with the server on port 1139 and is then moved over to a new
 * set of ports once it has received the list of video files
 * @author rjm529 sc900
 */
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
	private JFrame welcFrame;
	private JFrame videoFrame;
	private JPanel panel;
	private JPanel welcPanel;
	private JPanel videoPanel;
	private JPanel sliderPanel;
	private JPanel contPanel;
	private JSlider gammaSlider;
	private JSlider saturationSlider;
	private JSlider hueSlider;
	private JSlider brightnessSlider;
	private EmbeddedMediaPlayer mediaPlayer;
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private JTextArea consoleText;
	private final static String newline  = "\n";
	private final static int defaultWidth = 600;
	private final static int defaultHeight = 400;
	private boolean settingsOpen = false;
	private boolean isFullscreen = false;
	
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
		welcFrame = new JFrame();
		welcFrame.setVisible(true);
		welcFrame.setSize(400, 300);
		welcFrame.setTitle("Welcome");
		welcPanel = new JPanel();
		consoleText = new JTextArea();
		welcPanel.add(consoleText);
		welcFrame.add(welcPanel, BorderLayout.NORTH);
		consoleText.setBounds(50,50,200,100);
		consoleText.setVisible(true);
		consoleText.setBackground(null);
		consoleText.append("Loading, please wait");
		
		//String vlcLibraryPath = "C:/Program Files (x86)/VideoLAN/VLC";
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		consoleText.append("  ----------------------------------------------------  " + newline);
		consoleText.append("| Select a trailer to play from the menu   |" + newline);
		consoleText.append("  ---------------------------------------------------- " + newline);
		
		connectToServer();

		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		mediaPlayer = mediaPlayerComponent.getMediaPlayer();
		setupGUI();
		setupMediaPlayer();
		welcFrame.dispose();
	}
	

	/**
	 * Connects the client to the server, initially on port 1139 to retrieve the
	 * new port settings a list of videos and is then
	 * transfered to a different set of ports defined by the server
	 */
	private void connectToServer() {
		try {
			//Initial connection
			openSocket();
			//retrieve video List
			getListFromSocket();
			//get new ports
			getPortsFromSocket();
			//release port 1139
			serverSocket.close();
			//open on new port
			openCommSocket(serverComm.getCommPort());
		} catch (UnknownHostException e) {
			System.out.println("Client: Don't know about host : " + host);
		} catch (IOException e) {
			System.out.println("Client: Couldn't open I/O connection : " + host + ":" + port);
		} catch (ClassNotFoundException e) {
			System.out.println("Client: Class definition not found for incoming object.");
			e.printStackTrace();
		}
	}
	/**
	 * @param commPort - the port the client should communicate with the server  on
	 * @throws IOException a connection error may occur, server not running or a network issue
	 */
	private void openCommSocket(int commPort) throws IOException {
		
		serverSocket = new Socket(host, commPort);
		System.out.println("Client: Connected to: " + serverComm.getAddress() + " on port: "+serverComm.getCommPort());
		consoleText.append("Client: Connected to: " + serverComm.getAddress() + " on port: "+serverComm.getCommPort() + newline);
		System.out.println("Client: Ready to recieve video at " + serverComm.getAddress()+ ":" + serverComm.getVideoPort());
		consoleText.append("Client: Ready to recieve video at " + serverComm.getAddress()+ ":" + serverComm.getVideoPort() + newline);
		outputToServer = new ObjectOutputStream(serverSocket.getOutputStream());
	}
	/**
	 * retrieves the new set of ports from the server
	 */
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
		consoleText.append("Client: Ports retrieved" + newline);
		
	}
	public static void main(String[] args) {
		new Client();
	}

	/**
	 * Sets up the window in which the client will play the video and from which video selection can be made
	 */
	public void setupGUI()
	{
		//set up the data for the combo box
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
		frame.setSize(defaultWidth, defaultHeight);
		frame.setTitle("Media Player");

		panel.add(consoleText);
		
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel.add(contPanel, BorderLayout.SOUTH);
		contPanel.add(selectionBox, BorderLayout.NORTH);		
		selectionBox.addActionListener((ActionListener) this);
		frame.addWindowListener(new WindowAdapter()
		{
			//when the window is closed sends a signal to the server and releases all threads
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
			consoleText.append("Client: Selected title : " + serverComm.getVideo().getTitle() + newline);
			consoleText.append("Loading Video: " + serverComm.getVideo().getTitle() + newline);
			consoleText.setVisible(false);
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
			playButton.setVisible(false);
			pauseButton.setVisible(true);
		}
		else if(e.getSource() == fullScreenButton){
			if(isFullscreen){
				frame.setBounds(0, 0, defaultWidth, defaultHeight);
				mediaPlayer.toggleFullScreen();
				isFullscreen = false;
			}
			else if(!isFullscreen){
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				mediaPlayer.toggleFullScreen();
				isFullscreen = true;
			}
		}
		else if(e.getSource() == pauseButton){
			serverComm.setPause(true);
			mediaPlayer.pause();
			playButton.setVisible(true);
			pauseButton.setVisible(false);
			serverComm.setPlay(false);
		}
		else if(e.getSource() == videoSettingsButton){
			if(settingsOpen){
				videoFrame.dispose();
				settingsOpen = false;
			}
			else if(!settingsOpen){
				videoSettingsDialog();
				settingsOpen = true;
			}

		}
		else if(e.getSource() == playButton){
			mediaPlayer.play();
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
		JSlider source = (JSlider)e.getSource();
		if(e.getSource() == volumeSlider){
			mediaPlayer.setVolume(source.getValue());
		}

	}
	
	/**
	 * This method is only used in testing
	 * @param i the index corresponding to the object you want to retrieve
	 * @return the VideoFile corresponding to the index given
	 */
	public VideoFile getList(int i) {
		return videoList.get(i);
	}
	/**
	 * @throws UnknownHostException Occurs when the host is unreachable
	 * @throws IOException there is a problem with setting up the input stream
	 */
	private void openSocket() throws UnknownHostException, IOException {
		serverSocket = new Socket(host, port);
		System.out.println("Connected to: " + host + " on port: "+port);
		consoleText.append("Connected to: " + host + " on port: " + port + newline);
		inputFromServer = new ObjectInputStream(serverSocket.getInputStream());
	}
	
	/**
	 * Retrieves the list of videos from the input stream
	 * @throws IOException problem with the input stream
	 * @throws ClassNotFoundException unable to find VideoFile class
	 */
	private void getListFromSocket() throws IOException, ClassNotFoundException {
		videoList = (List<VideoFile>) inputFromServer.readObject();
		System.out.println("Client: List retrieved from Server");
		consoleText.append("Client: List retrieved from Server" + newline);
	}
	

	/**
	 * Sets up the media player and the GUI elements associated with it
	 */
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

	/**
	 * Sets up a window with settings for the video player
	 */
	public void videoSettingsDialog(){

		videoFrame  = new JFrame();
		videoPanel  = new JPanel();
		sliderPanel = new JPanel();
		
		videoFrame.add(videoPanel);
		videoFrame.setVisible(true);
		videoFrame.setTitle("Video Settings");
		videoFrame.setBounds(frame.getX() + frame.getWidth(), frame.getY(), 200, frame.getHeight());
		
		JLabel hueLabel		   = new JLabel("Hue");
		JLabel brightnessLabel = new JLabel("Brightness");
		JLabel saturationLabel = new JLabel("Saturation");
		JLabel gammaLabel      = new JLabel("Gamma");

		hueSlider = new JSlider();
	    hueSlider.setOrientation(JSlider.HORIZONTAL);
	    hueSlider.setMinimum(LibVlcConst.MIN_HUE);
	    hueSlider.setMaximum(LibVlcConst.MAX_HUE);
	    hueSlider.setPreferredSize(new Dimension(100, 40));
	    hueSlider.setToolTipText("Adjust the hue settings");
	    hueSlider.setEnabled(true);
	    
	    brightnessSlider = new JSlider();
	    brightnessSlider.setOrientation(JSlider.HORIZONTAL);
	    brightnessSlider.setMinimum(Math.round(LibVlcConst.MIN_BRIGHTNESS * 100.0f));
	    brightnessSlider.setMaximum(Math.round(LibVlcConst.MAX_BRIGHTNESS * 100.0f));
	    brightnessSlider.setPreferredSize(new Dimension(100, 40));
	    brightnessSlider.setToolTipText("Change Brightness settings");
	    brightnessSlider.setEnabled(true);
	    
	    saturationSlider = new JSlider();
	    saturationSlider.setOrientation(JSlider.HORIZONTAL);
	    saturationSlider.setMinimum(Math.round(LibVlcConst.MIN_SATURATION * 100.0f));
	    saturationSlider.setMaximum(Math.round(LibVlcConst.MAX_SATURATION * 100.0f));
	    saturationSlider.setPreferredSize(new Dimension(100, 40));
	    saturationSlider.setToolTipText("Change saturation settings");
	    saturationSlider.setEnabled(true);
	    
	    gammaSlider = new JSlider();
	    gammaSlider.setOrientation(JSlider.HORIZONTAL);
	    gammaSlider.setMinimum(Math.round(LibVlcConst.MIN_GAMMA * 100.0f));
	    gammaSlider.setMaximum(Math.round(LibVlcConst.MAX_GAMMA * 100.0f));
	    gammaSlider.setPreferredSize(new Dimension(100, 40));
	    gammaSlider.setToolTipText("Change gamma settings ");
	    gammaSlider.setEnabled(true);
	    
	    brightnessSlider.setValue(Math.round(mediaPlayer.getContrast() * 100.0f));
	    hueSlider.setValue(mediaPlayer.getHue());
	    saturationSlider.setValue(Math.round(mediaPlayer.getSaturation() * 100.0f));
	    gammaSlider.setValue(Math.round(mediaPlayer.getGamma() * 100.0f));
	    mediaPlayer.setAdjustVideo(true);
	    
	    
	    brightnessSlider.addChangeListener(new ChangeListener(){
	    	@Override
	    	public void stateChanged(ChangeEvent e){	    		
	    		JSlider source = (JSlider)e.getSource();
	    		if(e.getSource() == brightnessSlider){
	    			mediaPlayer.setGamma(source.getValue() / 100.0f);
	    		}	    		
	    	}
	    });
	    hueSlider.addChangeListener(new ChangeListener(){
	    	@Override
	    	public void stateChanged(ChangeEvent e){	    		
	    		JSlider source = (JSlider)e.getSource();
	    		if(e.getSource() == hueSlider){
	    			mediaPlayer.setHue(source.getValue());
	    		}	    		
	    	}
	    });
	    saturationSlider.addChangeListener(new ChangeListener(){
	    	@Override
	    	public void stateChanged(ChangeEvent e){	    		
	    		JSlider source = (JSlider)e.getSource();
	    		if(e.getSource() == saturationSlider){
	    			mediaPlayer.setSaturation(source.getValue() / 100.0f);
	    		}	    		
	    	}
	    });
	    gammaSlider.addChangeListener(new ChangeListener(){
	    	@Override
	    	public void stateChanged(ChangeEvent e){	    		
	    		JSlider source = (JSlider)e.getSource();
	    		if(e.getSource() == gammaSlider){
	    			mediaPlayer.setGamma(source.getValue() / 100.0f);
	    		}	    		
	    	}
	    });
	    
	    
	    videoPanel.setLayout(new BorderLayout());
	    sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
	    
	    sliderPanel.add(hueLabel);
	    sliderPanel.add(hueSlider);
	    sliderPanel.add(brightnessLabel);
	    sliderPanel.add(brightnessSlider);
	    sliderPanel.add(gammaLabel);
	    sliderPanel.add(gammaSlider);
	    sliderPanel.add(saturationLabel);
	    sliderPanel.add(saturationSlider);
	    

	    videoPanel.add(sliderPanel, BorderLayout.CENTER);
	
	}

}
