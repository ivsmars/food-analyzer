package bg.sofia.uni.fmi.mjt.requests;

import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class RequestTest {

    @Test
    public void testBuildInvalidRequest() {
        assertThrows(InvalidRequestException.class,() -> Request.newRequestBuilder().setPageNumber(1).build());
        assertThrows(InvalidRequestException.class,() -> Request.newRequestBuilder().build());
        assertThrows(InvalidRequestException.class, () -> Request.newRequestBuilder().setKeywords(List.of()).build());
    }
}
