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
	VideoFile videoFile = videoList.get(0);
	assertEquals("20120213a2", videoFile.getID());
	assertNotNull("Monsters Inc.", videoFile.getTitle());
	assertNotNull("monstersinc_high.mpg", videoFile.getFilename());
	videoFile = videoList.get(1);
	assertEquals("20120102b7", videoFile.getID());
	assertNotNull("Avengers", videoFile.getTitle());
	assertNotNull("avengers-featurehp.mp4", videoFile.getFilename());
	}
}
