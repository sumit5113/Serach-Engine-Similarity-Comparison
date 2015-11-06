/**
 * 
 */
package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author sumit
 *
 */
public class FileUtils {

	public static String[] getAllFiles(String pDirectoryPath) {
		return new File(pDirectoryPath).list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return dir.isFile();
			}
		});
	}

	public static String getText(String filePath, Charset chset)
			throws IOException {
		if (chset == null)
			chset = Constants.DEFAULT_CHAR_SET;

		/*
		 * StringBuilder build = new StringBuilder(); String newLine =
		 * System.getProperty("line.seperator"); if(chset==null) chset =
		 * StandardCharsets.UTF_8;
		 * 
		 * try (BufferedReader bfrdRead = Files.newBufferedReader(new File(
		 * filePath).toPath(), chset)) { String text = null; while ((text =
		 * bfrdRead.readLine()) != null) { build.append(text);
		 * build.append(newLine); } } return build.toString();
		 */
		return String.join(Constants.NEW_LINE_CHAR, Files.readAllLines(
				Paths.get(new File(filePath).toURI()), chset));
	}

	public static void flushToFile(FileWriter fileWriter, String text)
			throws IOException {
		fileWriter.write(text);
	}

	public static void writeToFile(String text, String fileName)
			throws IOException {
		try (BufferedWriter bfrdWriter = Files.newBufferedWriter(Paths
				.get(fileName))) {
			bfrdWriter.write(text);
		}
	}
}
