package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import server.VideoFile;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Client implements ActionListener {
	private Socket serverSocket;
	private int port = 1135;
	private String host = "127.0.0.1";
	private ObjectInputStream inputFromServer;
	private List<VideoFile> videoList;
	public JComboBox<String> selectionBox;
	
	public Client(){
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
		String vlcLibraryPath = "N:/examples/java/Year2/SWEng/VLC/vlc-2.0.1";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibraryPath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		setupGUI();
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
		
		final EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		frame.setContentPane(mediaPlayerComponent);
	}
	
	public void actionPerformed(ActionEvent e) {
		JComboBox<String>comboBox = (JComboBox<String>)e.getSource();
		String selectedTitle = (String)comboBox.getSelectedItem();
		System.out.println("Selected title : " + selectedTitle);
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
}
