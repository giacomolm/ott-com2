package it.univaq.ir;

import it.univaq.ir.model.Tweet;
import it.univaq.ir.model.TweetCollection;
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

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.api.StatusMethods;

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

		// load stopwords
		stopwords = new ArrayList<String>();
		InputStream in = getClass().getResourceAsStream(
				"/it/univaq/ir/util/en-stopword-list.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null)
			stopwords.add(line);

		excludewords = new ArrayList<String>();
		if (exclude != null && !exclude.isEmpty()) {
			Collection<String> tokens = SimpleTokenizer.tokenize(exclude);
			for (String token : tokens) {
				token = token.toLowerCase(); // lower case (to match against
												// stopword)
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

		// ANALYZE
		String text = t.getText();
		Collection<String> tokens = SimpleTokenizer.tokenize(text);
		for (String token : tokens) {
			token = token.toLowerCase(); // lower case (to match against
											// stopword)
			if (stopwords.contains(token))
				System.err.println("Stopword detected: " + token);
			else if (excludewords.contains(token))
				System.err.println("Excluded word detected: " + token);
			else if (token.startsWith("#")) // it's not an hashtag
				System.err.println("Hashtag detected: " + token);
			else if (token.startsWith("@")) // it's not a mention
				System.err.println("Mention detected: " + token);
			else if (token.matches("https?://\\S+")) // it's not an url (TODO:
														// improve this!)
				System.err.println("Url detected: " + token);
			else {
				String term = WrappedStemmer.stem(token);
				if (term.length() <= 2) {
					System.err.println("Term shorter than 3 chars detected: " + token);					
				} else {
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
		for (String t : index.get(tweetId))
			if (t.equalsIgnoreCase(term))
				cnt++;
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

	@Override
	public Map getMostValuableTweet(TweetCollection tc, TweetIndex ti, Set<String> solution) {
		// TODO Auto-generated method stub
		
		//max è un array che contiene le frequenze delle 5 parole più frequenti nei tweet
		int max[] = new int[5];
		
		//per sicurezza, non ricordo se vengono inizializzati a zero
		for(int i=0; i<max.length; i++){
			max[i]=0;
		}
		//La mappa contiene i tweet che hanno costo più elevato
		//La struttura della mappa è la seguente
		//sinistra: indice che coincide con la posizione del tweet nell'array max
		//destra: tweet che  
		Map tweets= new HashMap();
		for (Tweet t : tc.getTweet()) {
			
			int temp = 0;
			//calcolo il costo relativi i termini
			for(String str : ti.getTweetTerms(t.getId())){
				if(solution.contains(str)){
					temp+=ti.getTermDocumentFrequency(str);
				}
			}
			
			//Questo permette di capire se è retwittato: però fa le richieste in tempo reale
			/*Twitter twitter = new TwitterFactory().getInstance();
			try {
				System.out.println(twitter.showStatus(t.getId()).isRetweet());
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(t.getId()+" nonet");
			}
			*/
			
			
			//Verifichiamo qual'è l'indice con frequenza minore
			//questa sarà la candidata per lasciare il posto al nuovo tweet con costo maggiore
			int min = 0;
			for(int i=1; i<max.length; i++){
				if (max[i]<max[min])
					min = i;
			}
			
			//inseriamo il tweet nella mappa, sempre se il suo costo è maggiore di uno di quelli gia presenti nella mappa
			if(temp>max[min]){
				max[min] = temp;
				tweets.put(min,t);
			}
		}
		return tweets;
	}

}