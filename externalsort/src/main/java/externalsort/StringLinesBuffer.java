package externalsort;

import javax.naming.SizeLimitExceededException;
import java.util.ArrayList;
import java.util.Collections;


public class StringLinesBuffer {
    private final ArrayList<String> currentLines = new ArrayList<>();
    private final long bufferLength;
    private long currentLength = 0;
    private static final int NEW_LINE_LENGTH = 1;

    /**
     * Helper class for holding batch of string lines and performing simple operations om them.
     * @param bufferLength Maximal size of the buffer.
     */
    public StringLinesBuffer(long bufferLength) {
        this.bufferLength = bufferLength;
    }

    /**
     * @param line String that will be extended with newline sign in order to create a line inside a file.
     * @throws SizeLimitExceededException thrown when adding next line would overload buffer.
     */
    public void addLine(String line) throws SizeLimitExceededException{
        if (line.length() + currentLength > bufferLength) {
            throw new SizeLimitExceededException("Can't add that line as size of single batch would be exceeded");
        }
        else {
            currentLines.add(line);
            currentLength = currentLength == 0 ? line.length() : currentLength + line.length() + NEW_LINE_LENGTH;
        }
    }

    /**
     * Checks whether adding next line will overload the buffer (including newline sign that will be automatically added)
     */
    public boolean canAddLine(String line) {
        return currentLength == 0 ? line.length() <= bufferLength : currentLength + line.length() + NEW_LINE_LENGTH <= bufferLength;
    }

    public void reset() {
        currentLength = 0;
        currentLines.clear();
    }

    public void sortLines() {
        Collections.sort(currentLines);
    }

    public boolean isEmpty() {
        return currentLines.isEmpty();
    }

    @Override
    public String toString() {
        return String.join("\n", currentLines);
    }
}
