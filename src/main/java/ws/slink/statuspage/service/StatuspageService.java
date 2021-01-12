package ws.slink.statuspage.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatuspageService {

    private static class StatuspageServiceSingleton {
        private static final StatuspageService INSTANCE = new StatuspageService();
    }
    public static StatuspageService instance () {
        return StatuspageService.StatuspageServiceSingleton.INSTANCE;
    }


//    private final Map<String, StatusPage> statusPages = new ConcurrentHashMap<>();

    private StatuspageService() {
        System.out.println("---- created statuspage service");
    }

    public void clear() {
        System.out.println("--- clear statusPage store");
//        statusPages.clear();
    }
    public void init(String apiKey, String projectKey) {
        System.out.println("--- init statusPage for " + projectKey + " with ApiKey " + apiKey);
//        statusPages.put(projectKey, new StatusPage.Builder()
//            .apiKey(apiKey)
//            .bridgeErrors(true)
//            .rateLimit(true)
//            .rateLimitDelay(1000)
//            .build())
//        ;
    }

}
