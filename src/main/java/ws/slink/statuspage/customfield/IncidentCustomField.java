package ws.slink.statuspage.customfield;

import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ws.slink.statuspage.json.CustomExclusionStrategy;
import ws.slink.statuspage.model.IssueIncident;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Scanned
public class IncidentCustomField extends AbstractSingleFieldType<IssueIncident> {

    protected IncidentCustomField(
        @JiraImport CustomFieldValuePersister customFieldValuePersister,
        @JiraImport GenericConfigManager genericConfigManager) {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }

    @Nullable
    @Override
    protected Object getDbValueFromObject(IssueIncident s) {
        return getStringFromSingularObject(s);
    }

    @Nullable
    @Override
    protected IssueIncident getObjectFromDbValue(@Nonnull Object databaseValue) throws FieldValidationException {
        return getSingularObjectFromString((String)databaseValue);
    }

    @Override
    public String getStringFromSingularObject(IssueIncident singularObject) {
        if (singularObject == null)
            return null;
        else
            return getGsonObject().toJson(singularObject);
    }

    @Override
    public IssueIncident getSingularObjectFromString(String s) throws FieldValidationException {
        return getGsonObject().fromJson(s, IssueIncident.class);
    }

    @Override
    public boolean isUserInputRequiredForMove(CustomFieldParams relevantParams, FieldConfig config, Long targetProjectId, String targetIssueTypeId) {
        return false;
    }

    private Gson getGsonObject() {
        ExclusionStrategy strategy = new CustomExclusionStrategy();
        return new GsonBuilder()
            .addSerializationExclusionStrategy(strategy)
            .addDeserializationExclusionStrategy(strategy)
            .create()
        ;
    }
}
