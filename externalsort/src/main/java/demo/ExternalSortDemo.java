package demo;

import files.TempFileManager;
import externalsort.ExternalSort;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ExternalSortDemo {
    public static void main(String[] args) {
        String sourceContent = "a\nz\nf\nq\ne\na";
        try {
            File source = TempFileManager.writeToTempFile(sourceContent);
            File sink = TempFileManager.createTempFile("whatever", "will-be-deleted");
            ExternalSort.sort(source, sink, 3);
            System.out.println(Files.readString(Paths.get(sink.getAbsolutePath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
