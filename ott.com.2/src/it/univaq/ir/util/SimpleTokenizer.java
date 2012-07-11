package it.univaq.ir.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTokenizer {
	public static String pattern = "(https?|ftp|file)://\\S+"+ 	//urls 
								"|#[a-zA-Z0-9]+"+					//hashtags
								"|@[a-zA-Z0-9]+"+ 					//mentions
								"|[a-zA-Z]+"+						//strings
								"|[0-9]+";							//numbers
								
	
	public static Collection<String> tokenize(String text) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		Collection<String> tokens = new ArrayList<String>();
		while (m.find()) {
			tokens.add(m.group());
		}
		
		if (tokens.isEmpty()) tokens.add(text);
		
		return tokens;
	}
}
