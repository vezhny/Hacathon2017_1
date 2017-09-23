package server.exceptions;

public class ServerErrorException extends Exception {
    private String detail;

    public ServerErrorException(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return detail;
    }
}
