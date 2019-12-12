package externalsort;

import files.TempFileManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ExternalSortTest {

    private File setUpTempSourceFile(String content) throws IOException {
        return TempFileManager.writeToTempFile(content);
    }

    private File setUpTempOutputFile() throws IOException {
        return TempFileManager.createTempFile("test", "deletion-scheduled");
    }

    @Test
    public void shouldSortSimpleFile() throws IOException {
        final int SINGLE_TEMP_FILE_LENGTH = 5;

        String unsortedContent = "d\ne\na\nb";
        File source = setUpTempSourceFile(unsortedContent);
        File sink = setUpTempOutputFile();
        String expectedContent = "a\nb\nd\ne";

        ExternalSort.sort(source, sink, SINGLE_TEMP_FILE_LENGTH);

        assertEquals(expectedContent, Files.readString(Paths.get(sink.getAbsolutePath())));
    }

    @Test
    public void shouldSortFileWithDuplicatedLines() throws IOException {
        final int SINGLE_TEMP_FILE_LENGTH = 5;

        String unsortedContent = "d\ne\na\nb\na\na";
        File source = setUpTempSourceFile(unsortedContent);
        File sink = setUpTempOutputFile();
        String expectedContent = "a\na\na\nb\nd\ne";

        ExternalSort.sort(source, sink, SINGLE_TEMP_FILE_LENGTH);

        assertEquals(expectedContent, Files.readString(Paths.get(sink.getAbsolutePath())));
    }

    @Test
    public void shouldNotFailWhenFileIsEmpty() throws IOException {
        final int SINGLE_TEMP_FILE_LENGTH = 5;

        String unsortedContent = "";
        File source = setUpTempSourceFile(unsortedContent);
        File sink = setUpTempOutputFile();
        String expectedContent = "";

        ExternalSort.sort(source, sink, SINGLE_TEMP_FILE_LENGTH);

        assertEquals(expectedContent, Files.readString(Paths.get(sink.getAbsolutePath())));
    }


}