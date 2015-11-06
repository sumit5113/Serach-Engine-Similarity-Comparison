/**
 * 
 */
package util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sumit
 *
 */

public class DocumentParser {

	static final String TAG_START = "<";
	static final String TAG_END = ">";
	static final String FRWRD_SLASH = "/";
	static final String BACK_SLASH = "\\";
	static final String EMPTY_CHAR = "";

	public static List<String> extratctTagContent(String text, String tagName) {
		return getContentsOfTagFromText(text, tagName, false);
	}

	public static List<String> extartactAllTags(String text, String tagName) {
		return getContentsOfTagFromText(text, tagName, true);
	}

	public static String extractOpenStartTagContent(String text, String tagName) {

		String tagStart = TAG_START + tagName + TAG_END;
		int startIndex = text.indexOf(tagStart);
		if (startIndex == -1) {
			return null;
		}
		String anyEndTagRegExp = TAG_START + "[\\w\\d\\s]+" + TAG_END;
		Pattern pattern = Pattern.compile(anyEndTagRegExp);
		Matcher m = pattern.matcher(text.substring(startIndex
				+ tagStart.length()));

		int indexOfNextTag = m.find() ? startIndex + tagStart.length()
				+ m.start() : text.length();
		return text.substring(startIndex + tagStart.length(), indexOfNextTag);

	}

	private static List<String> getContentsOfTagFromText(String text,
			String tagName, boolean withTagRequire) {
		String regExp = getRegExp(tagName);
		Pattern pattern = Pattern.compile(regExp);
		Matcher m = pattern.matcher(text);
		List<String> st = new LinkedList<String>();
		String tagContent = null;
		String tagStart = TAG_START + tagName + TAG_END;
		String tagEnd = TAG_START + FRWRD_SLASH + tagName + TAG_END;
		while (m.find()) {
			tagContent = m.group();
			if (!withTagRequire) {
				tagContent = tagContent.replace(tagStart, EMPTY_CHAR).replace(
						tagEnd, EMPTY_CHAR);
			}
			st.add(tagContent);
			// System.out.println(tagContent);
		}

		return st;
	}

	private static String getRegExp(String tagName) {
		String tagStart = TAG_START + tagName + TAG_END;
		String tagEnd = TAG_START + FRWRD_SLASH + tagName + TAG_END;
		return tagStart + "[\\w\\s\\W\\d\\D]*?" + tagEnd + "[\\s]?";
	}
}
