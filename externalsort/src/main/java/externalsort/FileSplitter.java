package externalsort;

import files.TempFileManager;

import javax.naming.SizeLimitExceededException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileSplitter {

    public static List<File> splitIntoMultipleTempFiles(long maxBatchSize, File sourceFile) throws  IOException {
        var tempFilesPaths = new ArrayList<File>();
        StringLinesBuffer singleBatchContent = new StringLinesBuffer(maxBatchSize);

        try (var fileScanner = new Scanner(sourceFile)) {

            while (fileScanner.hasNextLine()) {
                String nextLine = fileScanner.nextLine();
                if (!singleBatchContent.canAddLine(nextLine)) {
                    singleBatchContent.sortLines();
                    tempFilesPaths.add(TempFileManager.writeToTempFile(singleBatchContent.toString()));
                    singleBatchContent.reset();
                }
                singleBatchContent.addLine(nextLine);
            }

            if (!singleBatchContent.isEmpty()) {
                singleBatchContent.sortLines();
                tempFilesPaths.add(TempFileManager.writeToTempFile(singleBatchContent.toString()));
            }
        } catch (SizeLimitExceededException e) {
            throw new RuntimeException("Adding next line to buffer would overload it. However such case is guarded by canAddLine - check this method for bugs");
        }
        return tempFilesPaths;
    }
}
