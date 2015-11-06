/**
 * 
 */
package util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
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

import structure.DocDetail;
import structure.DocQueryTermScores;

/**
 * @author sumit
 *
 */
public class SimpleSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	String indexFileUrl;

	// optmizations factor's for norm length computations
	private final static Map<String, DocQueryTermScores> LOCAL_CACHE = Collections
			.synchronizedMap(new HashMap<String, DocQueryTermScores>());

	public SimpleSearcher(Analyzer pAnalyzer, String pIndexUrl) {
		this.analyzer = pAnalyzer;
		this.indexFileUrl = pIndexUrl;
		LOCAL_CACHE.put(this.indexFileUrl, new DocQueryTermScores());
	}

	public DocQueryTermScores search(String field, String query)
			throws IOException, ParseException {
		DocQueryTermScores docQueryTermScores = null;
		try {
			initReader();
			this.clearQueryCahce();
			docQueryTermScores = LOCAL_CACHE.get(this.indexFileUrl);
			docQueryTermScores.setTotalNumberOfDocs(reader.numDocs());
			populateQueryTermsResult(field, query, docQueryTermScores);
			populateDocLengthAndFrequeny(field, docQueryTermScores);
		} finally {
			releaseReader();
		}
		return docQueryTermScores;
	}

	public List<DocDetail> search(String field, String query, int topK)
			throws IOException, ParseException {
		DocQueryTermScores docQueryTermScores = this.search(field, query);
		return docQueryTermScores.getTopKDocuments(topK);
	}

	private void populateDocLengthAndFrequeny(String field,
			DocQueryTermScores docQueryTermScores) throws IOException {

		// Use DefaultSimilarity.decodeNormValue(…) to decode normalized
		// document length
		DefaultSimilarity dSimi = new DefaultSimilarity();
		// Get the segments of the index
		List<LeafReaderContext> leafContexts = this.reader.getContext()
				.reader().leaves();

		// Processing each segment
		for (int i = 0; i < leafContexts.size(); i++) {
			// Get document length
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			// optimization required, update only when index is changing
			if (docQueryTermScores.getDocumentsProcessedCounts() < docQueryTermScores
					.getTotalNumberOfDocs()) {
				updateNormDocLength(field, docQueryTermScores, dSimi,
						leafContext, startDocNo, numberOfDoc);
			}
			// update the score for all query terms
			updateScore(field, docQueryTermScores, leafContext, startDocNo);
			/*System.out.println("Done processing From ::" + startDocNo
					+ " --TO-- " + (startDocNo + numberOfDoc));*/
			System.gc();
		}
	}

	/**
	 * @param field
	 * @param docQueryTermScores
	 * @param dSimi
	 * @param leafContext
	 * @param startDocNo
	 * @param numberOfDoc
	 * @throws IOException
	 */
	private void updateNormDocLength(String field,
			DocQueryTermScores docQueryTermScores, DefaultSimilarity dSimi,
			LeafReaderContext leafContext, int startDocNo, int numberOfDoc)
			throws IOException {
		for (int docId = 0; docId < numberOfDoc; docId++) {
			// Get normalized length (1/sqrt(numOfTokens)) of the
			// document
			float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
					.getNormValues(field).get(docId));
			// Get length of the document
			float docLeng = 1f / (normDocLeng * normDocLeng);
			docQueryTermScores.addDocumentNormLength((docId + startDocNo),
					docLeng, searcher.doc(docId + startDocNo).get("DOCNO"));
		}
	}

	private static void updateScore(String field,
			DocQueryTermScores docQueryTermScores,
			LeafReaderContext leafContext, int startDocNo) throws IOException {
		// Get frequency of the term "police" from its postings
		for (String query : docQueryTermScores.getAllQueryTerms()) {
			PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
					field, new BytesRef(query));
			if (de != null) {
				while ((de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
					docQueryTermScores.updateScore(de.docID() + startDocNo,
							de.freq(), query);
				}
			}
		}
	}

	private void populateQueryTermsResult(String field, String queryString,
			DocQueryTermScores docQueryTermScores) throws ParseException,
			IOException {
		// field = "TEXT"
		QueryParser parser = new QueryParser(field, this.analyzer);
		Query query = parser.parse(QueryParser.escape(queryString));
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		this.searcher.createNormalizedWeight(query, false).extractTerms(
				queryTerms);
		for (Term t : queryTerms) {
			// Number of documents containing the term
			int df = reader.docFreq(t);
			docQueryTermScores.addDocQueryFrequency(t.text(), df);
		}
	}

	private void releaseReader() throws IOException {
		this.reader.close();
	}

	private void initReader() throws IOException {
		this.reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(this.indexFileUrl)));
		this.searcher = new IndexSearcher(reader);
		
	}

	public void clearQueryCahce() {
		LOCAL_CACHE.get(this.indexFileUrl).clearQueryFrequency();
	}
}
