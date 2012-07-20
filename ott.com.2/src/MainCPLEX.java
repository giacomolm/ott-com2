import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import it.univaq.ir.TweetIndex;
import it.univaq.ir.TweetIndexImpl;
import it.univaq.ir.data.TweetCollectionPersister;
import it.univaq.ir.data.TweetCollectionPersisterImpl;
import it.univaq.ir.model.Tweet;
import it.univaq.ir.model.TweetCollection;
import it.univaq.ir.util.IndexComparator;
import it.univaq.lp.CPLEXCoveringSolver;
import it.univaq.lp.CoveringSolver;

public class MainCPLEX {

	/**
	 * @param args
	 * @throws TwitterException
	 * @throws IOException
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage:");
			System.out
					.println("\tDownload a tweet collection: -d path/to/the/collection query");
			System.out
					.println("\tCover a tweet collection: -c path/to/the/collection");
			System.out
					.println("\tCover a tweet collection excluding some terms: -ec path/to/the/collection word_to_exclude");
			System.exit(1);

		}

		try {
			if (args[0].equals("-d") && args.length == 3) {
				TweetCollectionPersister tcp = new TweetCollectionPersisterImpl();
				TweetCollection tc = tcp.downloadTweetCollection(args[2], 15,
						100);
				tcp.saveTweetCollection(args[1], tc);
				System.out.println("Done!");
			} else if ((args[0].equals("-c") && args.length == 2) || (args[0].equals("-ec") && args.length == 3)) {
				TweetCollectionPersister tcp = new TweetCollectionPersisterImpl();
				TweetCollection tc = tcp.loadTweetCollection(args[1]);
				
				TweetIndex ti = null;
				if (args[0].equals("-ec"))
					ti = new TweetIndexImpl(args[2]);
				else
					ti = new TweetIndexImpl();
				
				for (Tweet t : tc.getTweet()) {
					ti.insertTweet(t);
				}

				/*SolverFactory factory = new SolverFactoryGLPK();
				factory.setParameter(Solver.VERBOSE, 3);
				CoveringSolver mvcs = new ILPCoveringSolver(factory.get());
				Set<String> solution = new HashSet<String>();
				double val = mvcs.solve(ti.getDictionary(), ti.getTweetTerms(),
						solution);
				System.out.println(val + " " + solution);*/
				
				CoveringSolver mvcs = new CPLEXCoveringSolver();
				Set<String> solution = new HashSet<String>();
				double val = mvcs.solve(ti.getDictionary(), ti.getTweetTerms(),
						solution);
				List<String> sortedSolution = new ArrayList<String>(solution); 
				Collections.sort(sortedSolution, new IndexComparator(ti));
				Collections.reverse(sortedSolution);
				System.out.println("Number of element in the cover: "+val);
				for (String s : sortedSolution) {
					System.out.println(s+", freq: "+ti.getTermFrequency(s));
				}
				//System.out.println(val + " " + solution);
				
				//
				Map most = ti.getMostValuableTweet(tc, ti, solution);
				Iterator i = most.values().iterator();
				while(i.hasNext()){
					Tweet t = (Tweet) i.next();
					System.out.println(t.getText());
				}
				
			} else {
				System.out
						.println("Unknown options or wrong parameters number");
			}
		} catch (Exception e) {
			System.out.println("Ops! There is a problem:\n" + e);
		}
	}

}
