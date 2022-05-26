package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private Graph<String, DefaultWeightedEdge> grafo; // grafo semplice pesato non orientato, con i vertici che sono i tipi di reato (offense_type_id)
	private EventsDao dao;
	
	private List<String> best;
	
	// settiamo il dao = new EventsDAO();
	public Model() {
		dao = new EventsDao();
	}
	
	// creo il grafo semplice pesato non orientato dove gli passo la categoria di reato che è una stringa ed il mese
	// che mi servono per costruire il grafo;
	public void creaGrafo(String categoria, int mese) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		// aggiunta vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
	
		// aggiunta archi
		for(Adiacenza a : dao.getArchi(categoria, mese)) {
			Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(), a.getPeso());
		}
	}
		
//		// TODO riempire la tendina degli archi !
//		System.out.println("Grafo creato!");
//		System.out.println("# VERTICI: "+this.grafo.vertexSet().size());
//		System.out.println("# ARCHI: " +this.grafo.edgeSet().size());
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Adiacenza> getArchi() {
		List<Adiacenza> archi = new ArrayList<Adiacenza>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			archi.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int)this.grafo.getEdgeWeight(e)));
		}
		return archi;
	}
	
	public List<String> getCategorie() {
		return this.dao.getCategorie();
	}
	
	public List<Adiacenza> getArchiMaggioriPesoMedio() {
		// scorro gli archi del grafo e calcolo il peso medio
		double pesoTot = 0.0;
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			pesoTot += this.grafo.getEdgeWeight(e);
		}
		
		double avg = pesoTot / this.grafo.edgeSet().size();
		System.out.println("PESO MEDIO : "+avg);
		
		// ri-scorro tutti gli archi, prendendone quelli > di avg
		List<Adiacenza> result = new ArrayList<>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>avg)
				result.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int)this.grafo.getEdgeWeight(e)));	
		}
		return result;	
	}
	
	public List<String> calcolaPercorso(String sorgente, String destinazione) {	// o riceviamo l'adiacenza oppure sorgente e destinazione
		
		best = new LinkedList<>();
		List<String> parziale = new LinkedList<>();
		parziale.add(sorgente);
		
		// lancio la ricorsione, avrò il metodo cerca ricorsivo
		cerca(parziale, destinazione);	// 1 è il livello da cui lo faccio partire
		return best;
	}
	
	private void cerca(List<String> parziale, String destinazione) {
		// condizione di terminazione; Mi fermo quando arrivo a destinazione
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			// è la soluzione migliore?
			if(parziale.size() > best.size()) {
				best = new LinkedList<>(parziale);
			}
			return;
		}
		
		// scorro i vicini dell'ultimo inserito e provo le varie "strade"; Prendo il grafo, e l'ultimo inserito
		for(String v : Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(v)) {
				parziale.add(v);
				cerca(parziale,destinazione);
				parziale.remove(parziale.size()-1);
			}
		}
		
	}

	
}
