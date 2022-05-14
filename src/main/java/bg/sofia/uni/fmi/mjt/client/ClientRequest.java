package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.exceptions.BarcodeException;
import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ClientRequest {
    private String request;
    private final String[] requestTokens;
    private static final int COMMAND_INDEX = 0;

    private static final String BARCODE_REQUEST = "get-food-by-barcode";
    private static final String KEYWORDS_FOOD_REQUEST = "get-food";
    private static final String FOOD_REPORT_REQUEST = "get-food-report";

    private static final String IMG_PARAMETER = "--img=";
    private static final String CODE_PARAMETER = "--code=";

    private static final String SEPARATOR = " ";

    public ClientRequest(String request) throws InvalidRequestException {
        if (request == null || request.isBlank()) {
            throw new InvalidRequestException("Cannot process null or blank request");
        }
        this.requestTokens = request.split(SEPARATOR);
        validateRequest(request);
        this.request = request;
        if (isBarcodeRequest()) {
            modifyBarcodeRequest();
        }
    }

    private void validateRequest(String request) throws InvalidRequestException {
        if (requestTokens.length < 2 || (!requestTokens[COMMAND_INDEX].equals(KEYWORDS_FOOD_REQUEST)
                && !requestTokens[COMMAND_INDEX].equals(FOOD_REPORT_REQUEST)
                && !requestTokens[COMMAND_INDEX].equals(BARCODE_REQUEST))) {
            throw new InvalidRequestException(String.format("Invalid request \"%s\"", request));
        }

    }

    private String getGtinUpcFromImage(String path) throws BarcodeException {
        File file = new File(path);
        MultiFormatReader reader = new MultiFormatReader();
        Result result;
        try {
            result = reader.decodeWithState(new BinaryBitmap
                    (new HybridBinarizer
                            (new BufferedImageLuminanceSource
                                    (ImageIO.read(file)))));
        } catch (NotFoundException | IOException e) {
            throw new BarcodeException("A problem occurred while trying to process image file, please try again", e);
        }
        return result.getText();
    }

    private void modifyBarcodeRequest() throws InvalidRequestException {
        String res = null;
        String parameterSeparator = "=";
        for (String token : requestTokens) {
            if (token.startsWith(CODE_PARAMETER)) {
                if (token.split(parameterSeparator).length != 2) {
                    throw new InvalidRequestException("Missing code parameter");
                }
                this.request = String.format("%s %s", BARCODE_REQUEST, token.split(parameterSeparator)[1]);
                return;
            } else if (token.startsWith(IMG_PARAMETER)) {

                if (token.split(parameterSeparator).length != 2) {
                    throw new InvalidRequestException("Missing image parameter");
                }

                String gtinUpc;
                try {
                    gtinUpc = getGtinUpcFromImage(token.split(parameterSeparator)[1]);
                } catch (BarcodeException e) {
                    throw new InvalidRequestException("Invalid barcode image file", e);
                }

                res = String.format("%s %s", BARCODE_REQUEST, gtinUpc);
            }
        }
        if (res == null) {
            throw new InvalidRequestException("Invalid barcode request");
        }
        this.request = res;
    }

    private boolean isBarcodeRequest() {
        return requestTokens[COMMAND_INDEX].equals(BARCODE_REQUEST);
    }

    public String getRequest() {
        return request;
    }
}
