package server.exceptions;

public class InvalidTimestampException extends Exception {
    String detail;

    public InvalidTimestampException(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return detail;
    }
}
