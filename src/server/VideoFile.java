package server;
public class VideoFile {

	private String id, title, filename;

	// Constuctor
	public VideoFile(){}

	// Setters
	public void setId(String id) { this.id = id; } 	
	public void setTitle(String title) { this.title = title; }	
	public void setFilename(String filename) { this.filename = filename; }

	// Getters
	public String getID() { return id; }
	public String getTitle() { return title; }
	public String getFilename() { return filename; }

}
