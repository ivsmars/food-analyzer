package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ClientRequestTest {
    private static final String BARCODE_IMAGE = "./src/test/resources/barcodeImage.png";

    @Test
    public void testValidateRequestBadRequest() {
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("sasaffsa"));
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food"));
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food-report"));
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food-by-barcode"));
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food-by-barcode 18471328"));
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food-by-barcode --code="));
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food-by-barcode --img="));
    }

    @Test
    public void testValidateRequestValidRequest() {
        assertDoesNotThrow(() -> new ClientRequest("get-food cheddar cheese"));
        assertDoesNotThrow(() -> new ClientRequest("get-food-report 1321321"));
        assertDoesNotThrow(() -> new ClientRequest("get-food-by-barcode --code=12312312"));
    }

    @Test
    public void testModifiesBarcodeRequest() {
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        assertDoesNotThrow(() -> request.set(new ClientRequest("get-food-by-barcode --code=0000000000")));
        assertEquals(request.get().getRequest(), "get-food-by-barcode 0000000000");
    }

    @Test
    public void testGetGtinUpcFromImage() {
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        assertDoesNotThrow(() -> request.set(new ClientRequest(String.format("get-food-by-barcode --img=%s",BARCODE_IMAGE))));
        assertEquals(request.get().getRequest(), "get-food-by-barcode 8000500023976");
    }

    @Test
    public void testBarcodeRequestImageAndCodeIgnoresImage() {
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        assertDoesNotThrow(() -> request.set(new ClientRequest(String.format("get-food-by-barcode --img=%s --code=0000000000",BARCODE_IMAGE))));
        assertEquals(request.get().getRequest(), "get-food-by-barcode 0000000000");
    }

    @Test
    public void testBarcodeRequestBadParameters() {
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food-by-barcode --img=aisjd"));
        assertThrows(InvalidRequestException.class, () -> new ClientRequest("get-food-by-barcode --img=./barkod.png"));
    }


}
