/**
 * 
 */
package algo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import util.Constants;
import util.Constants.SimilarityEnum;
import util.DocumentParser;
import util.FileUtils;

/**
 * @author sumit
 *
 */
public class CompareAlgorithms {

	private final static String fileDirLoc = "L:/information retrieval/assignments/assignment 2/";

	/**
	 * @param args
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, ParseException {

		// Input to the programs
		String indexUrl = "L:/information retrieval/assignments/assignment 2/index";
		String queryUrl = "L:/information retrieval/assignments/assignment 2/topics.51-100";
		String[] queryTags = new String[] { Constants.TITLE_TAG_NAME,
				Constants.DESCRIPTION_TAG_NAME };
		Analyzer analyzer = new StandardAnalyzer();
		compareSiilarityScorer(indexUrl, queryUrl, queryTags, analyzer);

	}

	private static void compareSiilarityScorer(String indexUrl,
			String queryUrl, String[] queryTags, Analyzer analyzer)
			throws IOException, ParseException {
		String fileData = FileUtils
				.getText(queryUrl, StandardCharsets.US_ASCII);
		List<String> allTopTags = DocumentParser.extartactAllTags(fileData,
				Constants.TOP_TAG_NAME);

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexUrl)));
		IndexSearcher searcher = new IndexSearcher(reader);

		for (SimilarityEnum compSimilarity : SimilarityEnum.values()) {
			Similarity smlrty = getSimilarity(compSimilarity);
			searcher.setSimilarity(smlrty);
			printComparisonStats(analyzer, searcher, allTopTags, queryTags,
					compSimilarity);
		}

	}

	private static void printComparisonStats(Analyzer analyzer,
			IndexSearcher searcher, List<String> allTopTags,
			String[] queryTags, SimilarityEnum compSimilarity)
			throws ParseException, IOException {

		System.out.println("Start::" + compSimilarity.name());
		for (String tags : queryTags) {
			System.out.println("earching for Tags:: "+tags);
			StringBuilder result = new StringBuilder();
			for (String topTagContent : allTopTags) {

				String queryString = DocumentParser
						.extractOpenStartTagContent(topTagContent, tags).trim()
						.split(Constants.COLON_CHAR)[1].trim();
				String queryNumber = DocumentParser
						.extractOpenStartTagContent(topTagContent,
								Constants.NUMBER_TAG_NAME).trim()
						.split(Constants.COLON_CHAR)[1].trim();

				QueryParser parser = new QueryParser(Constants.TEXT_FIELD,
						analyzer);
				Query query = parser.parse(QueryParser.escape(queryString));

				/*
				 * System.out.println("Searching for: " +
				 * query.toString(Constants.TEXT_FIELD));
				 */
				
				TopDocs results = searcher.search(query,
						Constants.TOP_K_DOCMENTS);

				// Print number of hits
				int numTotalHits = results.totalHits;
				System.out.println(numTotalHits + " total matching documents");

				// Print retrieved results
				ScoreDoc[] hits = results.scoreDocs;
				for (int i = 0; i < hits.length; i++) {
					Document doc = searcher.doc(hits[i].doc);
					result.append(getLineResult(queryNumber, "Q1",
							doc.get("DOCNO"), i + 1, hits[i].score, "run-id1"));
				}
			}
			// write to file
			FileUtils.writeToFile(result.toString(),
					getFilePath(tags, compSimilarity));
			System.gc();

		}
		System.out.println("END::" + compSimilarity.name());
	}

	private static String getFilePath(String tags, SimilarityEnum compSimilarity) {
		String path = fileDirLoc + compSimilarity.name();
		switch (tags) {
		case Constants.TITLE_TAG_NAME:
			path = path + Constants.SHORT_OUTPUT_SUFFIX_FILE;
			break;
		case Constants.DESCRIPTION_TAG_NAME:
			path = path + Constants.LONG_OUTPUT_SUFFIX_FILE;
			break;
		}
		return path;
	}

	private static Object getLineResult(String queryNumber, String queryID,
			String docName, int rank, float score, String runID) {
		return queryNumber + "\t" + queryID + "\t" + docName + "\t" + rank
				+ "\t" + score + "\t" + runID + Constants.NEW_LINE_CHAR;
	}

	private static Similarity getSimilarity(SimilarityEnum compSimilarity) {
		Similarity smlrty = null;
		switch (compSimilarity) {
		case BM25:
			smlrty = new BM25Similarity();
			break;
		case VSM:
			smlrty = new DefaultSimilarity();
			break;
		case LMD:
			smlrty = new LMDirichletSimilarity();
			break;
		case LMJ:
			smlrty = new LMJelinekMercerSimilarity(Constants.LAMDA);
			break;
		}
		return smlrty;
	}

}
