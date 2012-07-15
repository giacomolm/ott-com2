package it.univaq.lp;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class CPLEXCoveringSolver implements CoveringSolver {
	
	@Override
	public <E> double solve(Set<E> universe, Collection<Set<E>> subsets,
			Set<E> solution) {
		// TODO Auto-generated method stub
		Map<E, Double> universeWithCost = new HashMap<E, Double>();
		for (E e : universe) {
			//Utilizzo la funzione con costi, ponendo tutti i costi pari ad 1
			universeWithCost.put(e, 1.0);
		}
		return solve(universeWithCost, subsets, solution);
	}

	@Override
	public <E> double solve(Map<E, Double> universe,
			Collection<Set<E>> subsets, Set<E> solution) {
		// TODO Auto-generated method stub
		
		IloCplex cplex = null;
		double result = 0;
		try {
			cplex = new IloCplex();
			
			//Costruisco le variabili del mio problema
			
			Map<IloIntVar, E> idMap = new HashMap<IloIntVar, E>();
			Map<E, IloIntVar> reverseIdMap = new HashMap<E, IloIntVar>();
			String[] ids = new String[universe.size()];
			int i = 0;
			
			//Devo necessariamente utilizzare le variabili cje rispettano la struttura prevista da CPLEX
			for(i=0; i<universe.size(); i++){
				String id = "x_"+i;
				ids[i] = id;
			}
			IloIntVar[] vars = cplex.boolVarArray(ids.length, ids);
			
			i=0;
			for (E e : universe.keySet()) {
				//costruisco l'insieme dell variabili associate alle parole del dizionario
				idMap.put(vars[i], e);
				reverseIdMap.put(e, vars[i]);
				i++;
			}
			//assegno le mie variabile binarie
			
			//Costruiamo la funzione obiettivo
			//Per prima cosa dobbiamo costruire l'espressione che la andrà a comporre 
			IloLinearNumExpr lin = cplex.linearNumExpr();
			
			for (Entry<E, Double> entry : universe.entrySet()) {
				//aggiungo alla funzione lineare con il primo parametro i coefficienti, mentre con il secondo settiamo le variabili
				lin.addTerm(entry.getValue(), reverseIdMap.get(entry.getKey())); //funzione obiettivo
			}
			
			//Imposto la funzione obiettivo
			cplex.addMinimize(lin);
			
			//Adesso devo impostare i constrain relativi i reali tweet
			
			int c = 1;
			//In questa sezione definisco i vincoli di copertura, cioè almeno una parola che appartiene allo specifico tweet deve essere nella soluzione finale
			for (Set<E> s : subsets) {
				//Ho scompattato l'insieme dei tweet 
				if (!s.isEmpty()) {
					IloLinearNumExpr expr = cplex.linearNumExpr();
					for (E e : s) {
						//per ogni parola contenuta nel tweet attuale inserisco la relativa variabile in modo tale do ottenere il vincolo di copertura
						expr.addTerm(1.0, reverseIdMap.get(e)); //vincoli sugli insiemi
					}
					//aggiungo il constraint relativo al tweet attuale
					cplex.addRange(1, expr, Double.MAX_VALUE,"c_"+c++);
				}
			}
			
			if ( cplex.solve() ) { 
		        for (i = 0; i < vars.length; i++) 
			         if(cplex.getValue(vars[i])==1){
			        	 solution.add(idMap.get(vars[i]));
			         } 
		     }
			else System.out.println("Error with the model");
			

			result = cplex.getObjValue();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}

}
