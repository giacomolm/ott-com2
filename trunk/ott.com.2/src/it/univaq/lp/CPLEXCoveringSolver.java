package it.univaq.lp;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sf.javailp.Solver;

public class CPLEXCoveringSolver implements CoveringSolver {

	private Solver solver;
	
	public CPLEXCoveringSolver(Solver solver) {
		this.solver = solver;
	}
	
	@Override
	public <E> double solve(Set<E> universe, Collection<Set<E>> subsets,
			Set<E> solution) {
		// TODO Auto-generated method stub
		try {
			IloCplex cplex = new IloCplex();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public <E> double solve(Map<E, Double> universe,
			Collection<Set<E>> subsets, Set<E> solution) {
		// TODO Auto-generated method stub
		return 0;
	}

}
