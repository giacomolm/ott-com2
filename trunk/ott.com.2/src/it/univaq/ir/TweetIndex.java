package it.univaq.ir;

import it.univaq.ir.model.Tweet;

import java.util.Collection;
import java.util.Set;

public interface TweetIndex {
	public void insertTweet(Tweet t);
	public void deleteTweet(long id);
	public Set<String> getTweetTerms(long id);
	public Collection<Set<String>> getTweetTerms();
	public Set<String> getDictionary();
	public int getTermFrequency(long tweetId, String term);
	public int getTermFrequency(String term);
	public int getTermDocumentFrequency(String term);	
}
