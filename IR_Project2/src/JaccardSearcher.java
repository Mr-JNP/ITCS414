//Name:		Thanakorn Pasangthein, Nontapat Pintira, Tanaporn Rojanaridpiched
//Section: 	6088109, 6088118, 6088146
//ID: 		1, 1, 3

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JaccardSearcher extends Searcher{

	public JaccardSearcher(String docFilename) {
		super(docFilename);
		/************* YOUR CODE HERE ******************/								
		/***********************************************/
	} 

	@Override
	public List<SearchResult> search(String queryString, int k) {
		/************* YOUR CODE HERE ******************/
		double jaccardScore, i_size, u_size;
		List<SearchResult> jaccards = new ArrayList<SearchResult>();
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		// Create a set of query terms.
		Set<String> q = new HashSet<String>(new ArrayList<String>(tokenize(queryString)));
		
		// Run for loop to check Jaccard similarity for all documents.
		for(Document d: this.documents) {
			
			// Create set of document terms.
			Set<String> docTerms = new HashSet<>(d.getTokens());
			Set<String> intersect = new HashSet<>(q);
			Set<String> union = new HashSet<>(q);
			
			// Find the intersection and union of the query terms and document terms.
			intersect.retainAll(docTerms);
			union.addAll(docTerms);
			
			// Calculate Jaccard similarity.
			i_size = intersect.size();
			u_size = union.size();
			jaccardScore = i_size/u_size;
			
			// Add a search result into the list.
			jaccards.add(new SearchResult(d, jaccardScore));	
		}
		
		// Sort the search result by id (Comparator is implemented in SearchResult class).
		Collections.sort(jaccards);

		// Get top k result.
		for(int i = 0; i < k; i++) {
			results.add(jaccards.get(i));
		}
		
		return results;
		/***********************************************/
	}

}
