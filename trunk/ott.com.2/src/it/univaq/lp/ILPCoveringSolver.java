package it.univaq.lp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.javailp.Constraint;
import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;

public class ILPCoveringSolver implements CoveringSolver{

	private Solver solver;
	
	public ILPCoveringSolver(Solver solver) {
		this.solver = solver;
	}
	
	public <E> double solve(Set<E> universe, Collection<Set<E>> subsets, Set<E> solution) {
		Map<E, Double> universeWithCost = new HashMap<E, Double>();
		for (E e : universe) {
			universeWithCost.put(e, 1.0);
		}
		return solve(universeWithCost, subsets, solution);
	}
	
//	public <E> double solve(Map<E, Double> universe,
//			Collection<Set<E>> subsets, Set<E> solution) {						
//		Problem p = new Problem();
//		Linear obj = new Linear();		
//		for (Entry<E, Double> entry : universe.entrySet()) {
//			obj.add(entry.getValue(), "x_"+entry.getKey()); //funzione obiettivo
//		}
//		p.setObjective(obj, OptType.MIN);
//		
//		int c = 0;
//		for (Set<E> s : subsets) {		
//			Linear constraint = new Linear();
//			for (E e : s) {
//				constraint.add(1.0, e); //vincoli
//			}
//			p.add("c"+c++, constraint, ">=", 1.0);
//		}
//		
//		//vincoli di interezza
//		for (E e : universe.keySet()) {
//			p.setVarType(e, Integer.class);
//		}
//		
//		System.out.println(p);
//		Result result = solver.solve(p);
//		
//		for (E e : universe.keySet()) {
//			if (result.getPrimalValue(e).doubleValue() != 0.0) solution.add(e);
//		}
//		
//		return (Double) result.getObjective();
//		
//	}
	
	public <E> double solve(Map<E, Double> universe,
			Collection<Set<E>> subsets, Set<E> solution) {						
		Problem p = new Problem();
		Linear obj = new Linear();
		
		int i = 0;
		Map<String, E> idMap = new HashMap<String, E>();
		Map<E, String> reverseIdMap = new HashMap<E, String>();		
		for (E e : universe.keySet()) {
			String id = "x_"+i++;
			idMap.put(id, e);
			reverseIdMap.put(e, id);
		}
		
		for (Entry<E, Double> entry : universe.entrySet()) {
			obj.add(entry.getValue(), reverseIdMap.get(entry.getKey())); //funzione obiettivo
		}
		p.setObjective(obj, OptType.MIN);
		
		int c = 1;
		for (Set<E> s : subsets) {
			if (!s.isEmpty()) {
				Linear constraint = new Linear();
				for (E e : s) {
					constraint.add(1.0, reverseIdMap.get(e)); //vincoli sugli insiemi
				}
				p.add("c_"+c++, constraint, ">=", 1.0);
			}
		}
		//vincoli di 'positività'
		int pos = 1;
		for (E e : universe.keySet()) {
			Linear posCostr = new Linear();
			posCostr.add(1.0, reverseIdMap.get(e));
			p.add("p_"+pos++, posCostr, ">=", 0.0);
		}				
		//vincoli di interezza
		for (E e : universe.keySet()) {
			p.setVarType(reverseIdMap.get(e), Integer.class);
		}
				
		System.err.println(p);
		Result result = solver.solve(p);
		
		for (E e : universe.keySet()) {
			if (result.getPrimalValue(reverseIdMap.get(e)).doubleValue() != 0.0) solution.add(e);
		}
		
		return (Double) result.getObjective();
		
	}	

}
