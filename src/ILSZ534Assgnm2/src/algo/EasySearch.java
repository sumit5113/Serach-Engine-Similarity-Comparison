/**
 * 
 */
package algo;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;

import structure.DocDetail;
import structure.DocQueryTermScores;
import util.Constants;
import util.SimpleSearcher;

/**
 * @author sumit
 *
 */
public class EasySearch {

	public static void main(String[] args) throws IOException, ParseException {
		String indexURL = "L:/information retrieval/assignments/assignment 2/index";
		String queryTerms = "New York";
		performScoreComputation(indexURL, queryTerms, new StandardAnalyzer());
	}

	private static void performScoreComputation(String indexURL,
			String query, Analyzer analyzer) throws IOException,
			ParseException {
		SimpleSearcher srch = new SimpleSearcher(analyzer, indexURL);
		DocQueryTermScores docQueryTerms = srch.search(Constants.TEXT_FIELD, query);
		System.out.println("Number of documents conatins the query term : "+docQueryTerms.getNumOfDocMatchQuery());
		List<DocDetail> docDetails=docQueryTerms.getRawSummary();
		
		for(DocDetail docDetail:docDetails){
			System.out.println(docDetail.toString());
		}
	}

}
