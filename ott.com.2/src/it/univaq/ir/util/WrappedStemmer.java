package it.univaq.ir.util;

public class WrappedStemmer {
	
	public static String stem(String string) {
		Stemmer stemmer = new Stemmer();
		char[] chars = string.toCharArray();
		for (char c : chars) stemmer.add(c);
		stemmer.stem();
		return stemmer.toString();
	}
}
