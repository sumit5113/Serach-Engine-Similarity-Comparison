/**
 * 
 */
package generateIndex.creation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * @author sumit
 *
 */

interface ITagName {
	String getTagName();
}

// Representation of tag names in the file AP89 corpuses
enum AP89TagName implements ITagName {
	// all tags for AP89
	DOC("DOC"), DOCNO("DOCNO"), FILEID("FILEID"), FIRST("FIRST"), SECOND(
			"SECOND"), HEAD("HEAD"), BYLINE("BYLINE"), DATELINE("DATELINE"), TEXT(
			"TEXT");

	String filedName;

	private AP89TagName(String pFiledName) {
		this.filedName = pFiledName;
	}

	@Override
	public String getTagName() {
		return this.filedName;
	}
}

class SearchInputBean {

	private String sourceURL;
	private Analyzer analyzer;
	private List<ITagName> processingTagNames;
	private ITagName rootTagName;
	private String indexPathUrl;
	private OpenMode openMode;

	public SearchInputBean() {

	}

	public String getSourceURL() {
		return sourceURL;
	}

	public void setSourceURL(String sourceURL) {
		this.sourceURL = sourceURL;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public List<ITagName> getProcessingTagNames() {
		return processingTagNames;
	}

	public void setProcessingTagNames(List<ITagName> processingTagNames) {
		this.processingTagNames = processingTagNames;
	}

	public ITagName getRootTagName() {
		return rootTagName;
	}

	public void setRootTagName(ITagName rootTagName) {
		this.rootTagName = rootTagName;
	}

	public String getIndexPathUrl() {
		return indexPathUrl;
	}

	public void setIndexPathUrl(String indexPathUrl) {
		this.indexPathUrl = indexPathUrl;
	}

	/**
	 * @return the openMode
	 */
	public OpenMode getOpenMode() {
		return openMode;
	}

	/**
	 * @param openMode
	 *            the openMode to set
	 */
	public void setOpenMode(OpenMode openMode) {
		this.openMode = openMode;
	}
}

class DocumentParserUtil {

	public static List<String> extractTagContent(ITagName tagName, String text) {
		return extractTagContent(tagName, text, false);
	}

	public static List<String> extractTag(ITagName tagName, String text) {
		return extractTagContent(tagName, text, true);
	}

	private static List<String> extractTagContent(ITagName tagName,
			String text, boolean withTagRequire) {
		if (tagName == null || (text == null || text.trim().length() == 0)) {
			return Collections.emptyList();
		}

		List<String> allTagContent = getContentsOfTagFromText(text, tagName,
				withTagRequire);// text.split(regExp);

		return allTagContent;
	}

	private static List<String> getContentsOfTagFromText(String text,
			ITagName tagName, boolean withTagRequire) {
		String regExp = getRegExpTagContent(tagName);
		Pattern pattern = Pattern.compile(regExp);
		Matcher m = pattern.matcher(text);
		List<String> st = new LinkedList<String>();
		String tagContent = null;
		String[] tagPairs = getStartAndEndTag(tagName);
		while (m.find()) {
			tagContent = m.group();
			if (!withTagRequire) {
				tagContent = tagContent.replace(tagPairs[0],
						Constants.EMPTY_CHAR).replace(tagPairs[1],
						Constants.EMPTY_CHAR);
			}
			st.add(tagContent);
			// System.out.println(tagContent);
		}

		return st;
	}

	private static String getRegExpTagContent(ITagName tagName) {
		String[] tagPairs = getStartAndEndTag(tagName);
		String regExp = tagPairs[0] + "[\\w\\s\\W\\d\\D]*?" + tagPairs[1]
				+ "[\\s]?";
		return regExp;
	}

	/**
	 * @param tagName
	 * @return
	 */
	private static String[] getStartAndEndTag(ITagName tagName) {
		String[] tagPairs = new String[2];
		tagPairs[0] = Constants.TAG_START + tagName.getTagName()
				+ Constants.TAG_END;
		tagPairs[1] = Constants.TAG_START + Constants.FORWARD_CHAR
				+ tagName.getTagName() + Constants.TAG_END;
		return tagPairs;
	}
}

class Constants {
	public static final String TAG_START = "<";
	public static final String TAG_END = ">";
	public static final String SPACE = " ";
	public static final String FORWARD_CHAR = "/";
	public static final String EMPTY_CHAR = "";
}

interface IIndexProcessor<E, R> {
	R startIndexingProcess(E e) throws Exception;
}

// Implementation class for this assignments
class IndexProcessorStats implements IIndexProcessor<SearchInputBean, Object> {
	IndexWriter indxWrtr = null;

	private static class TimeStat {
		Date startTime;
		Date endTime;

		void start() {
			this.startTime = new Date();
		}

		void end() {
			this.endTime = new Date();
		}

		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		public Date getEndTime() {
			return endTime;
		}

		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}
	}

	@Override
	public Object startIndexingProcess(SearchInputBean e) throws IOException {
		// 1. read one by one file in the source directory
		// 2. create the document object for each of the field and write to the
		// index writer
		// 3. generate the statistical on the indexes created
		TimeStat statTime = new IndexProcessorStats.TimeStat();
		int noOfFilesCompleted = 0;
		try {
			statTime.start();
			initWriter(e);
			for (File f : new File(e.getSourceURL()).listFiles()) {
				noOfFilesCompleted++;

				String text = getText(f);
				List<String> allRootTagsDetails = DocumentParserUtil
						.extractTag(AP89TagName.DOC, text);

				for (String rootElement : allRootTagsDetails) {
					Document luceneDoc = new Document();
					for (ITagName tagName : e.getProcessingTagNames()) {
						luceneDoc.add(getField(tagName, rootElement));
					}
					this.indxWrtr.addDocument(luceneDoc);
				}

				if (noOfFilesCompleted % 20 == 0) {
					System.out.println("No Of files completed : "
							+ noOfFilesCompleted);
					System.gc();
				}
			}
			System.out.println("No Of files completed : " + noOfFilesCompleted);
			this.indxWrtr.commit();
			statTime.end();
		} finally {
			releaseWriter();
		}
		statisticalReport(e, statTime);
		return null;
	}

