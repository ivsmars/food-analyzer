package bg.sofia.uni.fmi.mjt.exceptions;

public class CacheException extends Exception {
    public CacheException(String msg) {
        super(msg);
    }

    public CacheException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
