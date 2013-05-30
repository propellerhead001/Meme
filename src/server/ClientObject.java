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


import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

public class ClientObject {
	ClientObject(ClientPort clientDetails){
		clientPort = clientDetails;
		playerThread.start();
	}
	ObjectOutputStream outputToClient;
	private ClientPort clientPort;
	private ServerSocket playerSocket;
	private Socket clientCommSocket = new Socket();
	private ObjectInputStream clientUpdate;
	private ClientPort cleanClient;
	Thread playerThread = new Thread("Player"){
		public void run(){
			openCommSocket(clientPort.getCommPort());
			clientPort.setInUse(true);
			try {
				cleanClient = getStatusFromSocket();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			cleanClient.setInUse(true);
			HeadlessMediaPlayer mediaPlayer;
			MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(cleanClient.getVideo().getFilename().toString());
			mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
			String serverAddress = clientPort.getAddress();
			try {
				cleanClient = getStatusFromSocket();
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				cleanClient.setInUse(false);
			}

			while(cleanClient.getInUse()){
				try{
					cleanClient = getStatusFromSocket();
				} catch (ClassNotFoundException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					cleanClient.setInUse(false);
				}
				String options = formatRtpStream(serverAddress, clientPort.getVideoPort());
				if(!cleanClient.getPlay()){
					System.out.println(cleanClient.getVideo().getTitle().toString());
					mediaPlayer.stop();
					mediaPlayer.playMedia(cleanClient.getVideo().getFilename().toString(), options,
							":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
				}
			}
			try {
				playerSocket.close();
				clientCommSocket.close();
				System.out.println("Client at " + clientPort.getAddress() + " has closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	};
	private void openCommSocket(int commPort) {
		try {
			playerSocket = new ServerSocket(commPort);
		} catch (IOException e) {
			System.out.println("Could not listen on port : " + commPort);
			System.exit(-1);
		}
		System.out.println("Opened socket on : " + commPort + ", waiting for client.");
		try {
			clientCommSocket = playerSocket.accept();
			clientUpdate =new ObjectInputStream(clientCommSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Could not accept client.");
			System.exit(-1);
		}
	}

	protected ClientPort getStatusFromSocket() throws ClassNotFoundException, IOException {
			clientPort =(ClientPort) clientUpdate.readObject();
			return clientPort;
		
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
