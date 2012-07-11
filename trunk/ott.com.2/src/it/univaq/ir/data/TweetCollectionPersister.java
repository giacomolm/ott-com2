package it.univaq.ir.data;

import it.univaq.ir.model.TweetCollection;

public interface TweetCollectionPersister {
	public void saveTweetCollection(String path, TweetCollection tc) throws TweetException;
	public TweetCollection loadTweetCollection(String path) throws TweetException;
	public TweetCollection downloadTweetCollection(String query, int pages, int tweetPerPages) throws TweetException;
}
