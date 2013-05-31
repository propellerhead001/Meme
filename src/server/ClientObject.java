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

/**
 * Handles an individual Client's requests regarding video play and actioning
 * those buttons which affect play back (play, pause etc.) 
 * @author rjm529 sc900
 */
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
		/* (non-Javadoc)
		 * This Thread is non-blocking on the Thread which calls it
		 * it is designed to check for changes to the  ClientPort object
		 * it receives from the client and if any changes are made perform and
		 * action based on the change
		 * @see java.lang.Thread#run()
		 */
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
			//set up headless media player
			HeadlessMediaPlayer mediaPlayer;
			MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(cleanClient.getVideo().getFilename().toString());
			mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
			String serverAddress = clientPort.getAddress();
			try {
				cleanClient = getStatusFromSocket();
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			cleanClient.setInUse(true);
			//check for live client
			while(cleanClient.getInUse()){
				String options = formatRtpStream(serverAddress, clientPort.getVideoPort());
				try{
					cleanClient = getStatusFromSocket();
				} catch (ClassNotFoundException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					//if the client closes with out informing the server then the loop exits
					cleanClient.setInUse(false);
				}
				if(!cleanClient.getPlay()){
					if(cleanClient.isPause()){
						mediaPlayer.pause();
						System.out.println("pause");
					}
					else if(cleanClient.isStop()){
						mediaPlayer.stop();
						System.out.println("stop");
					}
					else if(cleanClient.isFfwd()){
						mediaPlayer.skip(10000);
						System.out.println("ffwd");
					}
					else if(cleanClient.isRwd()){
						mediaPlayer.skip(-10000);
						System.out.println("rwd");
					}
					else if(cleanClient.isPlayB()){
						mediaPlayer.play();
						System.out.println("play");
					}
					else{
						/*
						 * if this statement is reached then the a video
						 * request must have been made so change the video
						 */
						System.out.println(cleanClient.getVideo().getTitle().toString());
						mediaPlayer.playMedia(cleanClient.getVideo().getFilename().toString(), options,
								":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
					}
				}
			}
			//on loop exit stops media player and releases its thread
			mediaPlayer.stop();
			mediaPlayer.release();
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
	/**
	 * Sets up the communication socket with the client
	 * @param commPort the port on which to set up the socket
	 */
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

	/**
	 * @return gets the ClientPort object from the client 
	 * @throws ClassNotFoundException Class error
	 * @throws IOException problem with the socket input stream
	 */
	protected ClientPort getStatusFromSocket() throws ClassNotFoundException, IOException {
		clientPort =(ClientPort) clientUpdate.readObject();
		return clientPort;

	}
	/**
	 * sets up the parameters for the video stream, produced by Stuart Porter 
	 * @param serverAddress the IP Address of the Server
	 * @param serverPort the port the video should be played on
	 * @return
	 */
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
