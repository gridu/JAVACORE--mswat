package externalsort;

import externalsort.FilesMerger;
import files.TempFileManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class FilesMergerTest {

    private File setUpTempSourceFile(String content) throws IOException {
        return TempFileManager.writeToTempFile(content);
    }

    private File setUpTempOutputFile() throws IOException {
        return TempFileManager.createTempFile("test", "deletion-scheduled");
    }

    @Test
    public void contentOfMergedFileShouldBeSorted() throws IOException {
        File sink = setUpTempOutputFile();
        List<File> tempFiles = List.of(setUpTempSourceFile("d\ne"), setUpTempSourceFile("a"));
        String expectedOutputContent = "a\nd\ne";
        final int SINGLE_TEMP_FILE_LENGTH = 4;


        FilesMerger.merge(tempFiles, sink, SINGLE_TEMP_FILE_LENGTH);

        assertEquals(expectedOutputContent, Files.readString(Paths.get(sink.getAbsolutePath())));
    }

}