package bg.sofia.uni.fmi.mjt.exceptions;

public class ServerException extends RuntimeException {
    public ServerException(Throwable cause) {
        super(cause);
    }

    public ServerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
