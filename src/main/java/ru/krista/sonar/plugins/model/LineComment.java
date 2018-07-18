package ru.krista.sonar.plugins.model;

import net.sf.json.JSONObject;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;


/**
 * Строковый коммент.
 */
public class LineComment {

    private final Severity severity;
    private final String message;
    private final String line;
    private final String filePath;
    private final String issueKey;

    public LineComment(PostJobIssue issue, String relativePath, String message) {
        this.line = issue.line() == null ? null : String.format("n%d", issue.line());
        this.severity = issue.severity();
        this.filePath = relativePath;
        this.message = message;
        this.issueKey = issue.key();
    }

    //
    public JSONObject toRequestBody() {
        JSONObject result = new JSONObject();
        result.element("message", message).element("line", line).element("file", filePath);
        return result;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getLine() {
        return line;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getMessage() {
        return message;
    }
}
