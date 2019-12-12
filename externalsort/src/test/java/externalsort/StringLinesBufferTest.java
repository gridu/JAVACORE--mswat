package externalsort;

import externalsort.StringLinesBuffer;
import org.junit.Before;
import org.junit.Test;

import javax.naming.SizeLimitExceededException;
import java.util.List;

import static org.junit.Assert.*;

public class StringLinesBufferTest {
    private final int TEST_BUFFER_LENGTH = 5;
    private StringLinesBuffer buffer;

    @Before
    public void setUpBuffer() {
        this.buffer = new StringLinesBuffer(TEST_BUFFER_LENGTH);
    }

    @Test(expected = SizeLimitExceededException.class)
    public void shouldThrowExceptionWhenBatchSizeIsExceededDuringLineAddition() throws SizeLimitExceededException {
        var tooLongLine = "aaaaaa";

        buffer.addLine(tooLongLine);
    }

    @Test
    public void shouldBeAbleToAddLineAfterReset() throws SizeLimitExceededException {
        var expectedLine = "bbbb";

        buffer.addLine("aaaa");
        buffer.reset();
        buffer.addLine(expectedLine);

        assertEquals(expectedLine, buffer.toString());
    }

    @Test
    public void shouldBeEmptyAfterReset() throws SizeLimitExceededException {

        buffer.addLine("aaaa");
        buffer.reset();

        assertTrue(buffer.isEmpty());
    }

    @Test
    public void shouldJoinMultipleLinesWithNewline() throws SizeLimitExceededException {
        String testLine = "a";
        String expectedOutput = "a\na\na";

        buffer.addLine(testLine);
        buffer.addLine(testLine);
        buffer.addLine(testLine);

        assertEquals(expectedOutput, buffer.toString());
    }

    @Test
    public void shouldSortLinesItIsContaining() throws SizeLimitExceededException {
        List<String> lines = List.of("b", "a", "d");
        String expectedOutput = "a\nb\nd";

        for (var line : lines) {
            buffer.addLine(line);
        }
        buffer.sortLines();

        assertEquals(expectedOutput, buffer.toString());
    }

    @Test
    public void shouldTakeNewlinesIntoAccountWhenMeasuringBufferLengthWithMultipleLines() throws SizeLimitExceededException {
        String testLine = "a";

        buffer.addLine(testLine);
        buffer.addLine(testLine);
        buffer.addLine(testLine);

        assertFalse(buffer.canAddLine(testLine));
    }

    @Test
    public void shouldNotTakeNewlinesIntoAccountWhenMeasuringBufferLengthWithSingleLine() throws SizeLimitExceededException {
        String lineWithLengthEqualToBufferLength = "a".repeat(TEST_BUFFER_LENGTH);

        buffer.addLine(lineWithLengthEqualToBufferLength);

        assertFalse(buffer.canAddLine("a"));
    }

}