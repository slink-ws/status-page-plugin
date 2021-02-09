package ws.slink.statuspage.service;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.plugin.webresource.impl.support.Tuple;
import com.google.common.cache.*;
import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.StatusPage;
import ws.slink.statuspage.error.ServiceCallException;
import ws.slink.statuspage.model.*;
import ws.slink.statuspage.type.ComponentStatus;
import ws.slink.statuspage.type.IncidentSeverity;
import ws.slink.statuspage.type.IncidentStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatuspageService {

    private static class StatuspageServiceSingleton {
        private static final StatuspageService INSTANCE = new StatuspageService();
    }

    public static StatuspageService instance() {
        return StatuspageService.StatuspageServiceSingleton.INSTANCE;
    }

    private LoadingCache<IssueIncident, Optional<Tuple<Boolean, Incident>>> incidentCache;
    private final Map<String, StatusPage> statusPages = new ConcurrentHashMap<>();

    private StatuspageService() {

        CacheLoader<IssueIncident, Optional<Tuple<Boolean, Incident>>> loader
                = new CacheLoader<IssueIncident, Optional<Tuple<Boolean, Incident>>>() {
            @Override
            public Optional<Tuple<Boolean, Incident>> load(final IssueIncident key) {
                Optional<StatusPage> statusPage = StatuspageService.instance().get(key.projectKey());
                if (statusPage.isPresent()) {
                    try {
                        Optional<Incident> incident = statusPage.get().getIncident(key.pageId(), key.incidentId(), true);
                        if (incident.isPresent()) {
                            System.out.println("------> loaded incident from statuspage: " + key);
                            return Optional.of(new Tuple<>(true, incident.get()));
                        } else {
                            System.out.println("------> could not find incident on statuspage: " + key);
                            return Optional.of(new Tuple<>(false, null));
                        }
                    } catch (ServiceCallException e) {
                        System.out.println("------> error querying statuspage: " + key);
                        return Optional.of(new Tuple<>(false, null));
                    }
                }
                return Optional.empty();
            }
        };

        RemovalListener<IssueIncident, Optional<Tuple<Boolean, Incident>>> listener = n -> {
            if (null != n.getKey() && n.wasEvicted()) {
                System.out.println("-----> removed " + n.getKey() + " (" + n.getCause().name() + ")");
            }
        };

        incidentCache = CacheBuilder.newBuilder()
                .weakKeys()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .removalListener(listener)
                .build(loader)
        ;

//        System.out.println("---- created statuspage service");
    }

    public void clear() {
//        System.out.println("--- clear statusPage store");
        statusPages.clear();
        incidentCache.cleanUp();
    }

    public void init(String projectKey, String apiKey) {
        String apiKeyStr = apiKey;
        if (StringUtils.isNotBlank(apiKeyStr)) {
            if (apiKeyStr.length() > 10)
                apiKeyStr = apiKeyStr.substring(0,10) + "...";
        }
        System.out.println("--- init statusPage for " + projectKey + " with ApiKey " + apiKeyStr);
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

    public List<IssueIncidentImpact> incidentImpactList() {
        return Arrays.asList(IncidentSeverity.values())
                .stream()
                .sorted(Comparator.comparing(a -> Integer.valueOf(a.id())))
                .map(IssueIncidentImpact::of)
                .filter(s -> StringUtils.isNotBlank(s.title))
                .collect(Collectors.toList())
                ;
    }

    public List<IssueIncidentStatus> incidentStatusList(boolean sheduledIncident) {
        Stream<IncidentStatus> is = Arrays.asList(IncidentStatus.values()).stream();
        if (sheduledIncident)
            is = is.filter(s -> s.id() >= IncidentStatus.SCHEDULED.id());
        else
            is = is.filter(s -> s.id() < IncidentStatus.SCHEDULED.id());
        return is
                .sorted(Comparator.comparing(a -> Integer.valueOf(a.id())))
                .map(IssueIncidentStatus::of)
                .filter(s -> StringUtils.isNotBlank(s.title))
                .collect(Collectors.toList())
                ;
    }

    public List<Component> nonAffectedComponentsList(List<Component> allComponents, Incident incident) {
        if (null == allComponents || null == incident) {
            return Collections.emptyList();
        } else {
            List<Component> result = new ArrayList<>(allComponents);
            result.removeAll(incident.components());
            return result;
        }
    }

    public Optional<Incident> getIncident(Issue issue) {
        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        Object cf = issue.getCustomFieldValue(customField);
        if (null != cf && cf instanceof IssueIncident) {
            IssueIncident issueIncident = (IssueIncident) cf;
            return getIncident(issueIncident);
        }
        return Optional.empty();
    }
    public Optional<Incident> getIncident(IssueIncident issueIncident) {
        Optional<Tuple<Boolean, Incident>> cachedTuple = null;
        try {
            synchronized (this) {
                cachedTuple = incidentCache.get(issueIncident);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (cachedTuple.isPresent()) {
            return Optional.ofNullable(cachedTuple.get().getLast());
        }
        return Optional.empty();
    }

}
