package externalsort;

import externalsort.FileSplitter;
import files.TempFileManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class FileSplitterTest {

    private File setUpTempSourceFile(String content) throws IOException {
        return TempFileManager.writeToTempFile(content);
    }

    @Test
    public void shouldSplitSourceFileIntoExpectedNumberOfTempFiles() throws IOException {
        final int PARTIAL_FILE_LENGTH = 4;
        String sourceContent = String.join("\n", List.of("a", "b", "c", "d"));
        File sourceFile = setUpTempSourceFile(sourceContent);
        final int expectedNumberOfTempFiles = 2;

        List<File> tempFiles = FileSplitter.splitIntoMultipleTempFiles(PARTIAL_FILE_LENGTH, sourceFile);

        assertEquals(expectedNumberOfTempFiles, tempFiles.size());
    }

    @Test
    public void shouldCreateSingleFileWhenSourceFileIsSmallerThanPartialFileLength() throws IOException {
        final int PARTIAL_FILE_LENGTH = 4;
        String sourceContent = "a\nb";
        File sourceFile = setUpTempSourceFile(sourceContent);
        final int expectedNumberOfTempFiles = 1;

        List<File> tempFiles = FileSplitter.splitIntoMultipleTempFiles(PARTIAL_FILE_LENGTH, sourceFile);

        assertEquals(expectedNumberOfTempFiles, tempFiles.size());
        assertEquals(sourceContent, Files.readString(Paths.get(tempFiles.get(0).getAbsolutePath())));
    }

    @Test
    public void shouldSortTempFilesContent() throws IOException {
        final int PARTIAL_FILE_LENGTH = 4;
        String sourceContent = "b\na\nc";
        File sourceFile = setUpTempSourceFile(sourceContent);
        String firstExpectedSortedTempFileContent = "a\nb";
        String secondExpectedSortedTempFileContent = "c";
        final int expectedNumberOfTempFiles = 2;

        List<File> tempFiles = FileSplitter.splitIntoMultipleTempFiles(PARTIAL_FILE_LENGTH, sourceFile);

        assertEquals(expectedNumberOfTempFiles, tempFiles.size());
        assertEquals(firstExpectedSortedTempFileContent, Files.readString(Paths.get(tempFiles.get(0).getAbsolutePath())));
        assertEquals(secondExpectedSortedTempFileContent, Files.readString(Paths.get(tempFiles.get(1).getAbsolutePath())));
    }
}