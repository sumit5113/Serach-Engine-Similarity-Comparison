/**
 * 
 */
package test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * @author sumit
 *
 */
public class InitialDraft {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {

		IndexReader reader = DirectoryReader
				.open(FSDirectory.open(Paths
						.get("L:/information retrieval/assignments/assignment 2/index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		/**
		 * Get query terms from the query string
		 */
		String queryString = "New York";
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		System.out.println("Terms in the query: ");
		for (Term t : queryTerms) {
			System.out.println(t.text());
		}
		System.out.println();

		/**
		 * Get document frequency
		 */
		int df = reader.docFreq(new Term("TEXT", "police"));
		System.out
				.println("Number of documents containing the term \"police\" for field \"TEXT\": "
						+ df);
		System.out.println();

		/**
		 * Get document length and term frequency
		 */
		// Use DefaultSimilarity.decodeNormValue(…) to decode normalized
		// document length
		DefaultSimilarity dSimi = new DefaultSimilarity();
		// Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader()
				.leaves();
		long totalNumOfDocs = 0;
		long freqTotal = 0;
		// Processing each segment
		for (int i = 0; i < leafContexts.size(); i++) {
			// Get document length
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			totalNumOfDocs += numberOfDoc;
			for (int docId = 0; docId < numberOfDoc; docId++) {
				// Get normalized length (1/sqrt(numOfTokens)) of the document
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
						.getNormValues("TEXT").get(docId));
				// Get length of the document
				float docLeng = 1 / (normDocLeng * normDocLeng);
				System.out.println("Length of doc(" + (docId + startDocNo)
						+ ", " + searcher.doc(docId + startDocNo).get("DOCNO")
						+ ") is " + docLeng);
			}
			System.out.println();
			// Get frequency of the term "police" from its postings
			PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
					"TEXT", new BytesRef("police"));
			int doc;
			if (de != null) {
				while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
					System.out.println("\"police\" occurs " + de.freq()
							+ " time(s) in doc(" + (de.docID() + startDocNo)
							+ ")");
					freqTotal += 1;
				}
			}
		}

		System.out.println(reader.numDocs() + ":"
				+ reader.getDocCount("TEXT") + ":" + reader.getRefCount()
				+ ":" + totalNumOfDocs + ":"+df+":"+freqTotal+":"+reader.maxDoc());
		//System.out.println(searcher.doc(30747).getFields());
		
	}
}
