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
	/*
	 * double norm This is a tempory variable to store the norm of a document vector.
	 * Map<Integer, Map<String, Double>> tfidf This is a map which maps document id to document vector.
	 * Map<String, Integer> dfs This is a map which maps a term to its document frequency.
	 * Map<String, Double> idfs This is a map which maps a term to its inverted document frequency.
	 * Map<Integer, Double> normds This is a map which maps a document id to its norm.
	 */
	double norm;
	Map<Integer, Map<String, Double>> tfidf = new HashMap<>();
	Map<String, Integer> dfs = new HashMap<>();
	Map<String, Double> idfs = new HashMap<>();
	Map<Integer, Double> normds = new HashMap<>();
	
	public TFIDFSearcher(String docFilename) {
		super(docFilename); 
		/************* YOUR CODE HERE ******************/
		/*
		 * Loop to count term frequency and document frequency
		 * 1. Create set to keep unique terms
		 * 2. Iterate through each term to count document frequency and put it in a map
		 * 	2.1. If the term already exist then increment the df of the given term
		 * 	2.2. Add new term to the map otherwise.
		 * 3. For each term in the document, count term frequency and put it in another map
		 */
		for(Document d: this.documents) {
			Set<String> terms = new HashSet<String>(d.getTokens());
			Map<String, Double> tfs = new HashMap<>();
			for(String term: terms) {
				if (dfs.get(term) != null)
					dfs.put(term, dfs.get(term) + 1);
				else
					dfs.put(term, 1);
				tfs.put(term, tf(d.getTokens(), term));
			}
			tfidf.put(d.getId(), tfs);
		}
		/*
		 * For each term, calculate inverted document frequency
		 */
		for(String term: dfs.keySet()) {
			idfs.put(term, Math.log10(1 + ((double) this.documents.size() / dfs.get(term))));
		}
		/*
		 * 1. For each document, calculate tfidf weight. (As a vector)
		 * 2. Calculate the norm of each vector, and store it in a map.
		 */
		for(Integer i: tfidf.keySet()) {
			norm = 0;
			for(String term: tfidf.get(i).keySet()) {
				tfidf.get(i).put(term, tfidf.get(i).get(term) * idfs.get(term));
				norm += Math.pow(tfidf.get(i).get(term), 2);
			}
			normds.put(i, Math.sqrt(norm));
		}
		/***********************************************/
	}
	
	/*
	 * Calculate term frequency
	 * @param d This is the list of token in a document.
	 * @param t This is the term that we want to check for its frequency.
	 * @return double This returns the term frequency (with logarithmic function).
	 */
	public double tf(List<String> d, String t) {
		int f = 0;
		for(String s: d)
			if(t.equals(s)) f++;
		if(f == 0)	return 0;
		else		return 1 + Math.log10(f);
	}
	
	/*
	 * Calculate cosine similarity score
	 * 1. Calculate the norm of query vector and document vector>
	 * 2. Calculate the dot product
	 * @param query This is the query vector.
	 * @param docId This is the document id of the document we are comparing.
	 * @return double This returns the cosine similarity of the query and the document.
	 */
	public double cosine(Map<String, Double> query, int docId) {
		double dot = 0, normq = 0, normd;
		HashSet<String> intersection = new HashSet<>(query.keySet());
		
		for(String term: query.keySet())
			normq += Math.pow(query.get(term), 2);
		normq = Math.sqrt(normq);
		normd = normds.get(docId);
		
		intersection.retainAll(tfidf.get(docId).keySet());
		for(String term: intersection)
			dot += query.get(term) * tfidf.get(docId).get(term);
		
		return dot/(normq*normd);
	}
	
	/*
	 * Get top K result using cosine similarity score
	 * @param queryString This is the query.
	 * @param k The number of result that the system returns.
	 * @return List<SearchResult> This return a list of search result type of top k result.
	 * 
	 * 1. Construct a query vector
	 * 2. Compute cosine similarity score
	 * 3. Sort the list
	 * 4. Get top K result
	 */
	@Override
	public List<SearchResult> search(String queryString, int k) {
		/************* YOUR CODE HERE ******************/
		List<SearchResult> cosineScore = new ArrayList<>();
		List<SearchResult> results = new ArrayList<>();
		Map<String, Double> qVector = new HashMap<>();
		Set<String> qTerm = new HashSet<>(tokenize(queryString));
		
		for(String term: qTerm) {
			qVector.put(term, tf(tokenize(queryString), term) * idfs.get(term));
		}
		
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
