package server.exceptions;

public class BrokenChainException extends Exception {
    private String detail;

    public BrokenChainException(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return detail;
    }
}
