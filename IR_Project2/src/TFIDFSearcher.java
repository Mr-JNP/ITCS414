//Name: 
//Section: 
//ID: 

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TFIDFSearcher extends Searcher
{	
	double norm;
	Map<Integer, Map<String, Double>> tfidf = new HashMap<>();
	Map<String, Integer> dfs = new HashMap<>();
	Map<String, Double> idfs = new HashMap<>();
	Map<Integer, Double> normds = new HashMap<>();
	
	public TFIDFSearcher(String docFilename) {
		super(docFilename); 
		/************* YOUR CODE HERE ******************/
		
//		Loop to count term frequency, document frequency.
		for(Document d: this.documents) {
			Set<String> terms = new HashSet<String>(d.getTokens());
			Map<String, Double> tfs = new HashMap<>();
			for(String term: terms) {
				
//				Count document frequency
//				If the term already exist then increment
//				Add the the term to the map otherwise
				if (dfs.get(term) != null)	dfs.put(term, dfs.get(term) + 1);
				else						dfs.put(term, 1);
				
				tfs.put(term, tf(d.getTokens(), term));
			}
			tfidf.put(d.getId(), tfs);
		}
		
//		Loop to calculate inverted document frequency
		for(String term: dfs.keySet()) {
			idfs.put(term, Math.log10(1 + ((double) this.documents.size() / dfs.get(term))));
		}
		
//		Loop to calculate td-idf weight
		for(Integer i: tfidf.keySet()) {
			norm = 0;
			for(String term: tfidf.get(i).keySet()) {
//				System.out.println(i + ": " + term + ", " + tfidf.get(i).get(term) + ", " + idfs.get(term) + ", " + tfidf.get(i).get(term) * idfs.get(term));
				tfidf.get(i).put(term, tfidf.get(i).get(term) * idfs.get(term));
				norm += Math.pow(tfidf.get(i).get(term), 2);
			}
			normds.put(i, Math.sqrt(norm));
		}
		/***********************************************/
	}
	
	
	public double tf(List<String> d, String t) {
		int f = 0;
		for(String s: d)
			if(t.equals(s)) f++;
		if(f == 0)	return 0;
		else		return 1 + Math.log10(f);
	}
	
	public double cosine(Map<String, Double> query, int docId) {
		
		double dot = 0, normq = 0, normd;
		HashSet<String> intersection = new HashSet<>(query.keySet());
//		Find norm of the query vector
		for(String term: query.keySet()) {
			normq += Math.pow(query.get(term), 2);
		}
		normq = Math.sqrt(normq);
		
//		Get norm of document with document id
		normd = normds.get(docId);
		
//		Find dot product
		intersection.retainAll(tfidf.get(docId).keySet());
		for(String term: intersection) {
			dot += query.get(term) * tfidf.get(docId).get(term);
		}
		
//		System.out.println(dot + " " + normq + " " + normd);
		return dot/(normq*normd);
	}
	
	
	@Override
	public List<SearchResult> search(String queryString, int k) {
		/************* YOUR CODE HERE ******************/
		List<SearchResult> cosineScore = new ArrayList<>();
		List<SearchResult> results = new ArrayList<>();
		Map<String, Double> qVector = new HashMap<>();
		
//		Create set of unique term for query
		Set<String> qTerm = new HashSet<>(tokenize(queryString));
		
//		Create the vector of the query
		for(String term: qTerm) {
			qVector.put(term, tf(tokenize(queryString), term) * idfs.get(term));
		}
		
//		Calculate cosine similarity
		for(Document d: this.documents) {
			cosineScore.add(new SearchResult(d, cosine(qVector, d.getId())));
		}
		
		Collections.sort(cosineScore);
		
		for(int i = 0; i < k; i++) {
			results.add(cosineScore.get(i));
		}
		
		return results;
		/***********************************************/
	}
}
