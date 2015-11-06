package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		String text="<a>12swer\n<b>2345</b><c>nmere</a><a>12swer\n<b>2345</b><c>nmere</a><c>nmere";
		Pattern pattern = Pattern.compile("<a>");//
		Matcher m = pattern.matcher(text);
		System.out.println(m.matches());
		while(m.find())
			System.out.println(m.start()+"--"+m.group()+"---"+m.end());
	}
}
