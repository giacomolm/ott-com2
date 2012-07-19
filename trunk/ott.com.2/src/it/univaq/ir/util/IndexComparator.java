package it.univaq.ir.util;

import it.univaq.ir.TweetIndex;

import java.util.Comparator;

public class IndexComparator implements Comparator<String>{

	private TweetIndex ti;
	
	public IndexComparator(TweetIndex ti) {
		this.ti = ti;
		// salva ti in un attributo privato
	}
	
	@Override
	public int compare(String o1, String o2) {
		int o1f = ti.getTermFrequency(o1);
		int o2f = ti.getTermFrequency(o2);
		return (o1f < o2f) ? -1 : (o1f > o2f) ? 1 : 0;
	}

}
