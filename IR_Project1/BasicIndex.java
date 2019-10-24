//Thanakorn Pasangthien 6088109 Sec 1
//Tanaporn Rojanaridpiched 6088146 Sec 3
//Nontapat Pintira 6088118 Sec 1

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Array;
import java.util.ArrayList;


public class BasicIndex implements BaseIndex {

    @Override
    public PostingList readPosting(FileChannel fc) {
        /*
         * TODO: Your code here
         *       Read and return the postings list from the given file.
         */
        int maxPostingPosition = 2;
        int[] posting = new int[2];
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES); // (Java Int = 32 bits) / (Byte Size = 8 bits) -> 4 bytes

        //readheader(Word Index, Word File Frequency)
        for (int i = 0; i < maxPostingPosition; i++) {
            try {
                // System.out.println("Position: " + fc.position() + "/" + fc.size());
                fc.read(buffer);
                buffer.flip(); //pointer back to first position in allocate
                posting[i] = buffer.getInt(); //get each posting header from reading buffer in int
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer.clear(); //clear buffer
        }
        buffer.clear();

        int sizeFactor = maxPrimeFactor(posting[1]);
        buffer = ByteBuffer.allocate(sizeFactor * Integer.BYTES); // (Java Int = 32 bits) / (Byte Size = 8 bits) -> 4 bytes

        ArrayList<Integer> documentId = new ArrayList<Integer>();
        for (int i = 0; i < posting[1] / sizeFactor; i++) {
            try {
                fc.read(buffer);
                buffer.flip();
                for (int j = 0; j < sizeFactor; j++) {
                    documentId.add(buffer.getInt());
                }
                buffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new PostingList(posting[0], documentId);
    }

    @Override
    public void writePosting(FileChannel fc, PostingList p) {
        /*
         * TODO: Your code here
         *       Write the given postings list to the given file.
         */

        ArrayList<Integer> writeData = new ArrayList<>();
        writeData.add(p.getTermId());
        writeData.add(p.getList().size());
        writeData.addAll(p.getList());

        int count = -1;
        int sizeFactor = maxPrimeFactor(writeData.size());
        ByteBuffer buffer = ByteBuffer.allocate(sizeFactor * Integer.BYTES); //Transform back to 4 bytes (1 Int = 4 Bytes) before write file

        for (int i = 0; i < writeData.size() / sizeFactor; i++) {
            for (int j = 0; j < sizeFactor; j++) {
                buffer.putInt(writeData.get(++count));
            }

            try {
                buffer.flip();
                fc.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                buffer.clear();
            }
        }
    }

    public static int maxPrimeFactor(int number) {

        int[] primeFactors = new int[number / 2];
        int count = 0;

        for (int i = 2; i <= number; i++) {
            while (number % i == 0) {
                primeFactors[count++] = i;
                number = number / i;
            }
        }

        if (primeFactors.length == 0){
            return number;
        }

        int max = primeFactors[0];

        for (int i = 0; i < primeFactors.length; i++) {
            if (max < primeFactors[i]) {
                max = primeFactors[i];
            }
        }

        return max;
    }
}