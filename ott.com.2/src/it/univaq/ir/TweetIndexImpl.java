package it.univaq.ir;

import it.univaq.ir.model.Tweet;
import it.univaq.ir.util.SimpleTokenizer;
import it.univaq.ir.util.WrappedStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class Frequency {
	public int termFrequency = 0;
	public int documentFrequency = 0;
}

public class TweetIndexImpl implements TweetIndex {

	private Map<Long, Collection<String>> index;
	private Map<String, Frequency> frequencies;
	private Collection<String> stopwords;
	private Collection<String> excludewords;
	
	public TweetIndexImpl() throws IOException {
		this(null);
	}
	
	public TweetIndexImpl(String exclude) throws IOException {

		index = new HashMap<Long, Collection<String>>();
		frequencies = new HashMap<String, Frequency>();
		
		//load stopwords
		stopwords = new ArrayList<String>();
		InputStream in = 
				   getClass().getResourceAsStream("/it/univaq/ir/util/en-stopword-list.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) stopwords.add(line);
		
		excludewords = new ArrayList<String>();
		if (exclude != null && !exclude.isEmpty()) {			
			Collection<String> tokens = SimpleTokenizer.tokenize(exclude);
			for (String token : tokens) {
				token = token.toLowerCase(); //lower case (to match against stopword)
				String term = WrappedStemmer.stem(token);
				excludewords.add(term);
			}			
		}
	}
	
	@Override
	public void insertTweet(Tweet t) {
		long id = t.getId();
		Collection<String> tweetTerms = new ArrayList<String>();
		index.put(id, tweetTerms);
		
		//ANALYZE
		String text = t.getText();		
		Collection<String> tokens = SimpleTokenizer.tokenize(text);
		for (String token : tokens) {
			token = token.toLowerCase(); //lower case (to match against stopword)
			if (stopwords.contains(token))
				System.err.println("Stopword detected: "+token);
			else if (excludewords.contains(token))
				System.err.println("Excluded word detected: "+token);			
			else if (token.startsWith("#")) //it's not an hashtag
				System.err.println("Hashtag detected: "+token);
			else if (token.startsWith("@")) //it's not a mention				
				System.err.println("Mention detected: "+token);
			else if (token.matches("https?://\\S+")) //it's not an url (TODO: improve this!) 
				System.err.println("Url detected: "+token);			
			else { 
				String term = WrappedStemmer.stem(token);
				if (tweetTerms.contains(term)) {
					frequencies.get(term).termFrequency++;
				} else {
					if (!frequencies.containsKey(term)) {
						Frequency freq = new Frequency();
						frequencies.put(term, freq);
					}
					frequencies.get(term).documentFrequency++;
					frequencies.get(term).termFrequency++;
				}
				tweetTerms.add(term);
			}
		}
		
		
	}

	@Override
	public void deleteTweet(long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getTweetTerms(long id) {
		return new HashSet<String>(index.get(id));
	}

	@Override
	public Set<String> getDictionary() {
		return new HashSet<String>(frequencies.keySet());
	}

	@Override
	public int getTermFrequency(long tweetId, String term) {
		int cnt = 0;
		for (String t : index.get(tweetId)) if (t.equalsIgnoreCase(term)) cnt++;
		return cnt;
	}

	@Override
	public int getTermFrequency(String term) {
		return frequencies.get(term).termFrequency;
	}

	@Override
	public int getTermDocumentFrequency(String term) {
		return frequencies.get(term).documentFrequency;
	}

	@Override
	public Collection<Set<String>> getTweetTerms() {
		Collection<Set<String>> terms = new ArrayList<Set<String>>();
		for (Entry<Long, Collection<String>> e : index.entrySet()) {
			terms.add(new HashSet<String>(e.getValue()));
		}
		return terms;
	}

}
