/**
 * 
 */
package structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * @author sumit
 *
 */

public class DocQueryTermScores {

	private int totalNumberOfDocs;
	private Map<Integer, DocDetail> docLengths;
	private Map<Integer, Float> docQueryTermsScore;
	private Map<String, Integer> docsQueryTermFreq;

	public DocQueryTermScores() {
		this.docLengths = new HashMap<>();
		this.docQueryTermsScore = new HashMap<>();
		this.docsQueryTermFreq = new HashMap<>();
	}

	public void updateScore(int docId, int countTerm, String query) {
		if (!docQueryTermsScore.containsKey(docId)) {
			docQueryTermsScore.put(docId, 0f);
		}
		float w_tf = (1f * countTerm) / docLengths.get(docId).getDocLength();// weighted
		// term
		// frequency
		float prevScore = this.docQueryTermsScore.get(docId);
		float idf = (float) Math.log(1 + (1.0*this.totalNumberOfDocs/ this.docsQueryTermFreq.get(query))); // idf
		this.docQueryTermsScore.put(docId, prevScore + w_tf * idf);
	}

	public void addDocumentNormLength(int docId, float length, String pDocName) {
		this.docLengths.put(docId, new DocDetail(docId, length, pDocName));
	}

	public int getTotalNumberOfDocs() {
		return totalNumberOfDocs;
	}

	public void setTotalNumberOfDocs(int totalNumberOfDocs) {
		this.totalNumberOfDocs = totalNumberOfDocs;
	}

	public void addDocQueryFrequency(String pQueryTerm, int pCount) {
		this.docsQueryTermFreq.put(pQueryTerm, pCount);
	}

	public void clearQueryFrequency() {
		this.docsQueryTermFreq.clear();
		this.docQueryTermsScore.clear();
		// clear all scores of docs
		for (DocDetail docDetail : this.docLengths.values()) {
			docDetail.setScores(0);
		}
	}

	public int getDocumentsProcessedCounts() {
		return this.docLengths.size();
	}

	public Set<String> getAllQueryTerms() {
		return this.docsQueryTermFreq.keySet();
	}

	public boolean isValidDocID(int docId) {
		return this.docLengths.containsKey(docId);
	}

	public boolean isDocContainsQuery(int docId) {
		return this.docQueryTermsScore.containsKey(docId);
	}

	public DocDetail getDocDetails(int docId) {
		return this.docLengths.get(docId);
	}

	public double getScoreOfDoc(int docId) {
		return this.docQueryTermsScore.get(docId);
	}

	public List<DocDetail> getTopKDocuments(int topK) {
		if (topK <= 0) {
			throw new IllegalArgumentException(
					"Top K documents should be posetive.");
		}
		// use a priority queue to update the rank
		// it is used as a min heap
		Queue<Integer> priorityQueue = new PriorityQueue<Integer>(
				new TopKDocScoreComparator(this));

		for (Map.Entry<Integer, Float> docIdScore : this.docQueryTermsScore
				.entrySet()) {
			if (priorityQueue.size() < topK) {
				priorityQueue.add(docIdScore.getKey());
			} else {
				int docId = priorityQueue.peek();
				float scoreMin = this.docQueryTermsScore.get(docId);
				if (scoreMin < docIdScore.getValue()) {
					priorityQueue.poll();
					priorityQueue.add(docIdScore.getKey());
				}
			}
		}

		// after having all top kDocs, set the scores and return the docs

		LinkedList<DocDetail> topKdocDetails = new LinkedList<>();
		int length = priorityQueue.size();
		for (int i = 0; i < length; i++) {
			DocDetail docDetail = this.docLengths.get(priorityQueue.poll());
			docDetail.setScores(this.docQueryTermsScore.get(docDetail
					.getDocID()));
			topKdocDetails.push(docDetail);
		}

		return topKdocDetails;
	}

	public List<DocDetail> getRawSummary() {

		List<DocDetail> docDetails = new ArrayList<>();
		for (Map.Entry<Integer, Float> docIdScorePair : this.docQueryTermsScore
				.entrySet()) {
			DocDetail docDetailTemp = this.docLengths.get(docIdScorePair
					.getKey());
			docDetailTemp.setScores(docIdScorePair.getValue());
			docDetails.add(docDetailTemp);
		}
		return docDetails;
	}

	public int getNumOfDocMatchQuery() {
		return this.docQueryTermsScore.size();
	}
}
