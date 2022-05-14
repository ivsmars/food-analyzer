package bg.sofia.uni.fmi.mjt.logger;

import bg.sofia.uni.fmi.mjt.exceptions.LogException;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoggerTest {
    @Test
    public void testLog() throws InterruptedException, LogException {
        int threadCount = 20;
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        Logger logger = new Logger(bw);

        Thread[] threads = new Thread[threadCount];
        for(int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> logger.log(Level.INFO,new RuntimeException()));
            threads[i].start();
        }
        for(int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        logger.close();
        assertEquals(logger.getLogWritesCount(),threadCount);
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionWriterIsNull() {
        BufferedWriter bw = null;
        assertThrows(IllegalArgumentException.class, () -> new Logger(bw));
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionPathIsNull() {
        Path path = null;
        assertThrows(IllegalArgumentException.class, () -> new Logger(path));
    }

    @Test
    public void testLogThrowsIllegalArgumentExceptionNullArguments() {
        Logger logger = new Logger(new BufferedWriter(new StringWriter()));
        assertThrows(IllegalArgumentException.class, () -> logger.log(null, new RuntimeException()));
        assertThrows(IllegalArgumentException.class, () -> logger.log(Level.WARN, null));
    }

}