	private void statisticalReport(SearchInputBean e, TimeStat statTime)
			throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get((e
				.getIndexPathUrl()))));
		System.out
				.println("Total Time taken in indexing process : \n Sart Time : "
						+ statTime.getStartTime()
						+ "\n End Time : "
						+ statTime.getEndTime());

		// Print the total number of documents in the corpus
		System.out.println("Total number of documents in the corpus:"
				+ reader.maxDoc());
		// Print the number of documents containing the term "new" in
		// <field>TEXT</field>.
		System.out
				.println("Number of documents containing the term \"new\" for	field \"TEXT\": "
						+ reader.docFreq(new Term(
								AP89TagName.TEXT.getTagName(), "new")));
		// Print the total number of occurrences of the term "new" across all
		// documents for <field>TEXT</field>.
		System.out
				.println("Number of occurrences of \"new\" in the field \"TEXT\": "
						+ reader.totalTermFreq(new Term("TEXT", "new")));
		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
		// Print the size of the vocabulary for <field>TEXT</field>, applicable
		// when the index has only one segment.
		System.out.println("Size of the vocabulary for this field:"
				+ vocabulary.size());
		// Print the total number of documents that have at least one term for
		// <field>TEXT</field>
		System.out
				.println("Number of documents that have at least one term for this field: "
						+ vocabulary.getDocCount());
		// Print the total number of tokens for <field>TEXT</field>
		System.out.println("Number of tokens for this field: "
				+ vocabulary.getSumTotalTermFreq());
		// Print the total number of postings for <field>TEXT</field>
		System.out.println("Number of postings for this field: "
				+ vocabulary.getSumDocFreq());
		// Print the vocabulary for <field>TEXT</field>
		TermsEnum iterator = vocabulary.iterator();
		BytesRef byteRef = null;
		StringBuilder vocabularyTerms = new StringBuilder();
		System.out.println("\n*******Vocabulary-Start**********");
		while ((byteRef = iterator.next()) != null) {
			String term = byteRef.utf8ToString();
			vocabularyTerms.append(term + "\t");
			// System.out.println(term + "\t");
		}
		System.out.println(vocabularyTerms);
		System.out.println("\n*******Vocabulary-End**********");
		reader.close();

	}

	private Field getField(ITagName tagName, String rootElement) {

		String contents = String.join(Constants.SPACE,
				DocumentParserUtil.extractTagContent(tagName, rootElement));
		Field field = null;
		switch ((AP89TagName) tagName) {
		case BYLINE:
		case DATELINE:
		case DOCNO:
			field = new StringField(tagName.getTagName(), contents,
					Field.Store.YES);
			break;
		case HEAD:
		case TEXT:
			field = new TextField(tagName.getTagName(), contents,
					Field.Store.YES);
			break;
		default:
			throw new UnsupportedOperationException(
					"Invalid Tag Type supported ::" + tagName.getTagName());

		}

		return field;
	}

	private void releaseWriter() throws IOException {
		if (this.indxWrtr != null && this.indxWrtr.isOpen()) {
			this.indxWrtr.close();
		}
	}

	private void initWriter(SearchInputBean e) throws IOException {
		Directory indexDir = FSDirectory.open(new File(e.getIndexPathUrl())
				.toPath());
		IndexWriterConfig indxWriterConfig = new IndexWriterConfig(
				e.getAnalyzer());
		indxWriterConfig.setOpenMode(e.getOpenMode());
		this.indxWrtr = new IndexWriter(indexDir, indxWriterConfig);
	}

	private String getText(File f) throws IOException {
		StringBuilder build = new StringBuilder();
		try (BufferedReader bfrdRead = Files.newBufferedReader(f.toPath(),
				StandardCharsets.ISO_8859_1)) {
			String text = null;
			while ((text = bfrdRead.readLine()) != null) {
				build.append(text);
			}
		}
		return build.toString();
		// return String.join("\n",
		// Files.readAllLines(Paths.get(f.toURI()),StandardCharsets.US_ASCII));
	}

}

public class GenerateIndex {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// customize the following parameters
		String corpusSourceURL = "L:/information retrieval/assignments/corpus/corpus/";
		String pathToIndexDirURL = "L:/information retrieval/assignments/corpus/index/";

		// start of the program
		SearchInputBean srchInputBean = new SearchInputBean();
		srchInputBean.setSourceURL(corpusSourceURL);
		srchInputBean.setIndexPathUrl(pathToIndexDirURL);
		srchInputBean.setAnalyzer(new StandardAnalyzer());
		srchInputBean.setOpenMode(OpenMode.CREATE);
		ITagName[] tagsIndexing = new ITagName[] { AP89TagName.BYLINE,
				AP89TagName.HEAD, AP89TagName.DOCNO, AP89TagName.DATELINE,
				AP89TagName.TEXT };
		srchInputBean.setProcessingTagNames(Arrays.asList(tagsIndexing));

		// indexing process to start
		IIndexProcessor<SearchInputBean, Object> indexer = new IndexProcessorStats();
		indexer.startIndexingProcess(srchInputBean);
	}

}
