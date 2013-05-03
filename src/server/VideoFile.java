package server;

import java.io.Serializable;

public class VideoFile implements Serializable{	
	private String id, title, filename;
	
	// Constructor
	public VideoFile(){}
	
	// Setters
	public void setId(String id) { this.id = id; }	
	public void setTitle(String title) { this.title = title; }	
	public void setFilename(String filename) { this.filename = filename; }
	
	// Getters
	public Object getID() { return id; }
	public Object getTitle() { return title; }
	public Object getFilename() { return filename; }
}
