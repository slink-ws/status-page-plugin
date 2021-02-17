package ws.slink.statuspage.service;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.plugin.webresource.impl.support.Tuple;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import ws.slink.statuspage.StatusPage;
import ws.slink.statuspage.error.IncidentNotFound;
import ws.slink.statuspage.error.ServiceCallException;
import ws.slink.statuspage.error.StatusPageObjectNotFound;
import ws.slink.statuspage.model.*;
import ws.slink.statuspage.servlet.RestResource;
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

    private static final short CACHE_TTL_SECONDS = 30;

    private static class StatuspageServiceSingleton {
        private static final StatuspageService INSTANCE = new StatuspageService();
    }
    public static StatuspageService instance() {
        return StatuspageService.StatuspageServiceSingleton.INSTANCE;
    }

    private final LoadingCache<IssueIncident, Optional<Tuple<Boolean, Incident>>> incidentCache;
    private final Map<String, StatusPage> statusPages = new ConcurrentHashMap<>();

    private StatuspageService() {

        CacheLoader<IssueIncident, Optional<Tuple<Boolean, Incident>>> loader
                = new CacheLoader<IssueIncident, Optional<Tuple<Boolean, Incident>>>() {
            @Override
            public Optional<Tuple<Boolean, Incident>> load(final IssueIncident key) {
                System.out.println("---> could not find cached incident for key [ " + key + " ]");
                Optional<StatusPage> statusPage = StatuspageService.instance().get(key.projectKey());
                if (statusPage.isPresent()) {
                    try {
                        Optional<Incident> incident = statusPage.get().getIncident(key.pageId(), key.incidentId(), true);
                        if (incident.isPresent()) {
                            System.out.println("------> loaded incident from statuspage for key [ " + key + " ]");
                            return Optional.of(new Tuple<>(true, incident.get()));
                        } else {
                            System.out.println("------> could not find incident on statuspage for key [  " + key + " ]");
                            return Optional.of(new Tuple<>(false, null));
                        }
                    } catch (ServiceCallException e) {
                        System.out.println("------> error querying statuspage for key [ " + key + " ]");
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
//            .weakKeys()
            .maximumSize(100)
            .expireAfterWrite(CACHE_TTL_SECONDS, TimeUnit.SECONDS)
            .removalListener(listener)
            .build(loader)
        ;

//        System.out.println("---- created statuspage service");
    }

    public void invalidateCache(IssueIncident issueIncident) {
        System.out.println("------> invalidating incident cache for " + issueIncident);
        incidentCache.invalidate(issueIncident);
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

    public Optional<Incident> getIncident(String projectKey, String pageId, String incidentId) {
        return getIncident(
            new IssueIncident()
                .projectKey(projectKey)
                .pageId(pageId)
                .incidentId(incidentId)
            )
        ;
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
        Optional<Tuple<Boolean, Incident>> cachedTuple = Optional.empty();
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

    public Optional<Incident> updateIncident(RestResource.IncidentUpdateParams updateParams, String message) {

        Optional<StatusPage> statusPage = StatuspageService.instance().get(updateParams.getProject());

        if (!statusPage.isPresent())
            throw new StatusPageObjectNotFound("could not find StatusPage object for project " + updateParams.getProject());

        Optional<Incident> incident = StatuspageService.instance().getIncident(updateParams.getProject(), updateParams.getPage(), updateParams.getIncident());
        if (!incident.isPresent())
            throw new IncidentNotFound("could not find incident " + updateParams.getIncident() + " for page " + updateParams.getPage());

        incident.get().status(IncidentStatus.of(updateParams.getStatus()));
        incident.get().impact(IncidentSeverity.of(updateParams.getImpact()));
        incident.get().components(getAffectedComponents(statusPage.get(), updateParams));
        Optional<Incident> updated = statusPage.get().updateIncident(incident.get(), message);
        if (!(updated.isPresent()
          && updated.get().id().equals(incident.get().id())
          && updated.get().impact() == incident.get().impact()
          && updated.get().status() == incident.get().status()
        ))
            throw new ServiceCallException("incident update error", HttpStatus.SC_EXPECTATION_FAILED);

        StatuspageService.instance().invalidateCache(
            new IssueIncident()
                .pageId(updated.get().pageId())
                .incidentId(updated.get().id())
                .projectKey(updateParams.getProject())
        );

        return updated;
    }

    private List<Component> getAffectedComponents(StatusPage statusPage, RestResource.IncidentUpdateParams incidentUpdateParams) {

        Map<String, Component> pageComponents =
            statusPage
                .components(incidentUpdateParams.getPage(), true)
                .stream()
                .collect(Collectors.toMap(Component::id, c -> c));

        List<Component> result = new ArrayList<>();

        incidentUpdateParams.getComponents().keySet().stream().forEach(status -> {
            ComponentStatus componentStatus = ComponentStatus.of(status);
            incidentUpdateParams.getComponents().get(status).stream().forEach(componentId -> {
                Component component = pageComponents.get(componentId);
                if (null != component) {
                    component.status(componentStatus);
                }
                result.add(component);
            });
        });

        return result;
    }

}
