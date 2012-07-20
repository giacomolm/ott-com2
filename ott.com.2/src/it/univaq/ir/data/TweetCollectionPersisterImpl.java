package it.univaq.ir.data;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import it.univaq.ir.model.TweetCollection;

public class TweetCollectionPersisterImpl implements TweetCollectionPersister {

	@Override
	public void saveTweetCollection(String path, TweetCollection tc)
			throws TweetException {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(TweetCollection.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			Writer w = new FileWriter(path);
			marshaller.marshal(tc, w);
			w.close();
		} catch (Exception e) {
			throw new TweetException(e);
		}
	}

	@Override
	public TweetCollection downloadTweetCollection(String queryString, String lang, 
			int pages, int tweetPerPages, boolean excludeRT)
			throws TweetException, IllegalArgumentException {
		if (pages < 1 || pages > 15)
			throw new IllegalArgumentException("It must be 1<=page<=15");
		if (tweetPerPages < 1 || tweetPerPages > 100)
			throw new IllegalArgumentException(
					"It must be 1<=tweetPerPages<=100");
		TweetCollection tweets = null;
		try {
			Twitter twitter = new TwitterFactory().getInstance();
			tweets = new TweetCollection();
			Query query = new Query(queryString);
			query.lang(lang);
			query.setRpp(tweetPerPages);
			for (int i = 1; i <= pages; i++) {
				System.err.println("Downloading page #" + i + " for "
						+ queryString + " ...");
				query.setPage(i);
				QueryResult result = twitter.search(query);
				for (Tweet t : result.getTweets()) {
					if (excludeRT && t.getText().startsWith("RT @")) {
						System.err.println("RT excluded, id: "+t.getId());
					} else {
						it.univaq.ir.model.Tweet t1 = new it.univaq.ir.model.Tweet();
						t1.setId(t.getId());
						t1.setAuthor(t.getFromUser());
						t1.setText(t.getText());
						GregorianCalendar c = new GregorianCalendar();
						c.setTime(t.getCreatedAt());
						XMLGregorianCalendar date = DatatypeFactory
								.newInstance().newXMLGregorianCalendar(c);
						t1.setDate(date);
						tweets.getTweet().add(t1);
					}
				}
			}
		} catch (Exception ex) {
			new TweetException(ex);
		}
		return tweets;
	}

	@Override
	public TweetCollection loadTweetCollection(String path)
			throws TweetException {
		TweetCollection tc = null;
		try {
			JAXBContext context = JAXBContext
					.newInstance(TweetCollection.class);
			Unmarshaller um = context.createUnmarshaller();
			Reader r = new FileReader(path);
			tc = (TweetCollection) um.unmarshal(r);
			r.close();
		} catch (Exception ex) {
			throw new TweetException(ex);
		}
		return tc;

	}

}
