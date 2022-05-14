package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.logger.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class ClientTest {

    @Test
    public void testClientConnectServerNotOpenThrowsIOException() {
        Logger logger = mock(Logger.class);
        final Client client = new Client(logger);
        assertThrows(IOException.class, client::connect);
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionLoggerIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Client(null));
    }

    @Test
    public void testThrowsIllegalArgumentExceptionRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Client(mock(Logger.class)).send(null));
    }


}
