package bg.sofia.uni.fmi.mjt.exceptions;

public class InvalidRequestException extends Exception {
    public InvalidRequestException(String msg) {
        super(msg);
    }

    public InvalidRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
