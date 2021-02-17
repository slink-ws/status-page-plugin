package ws.slink.statuspage.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableFieldType;
import ws.slink.statuspage.model.IssueIncident;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class IncidentCustomFieldIndexer extends AbstractCustomFieldIndexer {

    private final static String LINKED = "LINKED";

    private final CustomField field;
    private final FieldType luceneFieldType;

    public IncidentCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField) {
        super(fieldVisibilityManager, notNull("field", customField));
        this.field = customField;
        this.luceneFieldType = new FieldType();
        this.luceneFieldType.setDocValuesType(DocValuesType.NONE);
        this.luceneFieldType.setIndexOptions(IndexOptions.DOCS);
        this.luceneFieldType.setStored(true);
        this.luceneFieldType.setTokenized(false);
        System.out.println("----> IncidentCustomFieldIndexer.create: " + customField);
    }

    @Override public void addDocumentFieldsSearchable(Document document, Issue issue) {
        System.out.println("----> IncidentCustomFieldIndexer.addDocumentFieldsSearchable: " + document + "; " + issue);
        addDocumentFields(document, issue, luceneFieldType);
    }
    @Override public void addDocumentFieldsNotSearchable(Document document, Issue issue) {
        System.out.println("----> IncidentCustomFieldIndexer.addDocumentFieldsNotSearchable: " + document + "; " + issue);
        addDocumentFields(document, issue, luceneFieldType);
    }

    private void addDocumentFields(final Document document, final Issue issue, final IndexableFieldType indexType) {
        System.out.println("----> IncidentCustomFieldIndexer.addDocumentFields: " + document + "; " + issue + "; " + indexType);
        Object value = field.getValue(issue);
        if (value != null) {
            IssueIncident issueIncident = (IssueIncident)value;
            document.add(new Field(getDocumentFieldId(), issueIncident.toJsonString(), indexType));
            document.add(new Field(getDocumentFieldId(), LINKED, indexType));
        }
    }

}
