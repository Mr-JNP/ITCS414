//Thanakorn Pasangthien 6088109 Sec 1
//Tanaporn Rojanaridpiched 6088146 Sec 3
//Nontapat Pintira 6088118 Sec 1

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Index {

    // Term id -> (position in index file, doc frequency) dictionary
    private static Map<Integer, Pair<Long, Integer>> postingDict
            = new TreeMap<Integer, Pair<Long, Integer>>();
    // Doc name -> doc id dictionary
    private static Map<String, Integer> docDict
            = new TreeMap<String, Integer>();
    // Term -> term id dictionary
    private static Map<String, Integer> termDict
            = new TreeMap<String, Integer>();
    // Block queue
    private static LinkedList<File> blockQueue
            = new LinkedList<File>();

    // Total file counter
    private static int totalFileCount = 0;
    // Document counter
    private static int docIdCounter = 0;
    // Term counter
    private static int wordIdCounter = 0;
    // Index
    private static BaseIndex index = null;


    /*
     * Write a posting list to the given file
     * You should record the file position of this posting list
     * so that you can read it back during retrieval
     *
     * */
    private static void writePosting(FileChannel fc, PostingList posting)
            throws IOException {
        /*
         * TODO: Your code here
         *
         */
        // call Basic Index
        long position = fc.position();
        postingDict.get(posting.getTermId()).setFirst(position);
        index.writePosting(fc, posting);
    }


    /**
     * Pop next element if there is one, otherwise return null
     *
     * @param iter an iterator that contains integers
     * @return next element or null
     */
    private static Integer popNextOrNull(Iterator<Integer> iter) {
        if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }


    /**
     * Main method to start the indexing process.
     *
     * @param method        :Indexing method. "Basic" by default, but extra credit will be given for those
     *                      who can implement variable byte (VB) or Gamma index compression algorithm
     * @param dataDirname   :relative path to the dataset root directory. E.g. "./datasets/small"
     * @param outputDirname :relative path to the output directory to store index. You must not assume
     *                      that this directory exist. If it does, you must clear out the content before indexing.
     */
    public static int runIndexer(String method, String dataDirname, String outputDirname) throws IOException {
        /* Get index */
        String className = method + "Index";
        try {
            Class<?> indexClass = Class.forName(className);
            index = (BaseIndex) indexClass.newInstance();
        } catch (Exception e) {
            System.err
                    .println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
            throw new RuntimeException(e);
        }

        /* Get root directory */
        File rootdir = new File(dataDirname);
        if (!rootdir.exists() || !rootdir.isDirectory()) {
            System.err.println("Invalid data directory: " + dataDirname);
            return -1;
        }


        /* Get output directory*/
        File outdir = new File(outputDirname);
        if (outdir.exists() && !outdir.isDirectory()) {
            System.err.println("Invalid output directory: " + outputDirname);
            return -1;
        }

        /*	TODO: delete all the files/sub folder under outdir
         *
         */
        for (File file : outdir.listFiles()) {
            if (file.isDirectory()) {
                file.delete();
            }
            file.delete();
        }

        /* BSBI indexing algorithm */
        File[] dirlist = rootdir.listFiles();

        /* For each block */
        for (File block : dirlist) {
            File blockFile = new File(outputDirname, block.getName());
            System.out.println("Processing block "+block.getName());
            blockQueue.add(blockFile);

            File blockDir = new File(dataDirname, block.getName());
            File[] filelist = blockDir.listFiles();
            Map<Integer, Set<Integer>> blockPosting = new TreeMap<>();

            /* For each file */
            for (File file : filelist) {
                ++totalFileCount;
                String fileName = block.getName() + "/" + file.getName();

                // use pre-increment to ensure docID > 0
                int docId = ++docIdCounter;
                docDict.put(fileName, docId);


                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.trim().split("\\s+");
                    for (String token : tokens) {
                        /*
                         * TODO: Your code here
                         *       For each term, build up a list of
                         *       documents in which the term occurs
                         */
                        int termId;
                        if (!termDict.containsKey(token)) {
                            termDict.put(token, ++wordIdCounter);
                            termId = wordIdCounter;
                        } else {
                            termId = termDict.get(token);
                        }

                        if (!postingDict.containsKey(termId)) {
                            postingDict.put(termId, null);
                        }

                        if (!blockPosting.containsKey(termId)) {
                            blockPosting.put(termId, new TreeSet<>());
                        }

                        blockPosting.get(termId).add(docId);
                    }
                }
                reader.close();
            }

            /* Sort and output */
            if (!blockFile.createNewFile()) {
                System.err.println("Create new block failure.");
                return -1;
            }

            RandomAccessFile bfc = new RandomAccessFile(blockFile, "rw");

            /*
             * TODO: Your code here
             *       Write all posting lists for all terms to file (bfc)
             */

            for (int key : blockPosting.keySet()) {
                PostingList indexBlock = new PostingList(key, new ArrayList<>(blockPosting.get(key)));

                if (postingDict.containsKey(indexBlock.getTermId())) {
                    Pair<Long, Integer> pair = postingDict.get(indexBlock.getTermId());
                    if (pair == null) {
                        postingDict.put(indexBlock.getTermId(), new Pair<>(-1L, 0));
                        pair = postingDict.get(indexBlock.getTermId());
                    }
                    pair.setSecond(pair.getSecond() + indexBlock.getList().size());
                }

                index.writePosting(bfc.getChannel(), indexBlock);
            }
        }

        /* Merge blocks */
        while (true) {
            if (blockQueue.size() <= 1)
                break;

            File b1 = blockQueue.removeFirst();
            File b2 = blockQueue.removeFirst();

            File combfile = new File(outputDirname, b1.getName() + "+" + b2.getName());
            if (!combfile.createNewFile()) {
                System.err.println("Create new block failure.");
                return -1;
            }

            RandomAccessFile bf1 = new RandomAccessFile(b1, "r");
            RandomAccessFile bf2 = new RandomAccessFile(b2, "r");
            RandomAccessFile mf = new RandomAccessFile(combfile, "rw");

            /*
             * TODO: Your code here
             *       Combine blocks bf1 and bf2 into our combined file, mf
             *       You will want to consider in what order to merge
             *       the two blocks (based on term ID, perhaps?).
             *
             */

            List<PostingList> mergedList = mergeBlock(bf1.getChannel(), bf2.getChannel(), index);

            for (PostingList posting : mergedList) {
//				System.out.println(posting.getTermId() + " " + posting.getList());
                writePosting(mf.getChannel(), posting);
            }
//			System.out.println();

            bf1.close();
            bf2.close();

            mf.close();

            b1.delete();
            b2.delete();
            blockQueue.add(combfile);
        }

        /* Dump constructed index back into file system */
        File indexFile = blockQueue.removeFirst();
        indexFile.renameTo(new File(outputDirname, "corpus.index"));

        BufferedWriter termWriter = new BufferedWriter(new FileWriter(new File(
                outputDirname, "term.dict")));
        for (String term : termDict.keySet()) {
            termWriter.write(term + "\t" + termDict.get(term) + "\n");
        }
        termWriter.close();

        BufferedWriter docWriter = new BufferedWriter(new FileWriter(new File(
                outputDirname, "doc.dict")));
        for (String doc : docDict.keySet()) {
            docWriter.write(doc + "\t" + docDict.get(doc) + "\n");
        }
        docWriter.close();

        BufferedWriter postWriter = new BufferedWriter(new FileWriter(new File(
                outputDirname, "posting.dict")));
        for (Integer termId : postingDict.keySet()) {
            postWriter.write(termId + "\t" + postingDict.get(termId).getFirst()
                    + "\t" + postingDict.get(termId).getSecond() + "\n");
        }
        postWriter.close();

        return totalFileCount;
    }

    public static void main(String[] args) throws IOException {
        /* Parse command line */
        if (args.length != 3) {
            System.err
                    .println("Usage: java Index [Basic|VB|Gamma] data_dir output_dir");
            return;
        }

        /* Get index */
        String className = "";
        try {
            className = args[0];
        } catch (Exception e) {
            System.err
                    .println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
            throw new RuntimeException(e);
        }

        /* Get root directory */
        String root = args[1];


        /* Get output directory */
        String output = args[2];
        runIndexer(className, root, output);
    }

    public static PostingList duplicatePostings(PostingList p1, PostingList p2) {
        if (p1.getTermId() != p2.getTermId()) {
            throw new IllegalArgumentException("Both Posting's termId should be the same");
        }

        TreeSet<Integer> documentIdSet = new TreeSet<>(p1.getList());

        documentIdSet.addAll(p2.getList());

        return new PostingList(p1.getTermId(), new ArrayList<>(documentIdSet));
    }

    public static List<PostingList> readPostingRest(FileChannel fileChannel, BaseIndex index) throws IOException {

        ArrayList<PostingList> postingLists = new ArrayList<>();

        try {
            while (fileChannel.position() < fileChannel.size()) {

                postingLists.add(index.readPosting(fileChannel));

            }

            return postingLists;
        } catch (IOException e) {

            e.printStackTrace();

            throw e;
        }
    }

    public static ArrayList<PostingList> mergeBlock(FileChannel fc1, FileChannel fc2, BaseIndex index) throws IOException {

        long i = 0;
        long j = 0;
        long sizeA = fc1.size();
        long sizeB = fc2.size();

        long previous_i, previous_j;

        ArrayList<PostingList> mergedResult = new ArrayList<>();

        while (i < sizeA && j < sizeB) {
            previous_i = fc1.position();
            PostingList p1 = index.readPosting(fc1);
            i = previous_i;

            previous_j = fc2.position();
            PostingList p2 = index.readPosting(fc2);
            j = previous_j;

            if (p1.getTermId() < p2.getTermId()) {
                mergedResult.add(p1);
                i = fc1.position();
                fc1.position(i);

                fc2.position(j);
            } else if (p1.getTermId() > p2.getTermId()) {
                mergedResult.add(p2);
                j = fc2.position();
                fc2.position(j);

                fc1.position(i);
            } else {
                mergedResult.add(duplicatePostings(p1, p2));
                i = fc1.position();
                j = fc2.position();

                fc1.position(i);
                fc2.position(j);
            }
        }

        if (i != sizeA) {
            mergedResult.addAll(readPostingRest(fc1, index));
        }

        if (j != sizeB) {
            mergedResult.addAll(readPostingRest(fc2, index));
        }


        return mergedResult;

    }
}



