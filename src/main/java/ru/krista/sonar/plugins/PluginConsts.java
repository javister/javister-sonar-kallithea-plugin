package ru.krista.sonar.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.api.batch.rule.Severity;
import ru.krista.sonar.plugins.model.PrintedComments;

/**
 * Константы плагина.
 */
public final class PluginConsts {

    public static final String PLUGIN_CATEGORY = "Sonar Kallithea plugin";
    public static final String PLUGIN_PROP_NAME_PREFIX = "sonar.krista.kallithea.";
    public static final String PLUGIN_ENABLED_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "enanled";
    public static final String PLUGIN_CLEAN_PREV_COMMENTS_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "clean.prev.comments";
    public static final String PLUGIN_PRINTED_COMMENTS_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "printed.comments";
    public static final String PLUGIN_REPO_PREFIX_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "repo-prefix";
    public static final String PLUGIN_KALLITHEA_API_URL_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "kalithea-api.url";
    public static final String PLUGIN_KALLITHEA_USER_API_KEY_NAME = PLUGIN_PROP_NAME_PREFIX
            + "kalithea-api.user.api-key";
    public static final String PLUGIN_REPO_PATH_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "repo-path";
    public static final String PLUGIN_PULL_REQUEST_ID_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "pull-request.id";
    public static final String PLUGIN_COMMENT_USER_NAME_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "comment.user.name";
    public static final String PLUGIN_ISSUE_THRESHOLD_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "issue.threshold";
    public static final String PLUGIN_STATUS_THRESHOLD_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "status.threshold";
    public static final String PLUGIN_CONNECTION_TIMEOUT_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "connection.timeout";
    public static final String PLUGIN_NEED_CHANGE_STATUS_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "change.status";
    public static final String PLUGIN_ONLY_NEW_ISSUE_PROP_NAME = PLUGIN_PROP_NAME_PREFIX + "only.new.issue";
    public static final String PLUGIN_SEND_STATUS_MAIL_NAME = PLUGIN_PROP_NAME_PREFIX + "send.mail";
    public static final String SONAR_SERVER_URL_PROP_NAME = "sonar.core.serverBaseURL";
    public static final String SONAR_RUNNER_SONARQUBE_URL_PROP_NAME = "sonar.host.url";


    static final List<String> PRINTED_COMMENTS_LIST = Arrays.stream(PrintedComments.values())
            .map(PrintedComments::name).collect(Collectors.toList());

    public static final String SEVERITY_NONE = "NONE";
    static final List<String> SEVERITY_LIST = Arrays.stream(Severity.values()).map(Severity::name).collect(
            Collectors.toList());

    private static List<String> severityListWithNone;

    /**
     * Приватный конструктор финального класса.
     */
    private PluginConsts() {
        //
    }

    public static List<String> severityListWithNone() {
        if (severityListWithNone == null) {
            severityListWithNone = new ArrayList<>(SEVERITY_LIST);
            severityListWithNone.add(0, SEVERITY_NONE);
        }
        return severityListWithNone;
    }

}
