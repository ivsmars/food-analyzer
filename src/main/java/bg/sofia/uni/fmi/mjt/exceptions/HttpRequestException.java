package bg.sofia.uni.fmi.mjt.exceptions;

public class HttpRequestException extends RuntimeException {
    public HttpRequestException(String msg) {
        super(msg);
    }

    public HttpRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
