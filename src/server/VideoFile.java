package server;
public class VideoFile {
	private String id, title, filename;
	public void setId(String id) {
		this.id = id;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public VideoFile(){
	}

	public Object getID() {
		return id;
	}

	public Object getTitle() {
		return title;
	}

	public Object getFilename() {
		return filename;
	}

}
