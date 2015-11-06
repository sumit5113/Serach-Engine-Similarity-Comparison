/**
 * 
 */
package structure;

import java.util.Comparator;

/**
 * @author sumit
 *
 */
public class TopKDocScoreComparator implements Comparator<Integer> {

	DocQueryTermScores docQryTrmScore = null;

	public TopKDocScoreComparator(DocQueryTermScores pDocQuryTermScores) {
		this.docQryTrmScore = pDocQuryTermScores;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Integer docId1, Integer docId2) {
		boolean isValidDoc1 = this.docQryTrmScore.isDocContainsQuery(docId1);
		boolean isValidDoc2 = this.docQryTrmScore.isDocContainsQuery(docId2);
		if (!isValidDoc1 && !isValidDoc2) {
			return 0;
		}
		if (!isValidDoc1) {
			return -1;
		}
		if (!isValidDoc2) {
			return 1;
		}
		double score1 = this.docQryTrmScore.getScoreOfDoc(docId1);
		double score2 = this.docQryTrmScore.getScoreOfDoc(docId2);

		if (score1 == score2) {
			return 0;
		}

		return score1 > score2 ? 1 : -1;
	}
}
