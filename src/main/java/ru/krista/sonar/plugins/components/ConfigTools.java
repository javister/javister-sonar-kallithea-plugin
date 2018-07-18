package ru.krista.sonar.plugins.components;

import java.nio.charset.Charset;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Settings;
import ru.krista.sonar.plugins.PluginConsts;
import ru.krista.sonar.plugins.exceptions.ConfigException;
import ru.krista.sonar.plugins.model.PrintedComments;

/**
 * Конфигурация.
 * <p>
 * TODO: перейти на {@link org.sonar.api.config.Configuration} вместо {@link Settings}
 */
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class ConfigTools {

    // 10 минут
    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 600_000;
    public static final Charset UTF_8 = Charsets.UTF_8;

    private final Settings settings;
    private final AnalysisMode analysisMode;

    public ConfigTools(Settings settings, AnalysisMode analysisMode) {
        this.settings = settings;
        this.analysisMode = analysisMode;
    }

    public boolean check() {
        return analysisMode.isIssues() && settings.getBoolean(PluginConsts.PLUGIN_ENABLED_PROP_NAME);
    }

    public String getPullRequestId() {
        return settings.getString(PluginConsts.PLUGIN_PULL_REQUEST_ID_PROP_NAME);
    }

    public String getSonarUrl() {
        return settings.hasKey(PluginConsts.SONAR_RUNNER_SONARQUBE_URL_PROP_NAME)
                ? settings.getString(PluginConsts.SONAR_RUNNER_SONARQUBE_URL_PROP_NAME)
                : settings.getString(PluginConsts.SONAR_SERVER_URL_PROP_NAME);
    }

    public String getKallitheaApiUrl() {
        String result = settings.getString(PluginConsts.PLUGIN_KALLITHEA_API_URL_PROP_NAME);
        if (StringUtils.isBlank(result)) {
            throw new ConfigException("Parameter URL Kallithea API not set");
        }
        return result;
    }

    public String getUserName() {
        String userName = settings.getString(PluginConsts.PLUGIN_COMMENT_USER_NAME_PROP_NAME);
        if (StringUtils.isBlank(userName)) {
            throw new ConfigException("Parameter Kallithea user name not set");
        }
        return userName;
    }

    public String getKallitheaRootPath() {
        String result = settings.getString(PluginConsts.PLUGIN_REPO_PREFIX_PROP_NAME);
        if (StringUtils.isBlank(result)) {
            throw new ConfigException("Parameter Kallithea root URL not set");
        }
        return result;
    }

    public String getKallitheaApiKey() {
        return settings.getString(PluginConsts.PLUGIN_KALLITHEA_USER_API_KEY_NAME);
    }

    public String getPullRequestTargetRepository() {
        return settings.getString(PluginConsts.PLUGIN_REPO_PATH_PROP_NAME);
    }

    public String getStatusThreshold() {
        return settings.getString(PluginConsts.PLUGIN_STATUS_THRESHOLD_PROP_NAME);
    }

    public String getIssueThreshold() {
        return settings.getString(PluginConsts.PLUGIN_ISSUE_THRESHOLD_PROP_NAME);
    }

    public boolean needCleanPreviousComments() {
        return settings.getBoolean(PluginConsts.PLUGIN_CLEAN_PREV_COMMENTS_PROP_NAME);
    }

    public boolean onlyNewIssue() {
        return settings.getBoolean(PluginConsts.PLUGIN_ONLY_NEW_ISSUE_PROP_NAME);
    }

    public boolean needChangeStatus() {
        return settings.getBoolean(PluginConsts.PLUGIN_NEED_CHANGE_STATUS_PROP_NAME);
    }

    public boolean needSendStatusMail() {
        return settings.getBoolean(PluginConsts.PLUGIN_SEND_STATUS_MAIL_NAME);
    }

    public PrintedComments getPrintedComments() {
        return PrintedComments.valueOf(settings.getString(PluginConsts.PLUGIN_PRINTED_COMMENTS_PROP_NAME));
    }

    public Integer getConnectionTimeout() {
        try {
            return settings.getInt(PluginConsts.PLUGIN_CONNECTION_TIMEOUT_PROP_NAME);
        } catch (NumberFormatException e) {
            return DEFAULT_CONNECTION_TIMEOUT;
        }
    }
}
