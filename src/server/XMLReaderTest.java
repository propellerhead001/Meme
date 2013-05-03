package server;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class XMLReaderTest {
	private XMLReader reader;
	private List<VideoFile> videoList;
	
	@Before
	public void setUp() throws Exception {
		reader = new XMLReader();
		videoList = reader.getList("videoList.xml");
	}
	@Test
	public void createListOfVideos() {
		assertTrue(videoList instanceof List);
	}
	@Test
	public void listContainsVideoFiles() {
		assertTrue(videoList.get(0) instanceof VideoFile);
	}
	@Test
	public void videoFileReturnsCorrectFields() {
		// Check first video is returned
		VideoFile videoFile = videoList.get(0);
		assertEquals("20120213a2", videoFile.getID());
		assertEquals("Monsters Inc.", videoFile.getTitle());
		assertEquals("monstersinc_high.mpg", videoFile.getFilename());
		// Check second video is returned
		videoFile = videoList.get(1);
		assertEquals("20120102b7", videoFile.getID());
		assertEquals("Avengers", videoFile.getTitle());
		assertEquals("avengers-featurehp.mp4", videoFile.getFilename());
		// Check third video is returned
		videoFile = videoList.get(2);
		assertEquals("20120102b7", videoFile.getID());
		assertEquals("Prometheus", videoFile.getTitle());
		assertEquals("prometheus-featureukFhp.mp4", videoFile.getFilename());}
}
