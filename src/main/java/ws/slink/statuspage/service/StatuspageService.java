package ws.slink.statuspage.service;

import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.StatusPage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StatuspageService {

    private static class StatuspageServiceSingleton {
        private static final StatuspageService INSTANCE = new StatuspageService();
    }
    public static StatuspageService instance () {
        return StatuspageService.StatuspageServiceSingleton.INSTANCE;
    }

    private final Map<String, StatusPage> statusPages = new ConcurrentHashMap<>();

    private StatuspageService() {
        System.out.println("---- created statuspage service");
    }

    public void clear() {
        System.out.println("--- clear statusPage store");
        statusPages.clear();
    }
    public void init(String projectKey, String apiKey) {
        System.out.println("--- init statusPage for " + projectKey + " with ApiKey " + apiKey);
        if (StringUtils.isBlank(apiKey))
            return;
        statusPages.remove(projectKey);
        statusPages.put(projectKey, new StatusPage.Builder()
            .apiKey(apiKey)
            .bridgeErrors(true)
            .rateLimit(true)
            .rateLimitDelay(1000)
            .build())
        ;
    }
    public Optional<StatusPage> get(String projectKey) {
        return Optional.ofNullable(statusPages.get(projectKey));
    }

}
