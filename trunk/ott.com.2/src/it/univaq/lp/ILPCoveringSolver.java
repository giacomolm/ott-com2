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
	
	/**
	 * @param universe insieme composto dal dizionario dell'insieme dei tweet
	 * @param subsets insieme composto dal stringhe contenute in ciascun tweet
	 * @param solution
	 */
	public <E> double solve(Set<E> universe, Collection<Set<E>> subsets, Set<E> solution) {
		Map<E, Double> universeWithCost = new HashMap<E, Double>();
		for (E e : universe) {
			//Utilizzo la funzione con costi, ponendo tutti i costi pari ad 1
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
	/**
	 * @param universe insieme composto dal dizionario dell'insieme dei tweet e i relativi pesi
	 * @param subsets insieme composto dal stringhe contenute in ciascun tweet
	 * @param solution
	 */
	public <E> double solve(Map<E, Double> universe,
			Collection<Set<E>> subsets, Set<E> solution) {						
		Problem p = new Problem();
		Linear obj = new Linear();
		
		int i = 0;
		Map<String, E> idMap = new HashMap<String, E>();
		Map<E, String> reverseIdMap = new HashMap<E, String>();		
		for (E e : universe.keySet()) {
			//costruisco l'insieme dell variabili associate alle parole del dizionario
			String id = "x_"+i++;
			idMap.put(id, e);
			reverseIdMap.put(e, id);
		}
		
		for (Entry<E, Double> entry : universe.entrySet()) {
			//aggiungo alla funzione lineare con il primo parametro i coefficienti, mentre con il secondo settiamo le variabili
			obj.add(entry.getValue(), reverseIdMap.get(entry.getKey())); //funzione obiettivo
		}
		//setto la funzione lineare precedentemente definita come funzione obiettivo
		p.setObjective(obj, OptType.MIN);
		
		int c = 1;
		//In questa sezione definisco i vincoli di copertura, cioè almeno una parola che appartiene allo specifico tweet deve essere nella soluzione finale
		for (Set<E> s : subsets) {
			//Ho scompattato l'insieme dei tweet 
			if (!s.isEmpty()) {
				Linear constraint = new Linear();
				for (E e : s) {
					//per ogni parola contenuta nel tweet attuale inserisco la relativa variabile in modo tale do ottenere il vincolo di copertura
					constraint.add(1.0, reverseIdMap.get(e)); //vincoli sugli insiemi
				}
				//aggiungo il constraint relativo al tweet attuale
				p.add("c_"+c++, constraint, ">=", 1.0);
			}
		}
		//vincoli di 'positività'
		int pos = 1;
		for (E e : universe.keySet()) {
			//per ciascuna parola del dizionario devo definire la relativa positività
			Linear posCostr = new Linear();
			posCostr.add(1.0, reverseIdMap.get(e));
			
			p.add("p_"+pos++, posCostr, ">=", 0.0);
		}				
		//vincoli di interezza
		for (E e : universe.keySet()) {
			//ciascuna variabile la setto intero
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
