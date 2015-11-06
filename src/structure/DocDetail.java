/**
 * 
 */
package structure;

/**
 * @author sumit
 *
 */
public class DocDetail {
	private final int docID;
	private final String docName;
	private float docLength = 0;
	// this variable is added for the ease of operations; however it should be
	// present somewhere else
	private float scores;

	public DocDetail(int pID, float length, String pDocName) {
		this.docID = pID;
		this.docLength = length;
		this.docName = pDocName;
	}

	@Override
	public boolean equals(Object p) {
		if (p instanceof DocDetail) {
			return ((DocDetail) p).docID == this.docID;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.docID;
	}

	public float getDocLength() {
		return docLength;
	}

	public void setDocLength(float docLength) {
		this.docLength = docLength;
	}

	public int getDocID() {
		return docID;
	}

	public String getDocName() {
		return docName;
	}

	public double getScores() {
		return scores;
	}

	public void setScores(float scores) {
		this.scores = scores;
	}

	public String toString() {
		return "\n Document ID : " + this.docID + ", Document Name : "
				+ this.docName + ", Document Score : " + this.scores;
	}
}
