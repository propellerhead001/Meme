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
		playerThread.run();
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
			cleanClient = getStatusFromSocket();
			HeadlessMediaPlayer mediaPlayer;
			String serverAddress = clientPort.getAddress();
			try {
				playerSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while(true){
				try {
					playerSocket.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cleanClient = getStatusFromSocket();
				try {
					playerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String options = formatRtpStream(serverAddress, clientPort.getVideoPort());
				MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(cleanClient.getVideo().getFilename().toString());
				mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
				mediaPlayer.stop();
				mediaPlayer.playMedia(cleanClient.getVideo().getFilename().toString(), options,
						":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
				if(true){
					System.out.println(cleanClient.getVideo().getTitle().toString());
					mediaPlayerFactory = new MediaPlayerFactory(cleanClient.getVideo().getFilename().toString());
					mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
					mediaPlayer.stop();
					mediaPlayer.playMedia(cleanClient.getVideo().getFilename().toString(), options,
							":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
				}
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

	protected ClientPort getStatusFromSocket() {
		try {
			clientPort =(ClientPort) clientUpdate.readObject();
			return clientPort;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return clientPort;
		}
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
