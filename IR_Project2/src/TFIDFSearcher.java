//Name: 
//Section: 
//ID: 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TFIDFSearcher extends Searcher
{	
	Map<Integer, Map<String, Double>> tfidf = new HashMap<>();
	Map<String, Integer> dfs = new HashMap<>();
	Map<String, Double> idfs = new HashMap<>();
	
	public TFIDFSearcher(String docFilename) {
		super(docFilename); 
		/************* YOUR CODE HERE ******************/
		
//		Loop to count term frequency, document frequency.
		for(Document d: this.documents) {
			Set<String> terms = new HashSet<String>(d.getTokens());
			Map<String, Double> tfs = new HashMap<>();
			for(String term: terms) {
				if (dfs.get(term) != null) {
					dfs.put(term, dfs.get(term) + 1);
				}else {
					dfs.put(term, 0);
				}
				tfs.put(term, tf(d.getTokens(), term));
//				Line for debugging
//				System.out.println(term + " " + tfs.get(term));
			}
//			System.out.println(tfs.size());
			tfidf.put(d.getId(), tfs);
		}
		
//		Loop to calculate inverted document frequency
		for(String term: dfs.keySet()) {
			if(dfs.get(term) == 0) {
				idfs.put(term, (double) 0);
			}else {
				idfs.put(term, Math.log10(1 + (dfs.size() / (double) dfs.get(term))));
			}
//			Line for debugging
//			System.out.println(term + " " + dfs.get(term) + " " + idfs.get(term));
		}
		
//		Loop to calculate td-idf weight
		for(Integer i: tfidf.keySet()) {
			for(String term: tfidf.get(i).keySet()) {
				tfidf.get(i).put(term, tfidf.get(i).get(term) * idfs.get(term));
//				Line for debugging
//				System.out.println(term + " " + tfidf.get(i).get(term));
			}
		}
		
		/***********************************************/
	}
	
	public double tf(List<String> d, String t) {
		
		int f = 0;
		for(String s: d)
			if(t.equals(s)) f++;
		if(f == 0)	return 0;
		else		return 1 + Math.log10((double) f);
	}
	
	public double cosine(Map<String, Double> q, Map<String, Double> d) {
		
		double dot = 0, normq = 0, normd = 0;
		
		for(String t: dfs.keySet()) {
//			if(q.get(t) != null && d.get(t) != null) {
				dot += q.get(t) * d.get(t);
				normq += Math.pow(q.get(t), 2);
				normd += Math.pow(d.get(t), 2);
//			}
		}
		
//		System.out.println(sumDot + " " + sumQ + " " + sumQ);
//		System.out.println(dot/(Math.sqrt(normq) * Math.sqrt(normd)));
		return dot/(Math.sqrt(normq) * Math.sqrt(normd));
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
//			System.out.println(term + " " + tf(tokenize(queryString), term));
		}
		
		for(Document d: this.documents) {
			cosineScore.add(new SearchResult(d, cosine(qVector, tfidf.get(d.getId()))));
		}
		
		for(int i = 0; i < k; i++) {
			results.add(cosineScore.get(i));
		}
		
		return results;
		/***********************************************/
	}
}
