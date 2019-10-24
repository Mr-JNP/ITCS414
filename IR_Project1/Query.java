//Thanakorn Pasangthien 6088109 Sec 1
//Tanaporn Rojanaridpiched 6088146 Sec 3
//Nontapat Pintira 6088118 Sec 1

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Query {

	// Term id -> position in index file
	private Map<Integer, Long> posDict = new TreeMap<Integer, Long>();
	// Term id -> document frequency
	private Map<Integer, Integer> freqDict = new TreeMap<Integer, Integer>();
	// Doc id -> doc name dictionary
	private Map<Integer, String> docDict = new TreeMap<Integer, String>();
	// Term -> term id dictionary
	private Map<String, Integer> termDict = new TreeMap<String, Integer>();
	// Index
	private BaseIndex index = null;

	// indicate whether the query service is running or not
	private boolean running = false;
	private RandomAccessFile indexFile = null;

	/*
	 * Read a posting list with a given termID from the file You should seek to the
	 * file position of this specific posting list and read it back.
	 */
	private PostingList readPosting(FileChannel fc, int termId) throws IOException {
		/*
		 * TODO: Your code here
		 */

		Long position = posDict.get(termId);

		if (position == null) {

			return null;
		}

		fc.position(position);
		return index.readPosting(fc);
	}

	public void runQueryService(String indexMode, String indexDirname) throws IOException {
		// Get the index reader
		try {
			Class<?> indexClass = Class.forName(indexMode + "Index");
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		// Get Index file
		File inputdir = new File(indexDirname);
		if (!inputdir.exists() || !inputdir.isDirectory()) {
			System.err.println("Invalid index directory: " + indexDirname);
			return;
		}

		/* Index file */
		indexFile = new RandomAccessFile(new File(indexDirname, "corpus.index"), "r");

		String line = null;
		/* Term dictionary */
		BufferedReader termReader = new BufferedReader(new FileReader(new File(indexDirname, "term.dict")));
		while ((line = termReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			termDict.put(tokens[0], Integer.parseInt(tokens[1]));
		}
		termReader.close();

		/* Doc dictionary */
		BufferedReader docReader = new BufferedReader(new FileReader(new File(indexDirname, "doc.dict")));
		while ((line = docReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			docDict.put(Integer.parseInt(tokens[1]), tokens[0]);
		}
		docReader.close();

		/* Posting dictionary */
		BufferedReader postReader = new BufferedReader(new FileReader(new File(indexDirname, "posting.dict")));
		while ((line = postReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			posDict.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
			freqDict.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[2]));
		}
		postReader.close();

		this.running = true;
	}

	public List<Integer> retrieve(String query) throws IOException {
		if (!running) {
			System.err.println("Error: Query service must be initiated");
		}

		/*
		 * TODO: Your code here Perform query processing with the inverted index. return
		 * the list of IDs of the documents that match the query
		 *
		 */

		ArrayList<String> queries;
		ArrayList<Integer> termIds = new ArrayList<>();

		String[] tokens = query.trim().split("\\s+");
		queries = new ArrayList<>(Arrays.asList(tokens));

		for (String processQuery : queries) {
			if (termDict.get(processQuery) != null) {
				termIds.add(termDict.get(processQuery));
			}
		}

		HashMap<Integer, PostingList> termPosting = new HashMap<>();

		for (Integer termId : termIds) {
			try {
				termPosting.put(termId, readPosting(indexFile.getChannel(), termId));
			} catch (IOException | NullPointerException e) {
				e.printStackTrace();
			}
		}

		ArrayList<PostingList> postingLists = new ArrayList<>(termPosting.values());

		postingLists.sort(new Comparator<PostingList>() {
			@Override
			public int compare(PostingList p1, PostingList p2) {
				return p1.getList().size() - p2.getList().size();
			}
		});

		return booleanRetrieval(postingLists);

	}

	String outputQueryResult(List<Integer> res) {
		/*
		 * TODO:
		 *
		 * Take the list of documents ID and prepare the search results, sorted by
		 * lexicon order.
		 *
		 * E.g. 0/fine.txt 0/hello.txt 1/bye.txt 2/fine.txt 2/hello.txt
		 *
		 * If there no matched document, output:
		 *
		 * no results found
		 *
		 */
		StringBuilder resultString = new StringBuilder();

		Collections.sort(res);

		if (!res.isEmpty()) {

			for (int docId : res) {

				String docName = docDict.get(docId);

				resultString.append(docName).append("\n");
			}
		} else {

			resultString.append("no results found");
		}

		return resultString.toString();
	}

	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 2) {
			System.err.println("Usage: java Query [Basic|VB|Gamma] index_dir");
			return;
		}

		/* Get index */
		String className = null;
		try {
			className = args[0];
		} catch (Exception e) {
			System.err.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get index directory */
		String input = args[1];

		Query queryService = new Query();
		queryService.runQueryService(className, input);

		/* Processing queries */
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/* For each query */
		String line = null;
		while ((line = br.readLine()) != null) {
			List<Integer> hitDocs = queryService.retrieve(line);
			queryService.outputQueryResult(hitDocs);
		}

		br.close();
	}

	protected void finalize() {
		try {
			if (indexFile != null) {
				indexFile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final Comparator<Integer> COMPARATOR_INTEGER = Comparator.comparingInt(o -> o);

	public static <T> List<T> intersect(List<T> listA, List<T> listB, Comparator<T> comparator) {
		ArrayList<T> intersected = new ArrayList<>();

		int i = 0;
		int j = 0;
		int sizeA = listA.size();
		int sizeB = listB.size();

		while (i < sizeA && j < sizeB) {

			int comparison = comparator.compare(listA.get(i), listB.get(j));

			if (comparison < 0) {

				i++;

			} else if (comparison > 0) {

				j++;

			} else {

				intersected.add(listB.get(j++));

				i++;
			}
		}

		intersected.sort(comparator);

		return intersected;
	}

	public static List<Integer> booleanRetrieval(List<PostingList> postingLists) {

		if (postingLists.isEmpty()) {

			return new ArrayList<>();

		} else if (postingLists.size() == 1) {

			return new ArrayList<>(postingLists.get(0).getList());

		}

		ArrayList<Integer> firstElem = new ArrayList<>(postingLists.get(0).getList());
		ArrayList<Integer> secondElem = new ArrayList<>(postingLists.get(1).getList());

		List<Integer> intersectedDoc = intersect(firstElem, secondElem, COMPARATOR_INTEGER);

		for (int i = 1; i < postingLists.size(); i++) {

			PostingList nextPosting = null;

			try {

				nextPosting = postingLists.get(i + 1);

			} catch (IndexOutOfBoundsException e) {

				break;
			}

			intersectedDoc = intersect(intersectedDoc, nextPosting.getList(), COMPARATOR_INTEGER);
		}

		return intersectedDoc;

	}

}
