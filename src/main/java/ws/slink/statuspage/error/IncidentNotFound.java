package ws.slink.statuspage.error;

public class IncidentNotFound extends RuntimeException {
    public IncidentNotFound() {
        super();
    }
    public IncidentNotFound(String message) {
        super(message);
    }
}
