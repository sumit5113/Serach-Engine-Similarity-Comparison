/**
 * 
 */
package util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;

/**
 * @author sumit
 *
 */
public class Constants {

	public static final String NEW_LINE_CHAR = System
			.getProperty("line.separator");
	// related to corpus
	public static final String TEXT_FIELD = "TEXT";
	public static final String TOP_TAG_NAME = "top";
	public static final String TITLE_TAG_NAME = "title";
	public static final String DESCRIPTION_TAG_NAME = "desc";
	public static final String NUMBER_TAG_NAME = "num";
	public static final String COLON_CHAR = ":";

	// configurable constants
	public static final int TOP_K_DOCMENTS = 1000;
	public static final Charset DEFAULT_CHAR_SET = StandardCharsets.UTF_8;
	public final static String LONG_OUTPUT_SUFFIX_FILE = "LongQuery.txt";
	public final static String SHORT_OUTPUT_SUFFIX_FILE = "ShortQuery.txt";
	public static final float LAMDA = 0.7f;

	public enum SimilarityEnum {
		BM25, VSM, LMD, LMJ;
	}
	
	

}
