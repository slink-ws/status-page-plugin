package ws.slink.statuspage.service;

import org.apache.commons.lang3.StringUtils;
import org.ofbiz.core.util.UtilTimer;
import ws.slink.statuspage.StatusPage;
import ws.slink.statuspage.model.AffectedComponentStatus;
import ws.slink.statuspage.type.ComponentStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    public List<AffectedComponentStatus> componentStatusList() {
        return Arrays.asList(ComponentStatus.values())
            .stream()
            .sorted(Comparator.comparing(a -> Integer.valueOf(a.id())))
            .map(AffectedComponentStatus::of)
            .filter(s -> StringUtils.isNotBlank(s.title))
            .collect(Collectors.toList())
        ;
    }
}
