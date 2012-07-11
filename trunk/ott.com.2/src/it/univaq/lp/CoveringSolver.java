package it.univaq.lp;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface CoveringSolver {
	public <E> double solve(Set<E> universe, Collection<Set<E>> subsets, Set<E> solution);	
	public <E> double solve(Map<E, Double> universe,
			Collection<Set<E>> subsets, Set<E> solution);
}
