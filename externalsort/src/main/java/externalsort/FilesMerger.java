package externalsort;

import org.apache.commons.io.FileUtils;

import javax.naming.SizeLimitExceededException;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.stream.IntStream;

public class FilesMerger {
    public static void merge(List<File> tempFiles, File sinkFile, long maxBatchSize) throws IOException {
        StringLinesBuffer singleBatchContent = new StringLinesBuffer(maxBatchSize);

        try (LinesHeap files = new LinesHeap(tempFiles)) {

            while (files.hasNextLine()) {
                var nextLineInOrder = files.getNextLineInOrder();
                if (!singleBatchContent.canAddLine(nextLineInOrder)) {
                    appendToFile(sinkFile, String.format("%s\n", singleBatchContent.toString()));
                    singleBatchContent.reset();
                }
                singleBatchContent.addLine(nextLineInOrder);
            }
            appendToFile(sinkFile, singleBatchContent.toString());
        } catch (SizeLimitExceededException e) {
            throw new RuntimeException("Adding next line to buffer would overload it. However such case is guarded by canAddLine - check this method for bugs");
        }
    }

    private static void appendToFile(File destinationFile, String content) throws IOException {
        FileUtils.writeStringToFile(destinationFile, content, StandardCharsets.UTF_8, true);
    }

    private static class LinesHeap implements Closeable {
        private ArrayList<Scanner> filesScanners = new ArrayList<>();
        private PriorityQueue<LineWithScannerId> linesMinHeap = new PriorityQueue<>(LineWithScannerId::compareTo);

        /**
         * Heap consisting of lines that are read from temp files. Helps in fetching next line in order and allows
         * using try-with-resources construct.
         * @param files List of file handlers to temp files with sorted lines.
         */
        LinesHeap(List<File> files) throws FileNotFoundException {
            for (File file : files) {
                Scanner fileScanner = getFileScanner(file);
                filesScanners.add(fileScanner);
            }
            IntStream
                    .range(0, filesScanners.size())
                    .forEach(idx -> linesMinHeap.add(new LineWithScannerId(idx, filesScanners.get(idx).nextLine())));
        }

        String getNextLineInOrder() {
            LineWithScannerId nextLineInOrder = linesMinHeap.poll();
            var lineAssociatedScanner = filesScanners.get(nextLineInOrder.getId());
            if (lineAssociatedScanner.hasNextLine()) {
                linesMinHeap.add(new LineWithScannerId(nextLineInOrder.getId(), lineAssociatedScanner.nextLine()));
            }
            return nextLineInOrder.getLine();
        }

        boolean hasNextLine() {
            return !linesMinHeap.isEmpty();
        }

        private Scanner getFileScanner(File file) throws FileNotFoundException {
            try {
                return new Scanner(file);
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("LinesHeap could not find one of specified temp files.");
            }
        }

        @Override
        public void close() {
            filesScanners.forEach(Scanner::close);
        }

        private class LineWithScannerId implements Comparable {
            private final int id;
            private final String line;

            LineWithScannerId(int id, String line) {
                this.id = id;
                this.line = line;
            }

            int getId() {
                return id;
            }

            String getLine() {
                return line;
            }

            @Override
            public int compareTo(Object o) {
                LineWithScannerId other = (LineWithScannerId) o;
                return getLine().compareTo(other.getLine());
            }
        }
    }
}
