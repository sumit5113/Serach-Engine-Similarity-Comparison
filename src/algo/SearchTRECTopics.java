/**
 * 
 */
package algo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;

import structure.DocDetail;
import util.Constants;
import util.DocumentParser;
import util.FileUtils;
import util.SimpleSearcher;

/**
 * @author sumit
 *
 */
public class SearchTRECTopics {

	public static void main(String[] args) throws IOException, ParseException {

		// input parameters
		String queryFileName = "L:/information retrieval/assignments/assignment 2/topics.51-100";
		String indexURL = "L:/information retrieval/assignments/assignment 2/index";
		String shortQueryOutputURL = "L:/information retrieval/assignments/assignment 2/ShortQuery.txt";
		String longQueryOutputURL = "L:/information retrieval/assignments/assignment 2/LongQuery.txt";

		String[][] stats = new String[][] {
				{ Constants.TITLE_TAG_NAME, shortQueryOutputURL },
				{ Constants.DESCRIPTION_TAG_NAME, longQueryOutputURL } };

		printStatsQuery(stats, indexURL, queryFileName);

	}

	private static void printStatsQuery(String[][] stats, String indexURL,
			String queryFileName) throws IOException, ParseException {
		// start of program executions
		String fileData = FileUtils.getText(queryFileName,
				StandardCharsets.US_ASCII);
		List<String> allTopTags = DocumentParser.extartactAllTags(fileData,
				Constants.TOP_TAG_NAME);
		// for each top tag get the topic, perform search query and print the
		// result
		SimpleSearcher srch = new SimpleSearcher(new StandardAnalyzer(),
				indexURL);

		for (String[] input : stats) {
			String toSerachTag = input[0];
			String outputFilePath = input[1];
			printQueryStats(allTopTags, srch, outputFilePath, toSerachTag);
		}
	}

	/**
	 * @param allTopTags
	 * @param srch
	 * @param shortQueryOutputURL
	 * @throws IOException
	 * @throws ParseException
	 */
	private static void printQueryStats(List<String> allTopTags,
			SimpleSearcher srch, String queryOutputURL, String openStartTagName)
			throws IOException, ParseException {
		BufferedWriter bfrWriter = new BufferedWriter(new FileWriter(
				queryOutputURL));
		StringBuilder bldr = new StringBuilder();
		for (String topTag : allTopTags) {
			String topic = DocumentParser
					.extractOpenStartTagContent(topTag, openStartTagName)
					.trim().split(Constants.COLON_CHAR)[1].trim();
			String docNumber = DocumentParser
					.extractOpenStartTagContent(topTag,
							Constants.NUMBER_TAG_NAME).trim()
					.split(Constants.COLON_CHAR)[1].trim();

			List<DocDetail> docDetails = srch.search("TEXT", topic,
					Constants.TOP_K_DOCMENTS);

			printResult(docDetails, bldr, docNumber);
			System.out.println("[" + openStartTagName + "] "
					+ "Query Number : " + docNumber
					+ " . Completed : total number of documents returned: "
					+ docDetails.size());
			if (bldr.length() > 3000) {
				bfrWriter.write(bldr.toString());
				bfrWriter.flush();
				bldr = new StringBuilder();
				System.gc();
			}
		}
		if (bldr.length() > 0) {
			bfrWriter.write(bldr.toString());
		}
		bfrWriter.close();
	}

	private static void printResult(List<DocDetail> docDetails,
			StringBuilder bldr, String docNumber) {
		int rank = 0;
		for (DocDetail doc : docDetails) {
			rank++;
			bldr.append(docNumber + "\t" + "Q0" + "\t" + doc.getDocName()
					+ "\t" + rank + "\t" + doc.getScores() + "\t" + "run-id0"
					+ Constants.NEW_LINE_CHAR);
		}
	}
}
