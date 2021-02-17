package ws.slink.statuspage.error;

public class StatusPageObjectNotFound extends RuntimeException {
    public StatusPageObjectNotFound() {
        super();
    }
    public StatusPageObjectNotFound(String message) {
        super(message);
    }
}
