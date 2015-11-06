package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RunCmD {

	public static void runCmD() throws IOException {
		String[] fileNames = new String[] { "ShortQuery_1.txt", "ShortQuery.txt", "VSMShortQuery.txt",
				"BM25ShortQuery.txt", "LMDShortQuery.txt", "LMJShortQuery.txt", "LongQuery.txt", "VSMLongQuery.txt",
				"BM25LongQuery.txt", "LMDLongQuery.txt", "LMJLongQuery.txt", "LongQuery_1.txt" };
		String[] measures = new String[] { "Rprec", "recip_rank", "ndcg_cut", "map_cut" };
		for (String path : fileNames) {
			System.out.println("----------------- START ----------");
			System.out.println(path);
			for (String msr : measures) {
				Process pr = Runtime.getRuntime().exec("/Users/Aki/Downloads/trec_eval.9.0/trec_eval -m " + msr
						+ " /Users/Aki/Downloads/assignment_2/qrels.51-100 /Users/Aki/Downloads/assignment_2/" + path);
				printResult(pr.getErrorStream());
				printResult(pr.getInputStream());
			}

			System.out.println("------------------ END -----------");
		}

	}

	private static void printResult(InputStream stream) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(stream));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			System.out.println(line);
		}
	}

	public static void main(String[] args) throws IOException {
		runCmD();
	}

}
