package files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TempFileManager {
    /**
     * Creates randomly named temp file which will be deleted after JVM shuts down.
     * @param prefix Fixed part of temp file name.
     * @param suffix Fixed part of temp file name.
     * @return File handler for newly created empty file.
     * @throws IOException
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * @param content String content which will be written to file.
     * @return File handler.
     * @throws IOException
     */
    public static File writeToTempFile(String content) throws IOException {
        File tempFile = createTempFile("tempfile", "deletion-scheduled");
        Files.writeString(Paths.get(tempFile.getAbsolutePath()), content);
        return tempFile;
    }
}
