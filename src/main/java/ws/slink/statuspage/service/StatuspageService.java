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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatuspageService {

    private static final short INCIDENT_CACHE_TTL_SECONDS  = 120;
    private static final short COMPONENT_CACHE_TTL_SECONDS = 300;

    private class ComponentsKey implements Comparable<ComponentsKey> {
        String projectKey;
        String pageId;
        ComponentsKey project(String value) {
            this.projectKey = value;
            return this;
        }
        ComponentsKey page(String value) {
            this.pageId = value;
            return this;
        }
        @Override public int hashCode() {
            return (projectKey + "#" + pageId).hashCode();
        }
        @Override public boolean equals(Object other) {
            if (this == other)
                return true;
            if (null == other)
                return false;
            if (this.getClass() != other.getClass())
                return false;
            ComponentsKey ii = (ComponentsKey)other;
            if (StringUtils.isBlank(this.projectKey) && !(StringUtils.isBlank(ii.projectKey)))
                return false;
            if (!this.projectKey.equals(ii.projectKey))
                return false;
            if (StringUtils.isBlank(this.pageId) && !(StringUtils.isBlank(ii.pageId)))
                return false;
            if (!this.pageId.equals(ii.pageId))
                return false;
            return true;
        }

        @Override
        public int compareTo(ComponentsKey o) {
            return (projectKey.equals(o.projectKey))
                 ? pageId.compareTo(o.pageId)
                 : projectKey.compareTo(o.projectKey);
        }

        @Override
        public String toString() {
            return "#" + pageId;
        }
    }

    private static class StatuspageServiceSingleton {
        private static final StatuspageService INSTANCE = new StatuspageService();
    }
    public static StatuspageService instance() {
        return StatuspageService.StatuspageServiceSingleton.INSTANCE;
    }

    private final LoadingCache<IssueIncident, Optional<Tuple<Boolean, Incident>>> incidentCache;
    private final LoadingCache<ComponentsKey, Optional<List<Component>>> componentCache;

    private final Map<String, StatusPage> statusPages = new ConcurrentHashMap<>();

    private StatuspageService() {
        incidentCache  = initIncidentCache();
        componentCache = initComponentCache();
    }
    private LoadingCache<IssueIncident, Optional<Tuple<Boolean, Incident>>> initIncidentCache() {
        CacheLoader<IssueIncident, Optional<Tuple<Boolean, Incident>>> loader
            = new CacheLoader<IssueIncident, Optional<Tuple<Boolean, Incident>>>() {
            @Override
            public Optional<Tuple<Boolean, Incident>> load(final IssueIncident key) {
                System.out.println("--> could not find cached incident for key [ " + key + " ] [ " +
                        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
                Optional<StatusPage> statusPage = StatuspageService.instance().get(key.projectKey());
                if (statusPage.isPresent()) {
                    try {
                        Optional<Incident> incident = statusPage.get().getIncident(key.pageId(), key.incidentId(), true);
                        if (null != incident && incident.isPresent()) {
                            System.out.println("--> loaded incident from statuspage for key [ " + key + " ]  [ " +
                                DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
                            return Optional.of(new Tuple<>(true, incident.get()));
                        } else {
                            System.out.println("--> could not find incident on statuspage for key [  " + key + " ] " + Thread.currentThread().getName());
                            return Optional.of(new Tuple<>(false, null));
                        }
                    } catch (ServiceCallException e) {
                        System.out.println("--> error querying statuspage for key [ " + key + " ] " + Thread.currentThread().getName());
                        return Optional.of(new Tuple<>(false, null));
                    } catch (Exception e) {
                        System.out.println("--> unexpected exception querying statuspage for key [ " + key + " ]: " + e.getClass().getSimpleName() + " " + e.getMessage());
                    }
                }
                return Optional.empty();
            }
        };

        RemovalListener<IssueIncident, Optional<Tuple<Boolean, Incident>>> listener = n -> {
            if (null != n.getKey() && n.wasEvicted()) {
                System.out.println("--> removed " + n.getKey() + " (" + n.getCause().name() + ") [ " +
                        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
            }
        };

        return CacheBuilder.newBuilder()
          //.weakKeys()
            .maximumSize(100)
            .expireAfterWrite(INCIDENT_CACHE_TTL_SECONDS, TimeUnit.SECONDS)
            .removalListener(listener)
            .build(loader)
        ;
    }
    private LoadingCache<ComponentsKey, Optional<List<Component>>> initComponentCache() {
        CacheLoader<ComponentsKey, Optional<List<Component>>> loader
            = new CacheLoader<ComponentsKey, Optional<List<Component>>>() {
            @Override
            public Optional<List<Component>> load(final ComponentsKey key) {
                System.out.println("--> could not find cached components for page #" + key + " [ " +
                    DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
                Optional<StatusPage> statusPage = StatuspageService.instance().get(key.projectKey);
                if (statusPage.isPresent()) {
                    try {
                        List<Component> components = statusPage.get().components(key.pageId, true);
                        if (null != components && !components.isEmpty()) {
                            System.out.println("--> loaded components from statuspage for key [ " + key + " ]  [ " +
                                DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
                            return Optional.of(components);
                        } else {
                            System.out.println("--> could not find incident on statuspage for key [  " + key + " ] " + Thread.currentThread().getName());
                            return Optional.of(Collections.EMPTY_LIST);
                        }
                    } catch (ServiceCallException e) {
                        System.out.println("--> error querying statuspage for key [ " + key + " ] " + Thread.currentThread().getName());
                        return Optional.of(Collections.EMPTY_LIST);
                    } catch (Exception e) {
                        System.out.println("--> unexpected exception querying statuspage for key [ " + key + " ]: " + e.getClass().getSimpleName() + " " + e.getMessage());
                    }
                }
                return Optional.empty();
            }
        };

        RemovalListener<ComponentsKey, Optional<List<Component>>> listener = n -> {
            if (null != n.getKey() && n.wasEvicted()) {
                System.out.println("--> removed components " + n.getKey() + " (" + n.getCause().name() + ") [ " +
                    DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
            }
        };

        return CacheBuilder.newBuilder()
            //.weakKeys()
            .maximumSize(100)
            .expireAfterWrite(COMPONENT_CACHE_TTL_SECONDS, TimeUnit.SECONDS)
            .removalListener(listener)
            .build(loader)
        ;
    }

    public void invalidateCache(IssueIncident issueIncident) {
        System.out.println("--> invalidating incident cache for " + issueIncident);
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

    // - should be synchronized to prevent multiple calls to statuspage service;
    // - if multiple calls are made for the same incident, all successive calls
    //   will get data from cache after lock is released;
    // - if multiple calls are made for different incidents, anyway threads should wait
    //   for delay interval before making calls to service (to follow rate limiting rules)
    //   so while one thread is waiting for delay to pass, other threads will block
//    synchronized
    public Optional<Incident> getIncident(IssueIncident issueIncident) {
        Optional<Tuple<Boolean, Incident>> cachedTuple = Optional.empty();
        try {
            synchronized (this) {
                cachedTuple = incidentCache.get(issueIncident);
            }
        } catch (ExecutionException e) {
            System.out.println("cache access error: " + e.getMessage());
        }
        if (cachedTuple.isPresent()) {
            System.out.println("  --> got cached record for key [ " + issueIncident + " ] [ " +
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
            return Optional.ofNullable(cachedTuple.get().getLast());
        }
        return Optional.empty();
    }
    public Optional<Incident> updateIncident(RestResource.IncidentUpdateParams updateParams, String message) {

        Optional<StatusPage> statusPage = StatuspageService.instance().get(updateParams.getProject());

        if (!statusPage.isPresent())
            throw new StatusPageObjectNotFound("could not find StatusPage object for project " + updateParams.getProject());

        Optional<Incident> incident = getIncident(updateParams.getProject(), updateParams.getPage(), updateParams.getIncident());
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

    public List<Component> getComponents(String projectKey, String pageId) {
        Optional<List<Component>> cachedComponents = Optional.empty();
        try {
            synchronized (this) {
                cachedComponents = componentCache.get(new ComponentsKey().project(projectKey).page(pageId));
            }
        } catch (ExecutionException e) {
            System.out.println("cache access error: " + e.getMessage());
        }
        if (cachedComponents.isPresent()) {
            System.out.println("  --> got cached components for page #" + pageId + " [ " +
                    DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(Instant.now().atZone(ZoneId.of("UTC"))) + " ] " + Thread.currentThread().getName());
            return cachedComponents.get();
        }
        return Collections.emptyList();
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
