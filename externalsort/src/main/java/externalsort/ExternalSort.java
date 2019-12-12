package externalsort;

import java.io.File;
import java.io.IOException;

public class ExternalSort {
    /**
     * Sorts based on {@code String.compareTo} performed between lines.
     * Sorts source file content by splitting it to multiple smaller (size driven by {@code singleSubFileLengthLimit})
     * temp files, sorting their content and merging into final output file.
     * @param sourceFile File handler to source file.
     * @param outputFile File handler to empty file which will be filled with sorted content.
     * @param singleSubFileLengthLimit Maximal length in chars for single temp file.
     */
    public static void sort(File sourceFile, File outputFile, long singleSubFileLengthLimit) throws IOException {
            var tempFiles = FileSplitter.splitIntoMultipleTempFiles(singleSubFileLengthLimit, sourceFile);
            FilesMerger.merge(tempFiles, outputFile, singleSubFileLengthLimit);
    }
}
