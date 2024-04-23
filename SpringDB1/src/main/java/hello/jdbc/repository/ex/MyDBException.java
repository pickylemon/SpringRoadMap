package hello.jdbc.repository.ex;

public class MyDBException extends RuntimeException{
    public MyDBException() {
        super();
    }

    public MyDBException(Throwable cause) {
        super(cause);
    }

    public MyDBException(String message) {
        super(message);
    }

    public MyDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
