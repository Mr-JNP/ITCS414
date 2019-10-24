import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import opennlp.tools.stemmer.PorterStemmer;

/**
 * *****************  DO NOT MODIFY THIS FILE **************************
 * This abstract class provides default mechanisms to parse documents and preprocess textual content. It also provides
 * abstract methods for other searchers to extend. 
 * @author Dr. Suppawong Tuarob, (copyrighted 2018)
 *
 */
public abstract class Searcher {
	public static PorterStemmer porterStemmer = new PorterStemmer();
	public static final Set<String> stopWords = Stream.of("a","about","above","after","again","against","all","am","an","and","any","are","aren't","as","at","be","because","been","before","being","below","between","both","but","by","can't","cannot","could","couldn't","did","didn't","do","does","doesn't","doing","don't","down","during","each","few","for","from","further","had","hadn't","has","hasn't","have","haven't","having","he","he'd","he'll","he's","her","here","here's","hers","herself","him","himself","his","how","how's","i","i'd","i'll","i'm","i've","if","in","into","is","isn't","it","it's","its","itself","let's","me","more","most","mustn't","my","myself","no","nor","not","of","off","on","once","only","or","other","ought","our","ours","ourselves","out","over","own","same","shan't","she","she'd","she'll","she's","should","shouldn't","so","some","such","than","that","that's","the","their","theirs","them","themselves","then","there","there's","these","they","they'd","they'll","they're","they've","this","those","through","to","too","under","until","up","very","was","wasn't","we","we'd","we'll","we're","we've","were","weren't","what","what's","when","when's","where","where's","which","while","who","who's","whom","why","why's","with","won't","would","wouldn't","you","you'd","you'll","you're","you've","your","yours","yourself","yourselves").collect(Collectors.toSet());
	
	protected List<Document> documents = null;
	
	/**
	 * Default constructor. Load raw documents into Document objects in memory
	 * @param docFilename
	 */
	public Searcher(String docFilename)
	{
		this.documents = Searcher.parseDocumentFromFile(docFilename);
	}
	
	public static List<Document> parseDocumentFromFile(String filename)
	{
		//load the document file
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(new File(filename), "UTF-8");
		} catch (IOException e) {
			System.out.println("### Error reading file "+filename);
			e.printStackTrace();
			return null;
		}
				
		List<Document> documents = new Vector<Document>();
		for(String line: lines)
		{
			line = line.trim();
			if(line.isEmpty()) continue;
			//parse necessary document information
			String[] parts = line.split("\\t");
			Integer id = Integer.parseInt(parts[0]);
			String rawText = parts[1];
			List<String> tokens = tokenize(rawText);
			//add a document entry to documents
			Document doc = new Document(id, rawText, tokens);
			documents.add(doc);
		}
				
		System.out.println("@@@ Finished loading "+documents.size()+" documents from "+filename);
				
		return documents;
	}
	
	/**
	 * Default statis method for preprocessing and tokenizing raw text. You are required to use this method to 
	 * tokenize raw document and query text, to produce the same set of vocabulary.
	 * @param rawText
	 * @return
	 */
	public static List<String> tokenize(String rawText)
	{
		//lower casing
		String text = rawText.toLowerCase();
		
		//remove noise
		text = text.replaceAll("[^a-zA-Z0-9]", " ");
		
		//tokenizing
		String[] tokenArray = text.split("\\s+");
		
		//stemming, cleaning individual characters, and removing stop words
		List<String> tokens = new Vector<String>();
		for(String t: tokenArray)
		{
			if(t.length() <= 1) continue;
			if(stopWords.contains(t)) continue;
			 
			t = porterStemmer.stem(t);
			tokens.add(t);
		}
		//return
		return tokens;
	}
	
	/**
	 * Display search results in a more beautiful format
	 * @param results
	 */
	public static void displaySearchResults(List<SearchResult> results)
	{	StringBuilder str = new StringBuilder();
		for(int i = 0; i < results.size(); i++)
		{
			str.append("<"+(i+1)+">"+results.get(i));
		}
		System.out.println(str);
	}
	
	/**
	 * Abstract method for searching documents given a raw-text query, *queryString*. 
	 * Return a list of *k* SearchResult objects of the top documents that match the search query, 
	 * ranked by the SearchResult.score. If two documents have the same relevance score, the document whose
	 * document id is smaller is ranked higher.
	 * A SearchResult is basically an object containing a document and its relevance score.
	 * If there are fewer than k documents in the corpus, return the ranked list of all the documents
	 * The ranking will be determined by different search techniques. 
	 * @param queryString
	 * @return
	 */
	abstract public List<SearchResult> search(String queryString, int k);
}
