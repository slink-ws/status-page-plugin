package ws.slink.statuspage.customfield;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.FreeTextCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.jql.util.SimpleIndexValueConverter;
import com.atlassian.jira.jql.validator.ExactTextCustomFieldValidator;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class IncidentCustomFieldSearcher implements CustomFieldSearcher {

    private final FieldVisibilityManager fieldVisibilityManager;
    private JqlOperandResolver jqlOperandResolver;
    private CustomFieldInputHelper customFieldInputHelper;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    private volatile CustomFieldSearcherModuleDescriptor moduleDescriptor;

    public IncidentCustomFieldSearcher(/*@ComponentImport final JqlOperandResolver jqlOperandResolver, @ComponentImport final CustomFieldInputHelper customFieldInputHelper*/) {
        System.out.println("----> IncidentCustomFieldSearcher.create enter");
        this.fieldVisibilityManager = ComponentAccessor.getComponentOfType(FieldVisibilityManager.class);
        this.jqlOperandResolver     = ComponentAccessor.getComponentOfType(JqlOperandResolver.class); //jqlOperandResolver;
        this.customFieldInputHelper = ComponentAccessor.getComponentOfType(CustomFieldInputHelper.class); //customFieldInputHelper
        System.out.println("----> IncidentCustomFieldSearcher.create exit");
    }

    public void init(CustomField customField) {
        System.out.println("----> IncidentCustomFieldSearcher.init: " + customField);
        final ClauseNames clauseNames = customField.getClauseNames();
        final FieldIndexer indexer = new IncidentCustomFieldIndexer(fieldVisibilityManager, customField);

        final IndexValueConverter indexValueConverter = new SimpleIndexValueConverter(false);
        final CustomFieldValueProvider customFieldValueProvider = new SingleValueCustomFieldValueProvider();

        this.searcherInformation = new CustomFieldSearcherInformation(customField.getId(), customField.getNameKey(), Collections.singletonList(indexer), new AtomicReference(customField));
        this.searchRenderer = new CustomFieldRenderer(clauseNames, getDescriptor(), customField, customFieldValueProvider, fieldVisibilityManager);

        this.searchInputTransformer = new FreeTextCustomFieldSearchInputTransformer(customField, clauseNames, searcherInformation.getId(), customFieldInputHelper);  //new MultipleValuesCustomFieldSearchInputTransformer(field, names, searcherInformation.getId(), customFieldInputHelper);

        this.customFieldSearcherClauseHandler = new SimpleCustomFieldSearcherClauseHandler(
            new ExactTextCustomFieldValidator(),
            new ActualValueCustomFieldClauseQueryFactory(customField.getId(), jqlOperandResolver, indexValueConverter, false),
            OperatorClasses.EMPTY_OPERATORS,
            JiraDataTypes.TEXT
        );
    }
    public void init(CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor) {
        System.out.println("----> IncidentCustomFieldSearcher.init: " + customFieldSearcherModuleDescriptor);
        this.moduleDescriptor = customFieldSearcherModuleDescriptor;
    }

    public SearcherInformation<CustomField> getSearchInformation() {
        if (searcherInformation == null) {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }
    public SearchInputTransformer getSearchInputTransformer() {
        if (searchInputTransformer == null) {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        }
        return searchInputTransformer;
    }
    public SearchRenderer getSearchRenderer() {
        if (searchRenderer == null) {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        }
        return searchRenderer;
    }
    public CustomFieldSearcherModuleDescriptor getDescriptor() {
//        return null;
        if (moduleDescriptor == null) {
            throw new IllegalStateException("Attempt to retrieve moduleDescriptor off uninitialised custom field searcher.");
        }
        return moduleDescriptor;
    }
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        if (customFieldSearcherClauseHandler == null) {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        }
        return customFieldSearcherClauseHandler;
    }

}