package nasirov.yv.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.parser.WrappedObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;

/**
 * Created by nasirov.yv
 */

@RunWith(SpringRunner.class)
public class RoutinesIOTest {

	@Value("classpath:routinesIOtestFile.json")
	private Resource routinesIOtestResource;

	@Test
	public void marshalToFile() {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		List<AnimediaMALTitleReferences> collectionToMarshal = new ArrayList<>();
		RoutinesIO.marshalToFile(fileName, collectionToMarshal);
		assertTrue(testFile.exists());
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void unmarshalFromFileParameterStringFilePath() throws Exception {
		String fileName = routinesIOtestResource.getFilename();
		File testFile = createTestFileFromResourcesTestFile(fileName);
		unmarshalFromDifferentSources(fileName, null, null);
		FileSystemUtils.deleteRecursively(testFile);
	}
	@Test
	public void unmarshalFromFileParameterFile() throws Exception {
		File testFile = createTestFileFromResourcesTestFile(routinesIOtestResource.getFilename());
		unmarshalFromDifferentSources(null, testFile, null);
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void unmarshalFromResource() {
		unmarshalFromDifferentSources(null, null, routinesIOtestResource);
	}
	@Test
	public void writeToFileAppendTrue() {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		String firstString = "first string";
		String secondString = "second string";
		RoutinesIO.writeToFile(fileName, firstString, true);
		RoutinesIO.writeToFile(fileName, secondString, true);
		assertTrue(testFile.exists());
		String readFromFileStringPath = RoutinesIO.readFromFile(fileName);
		String readFromTestFile = RoutinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		String finalString = firstString + System.lineSeparator() + secondString;
		assertEquals(finalString, readFromTestFile);
		assertEquals(finalString, readFromFileStringPath);
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void writeToFileAppendFalse() {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		String firstString = "first string";
		String secondString = "second string";
		RoutinesIO.writeToFile(fileName, firstString, false);
		RoutinesIO.writeToFile(fileName, secondString, false);
		assertTrue(testFile.exists());
		String readFromFileStringPath = RoutinesIO.readFromFile(fileName);
		String readFromTestFile = RoutinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		assertEquals(secondString, readFromTestFile);
		assertEquals(secondString, readFromFileStringPath);
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void writeToFileException() {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertTrue(testDir.mkdir());
		assertFalse(testDir.isFile());
		String firstString = "first string";
		RoutinesIO.writeToFile(dirName, firstString, true);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void readFromFile() {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		String firstString = "first string";
		RoutinesIO.writeToFile(fileName, firstString, true);
		String readFromFileStringPath = RoutinesIO.readFromFile(fileName);
		String readFromTestFile = RoutinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		assertEquals(firstString, readFromTestFile);
		assertEquals(firstString, readFromFileStringPath);
		FileSystemUtils.deleteRecursively(testFile);
		assertFalse(testFile.exists());
	}

	@Test
	public void readFromFileException() {
		String fileName = "test123";
		File testDir = new File(fileName);
		assertTrue(testDir.mkdir());
		assertFalse(testDir.isFile());
		RoutinesIO.readFromFile(fileName);
		RoutinesIO.readFromFile(testDir);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void readFromResource() {
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = WrappedObjectMapper
				.unmarshal(RoutinesIO.readFromResource(routinesIOtestResource), AnimediaMALTitleReferences.class, LinkedHashSet.class);
		checkTestContent(unmarshalledFromFile);
	}

	@Test
	public void readFromResourceException() {
		ClassPathResource resourcesNotFound = new ClassPathResource("resourcesNotFound");
		assertFalse(resourcesNotFound.exists());
		RoutinesIO.unmarshalFromResource(resourcesNotFound, AnimediaMALTitleReferences.class, LinkedHashSet.class);
	}


	@Test
	public void mkDirException() throws Exception {
		String dirName = "test123.txt";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		assertTrue(testDir.createNewFile());
		RoutinesIO.mkDir(dirName);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void mkDir() {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		RoutinesIO.mkDir(dirName);
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void isDirectoryExists() throws Exception {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		assertTrue(testDir.mkdir());
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		RoutinesIO.isDirectoryExists(dirName);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}
	@Test
	public void removeDir() {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		assertTrue(testDir.mkdir());
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		RoutinesIO.removeDir(dirName);
		assertFalse(testDir.exists());
	}

	private File createTestFileFromResourcesTestFile(String fileName) throws IOException {
		File testFile = new File(fileName);
		FileSystemUtils.copyRecursively(routinesIOtestResource.getFile(), testFile);
		return testFile;
	}

	private void unmarshalFromDifferentSources(String fileName, File testFile, Resource testResource) {
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = null;
		if (fileName != null) {
			unmarshalledFromFile = RoutinesIO.unmarshalFromFile(fileName, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		} else if (testFile != null) {
			unmarshalledFromFile = RoutinesIO.unmarshalFromFile(testFile, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		} else if (testResource != null) {
			unmarshalledFromFile = RoutinesIO.unmarshalFromResource(testResource, AnimediaMALTitleReferences.class, LinkedHashSet.class);
			testResource.exists();
		}
		assertNotNull(unmarshalledFromFile);
		assertFalse(unmarshalledFromFile.isEmpty());
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("1")
				.titleOnMAL("one punch man specials").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("6").currentMax("6").build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("7")
				.titleOnMAL("one punch man: road to hero").minConcretizedEpisodeOnAnimedia("7").maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("1").currentMax("7").build();
		assertEquals(2, unmarshalledFromFile.size());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7)).count());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7_2)).count());
	}

	@Test
	public void testForbiddenPrivateConstructor() throws ReflectiveOperationException {
		Constructor<?>[] declaredConstructors = RoutinesIO.class.getDeclaredConstructors();
		assertEquals(1, declaredConstructors.length);
		assertFalse(declaredConstructors[0].isAccessible());
		declaredConstructors[0].setAccessible(true);
		try {
			declaredConstructors[0].newInstance();
		} catch (InvocationTargetException e) {
			assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
		}
	}

	@Test
	public void testMarshalToFileInTheFolderOk() {
		String tempFolderName = "temp";
		List<AnimediaMALTitleReferences> testContent = getTestContent();
		String testFilename = routinesIOtestResource.getFilename();
		RoutinesIO.marshalToFileInTheFolder(tempFolderName, testFilename, testContent);
		File testDir = new File(tempFolderName);
		File testFileInTestDir = new File(testDir, testFilename);
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = WrappedObjectMapper
				.unmarshal(RoutinesIO.readFromFile(testFileInTestDir), AnimediaMALTitleReferences.class, LinkedHashSet.class);
		checkTestContent(unmarshalledFromFile);
		FileSystemUtils.deleteRecursively(testDir);
	}

	@Test
	public void testMarshalToFileInTheFolderInvalidDir() throws IOException {
		String fileName = "test123.txt";
		File fileNotDir = new File(fileName);
		assertTrue(fileNotDir.createNewFile());
		List<AnimediaMALTitleReferences> testContent = getTestContent();
		String testFilename = routinesIOtestResource.getFilename();
		RoutinesIO.marshalToFileInTheFolder(fileName, testFilename, testContent);
		assertFalse(fileNotDir.isDirectory());
		FileSystemUtils.deleteRecursively(fileNotDir);
	}

	private void checkTestContent(Set<AnimediaMALTitleReferences> unmarshalledFromFile) {
		List<AnimediaMALTitleReferences> contentFromTestFile = getTestContent();
		AnimediaMALTitleReferences onePunch7 = contentFromTestFile.stream().filter(title -> title.getTitleOnMAL().equals("one punch man specials"))
				.findAny().get();
		AnimediaMALTitleReferences onePunch7_2 = contentFromTestFile.stream()
				.filter(title -> title.getTitleOnMAL().equals("one punch man: road to " + "hero")).findAny().get();
		assertNotNull(unmarshalledFromFile);
		assertFalse(unmarshalledFromFile.isEmpty());
		assertEquals(2, unmarshalledFromFile.size());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7)).count());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7_2)).count());
	}

	private List<AnimediaMALTitleReferences> getTestContent() {
		ArrayList<AnimediaMALTitleReferences> result = new ArrayList<>();
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("1")
				.titleOnMAL("one punch man specials").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("6").currentMax("6").build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("7")
				.titleOnMAL("one punch man: road to hero").minConcretizedEpisodeOnAnimedia("7").maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("1").currentMax("7").build();
		result.add(onePunch7);
		result.add(onePunch7_2);
		return result;
	}

}