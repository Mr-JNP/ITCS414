import java.util.Arrays;
import java.util.List;

/**
 * Class containing a set of test-cases.
 * @author Dr. Suppawong Tuarob (copyrighted)
 *
 */
public class StudentTester {
	
	//*********************** DO NOT MODIFY THESE VARIABLES *****************************//
	public static final String testCorpus = "./data/lisa";
	public static final int k = 10;
	public static final String[] testQueries = new String[] {
			""
			,"Information Retrieval"
			,"Machine Learning"
			,"Deep Learning"
			,"I AM INTERESTED IN INFORMATION ON THE PROVISION OF CURRENT AWARENESS BULLETINS, ESPECIALLY SDI SERVICES IN ANY INSTITUTION, E.G. ACADEMIC LIBRARIES, INDUSTRY, AND IN ANY SUBJECT FIELD. SDI, SELECTIVE DISSEMINATION OF INFORMATION, CURRENT AWARENESS BULLETINS, INFORMATION BULLETINS."
			,"THE WHITE HOUSE CONFERENCE ON LIBRARY AND INFORMATION SERVICES, 1979. SUMMARY, MARCH 1980. FOR AN ABSTRACT OF THIS REPORT SEE 81/795. REPORT NOT AVAILABLE FROM NTIS."
	};
	
	//*********************** DO NOT MODIFY THIS METHOD *****************************//
	public static void testJaccardSearcher(String corpus)
	{
		System.out.println("@@@ Testing Jaccard-based documents searcher on "+corpus);
		String documentFilename = corpus+"/documents.txt";
		long startTime = System.currentTimeMillis();
		//initialize search engine
		Searcher searcher = new JaccardSearcher(documentFilename);
		for(String query: testQueries)
		{
			List<SearchResult> results = searcher.search(query, k);
			System.out.println("@@@ Results: "+(query.length() > 50? query.substring(0, 50)+"...":query));
			Searcher.displaySearchResults(results);
			System.out.println();
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("@@@ Total time used: "+(endTime-startTime)+" milliseconds.");
	}
	
	//*********************** DO NOT MODIFY THIS METHOD *****************************//
	public static void testTFIDFSearcher(String corpus)
	{
		System.out.println("@@@ Testing TFIDF-based documents searcher on "+corpus);
		String documentFilename = corpus+"/documents.txt";
		long startTime = System.currentTimeMillis();
		//initialize search engine
		Searcher searcher = new TFIDFSearcher(documentFilename);
		for(String query: testQueries)
		{
			List<SearchResult> results = searcher.search(query, k);
			System.out.println("@@@ Results: "+(query.length() > 50? query.substring(0, 50)+"...":query));
			Searcher.displaySearchResults(results);
			System.out.println();
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("@@@ Total time used: "+(endTime-startTime)+" milliseconds.");
	}
	
	//*********************** DO NOT MODIFY THIS METHOD *****************************//
	public static void testCompareTwoSearchersOnSomeQueries(String corpus)
	{
		System.out.println("@@@ Comparing two searchers on some queries in "+corpus);
		long startTime = System.currentTimeMillis();
		SearcherEvaluator eval = new SearcherEvaluator(testCorpus);
		Searcher jSearcher = new JaccardSearcher(testCorpus+"/documents.txt");
		Searcher tSearcher = new TFIDFSearcher(testCorpus+"/documents.txt");
		
		int[] qIndexes = new int[3];
		qIndexes[0] = 0;
		qIndexes[1] = eval.getQueries().size()/2;
		qIndexes[2] = eval.getQueries().size()-1;
		
		for(int qIndex: qIndexes)
		{
			System.out.println("@@@ Query: "+eval.getQueries().get(qIndex));
			double[] jResults = eval.getQueryPRF(eval.getQueries().get(qIndex), jSearcher, k);
			double[] tResults = eval.getQueryPRF(eval.getQueries().get(qIndex), tSearcher, k);
			System.out.println("\tJaccard (P,R,F): "+Arrays.toString(jResults));
			System.out.println("\tTFIDF (P,R,F): "+Arrays.toString(tResults));
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("@@@ Total time used: "+(endTime-startTime)+" milliseconds.");
	}
	
	//*********************** DO NOT MODIFY THIS METHOD *****************************//
	public static void testCompareTwoSearchersOnAllQueries(String corpus)
	{
		System.out.println("@@@ Comparing two searchers on all the queries in "+corpus);
		long startTime = System.currentTimeMillis();
		SearcherEvaluator s = new SearcherEvaluator(corpus);
		Searcher jSearcher = new JaccardSearcher(testCorpus+"/documents.txt");
		Searcher tSearcher = new TFIDFSearcher(testCorpus+"/documents.txt");
		
		double[] jResults = s.getAveragePRF(jSearcher, k);
		double[] tResults = s.getAveragePRF(tSearcher, k);
		System.out.println("@@@ Jaccard: "+Arrays.toString(jResults));
		System.out.println("@@@ TFIDF: "+Arrays.toString(tResults));
		long endTime = System.currentTimeMillis();
		System.out.println("@@@ Total time used: "+(endTime-startTime)+" milliseconds.");
	}
	
	public static void testYourSearcher(String corpus)
	{
		//YOUR CODE HERE (BONUS)
	}
	
	public static void main(String[] args)
	{	
		/********************* Uncomment test cases you want to test ***************/
		testJaccardSearcher(testCorpus);
		//testTFIDFSearcher(testCorpus);
		//testCompareTwoSearchersOnSomeQueries(testCorpus);
		//testCompareTwoSearchersOnAllQueries(testCorpus);
		
		//********** BONUS **************//
		//testYourSearcher(testCorpus);
		//*******************************//
	}

}
