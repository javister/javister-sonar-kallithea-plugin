package ru.krista.sonar.plugins.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.rule.RuleKey;
import ru.krista.sonar.plugins.components.ConfigTools;
import ru.krista.sonar.plugins.model.LineComment;
import ru.krista.sonar.plugins.model.PullRequest;
import ru.krista.sonar.plugins.model.PullRequestFile;

/**
 * Утилитный класс плагина.
 */
public final class PluginUtils {

    /**
     * Приватный конструктор финального класса.
     */
    private PluginUtils() {
        //
    }

    public static String checkPath(String path) {
        String result = path;
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }

    public static String prepareCommentUrl(ConfigTools configTools) {
        return String
                .format("%spullrequest/sonar/report/%s?user=%s&clean=%s", checkPath(configTools.getKallitheaApiUrl()),
                        configTools.getPullRequestId(), configTools.getUserName(),
                        String.valueOf(configTools.needCleanPreviousComments()));
    }

    public static String prepareUrl(ConfigTools configTools) {
        return String.format("%spullrequest/detail/%s", checkPath(configTools.getKallitheaApiUrl()),
                configTools.getPullRequestId());
    }

    public static String buildPRHttpPageUrl(PullRequest pullRequest, ConfigTools configTools) {
        String targetRepositoryBaseUrl = configTools.getPullRequestTargetRepository();
        if (StringUtils.isBlank(targetRepositoryBaseUrl)) {
            targetRepositoryBaseUrl = pullRequest.getTargetRepository();
        }
        String apiKeyParams = configTools.getKallitheaApiKey();
        if (StringUtils.isBlank(apiKeyParams)) {
            return String.format("%s%spull-request/%s/?fulldiff=1", checkPath(configTools.getKallitheaRootPath()),
                    checkPath(targetRepositoryBaseUrl), configTools.getPullRequestId());
        } else {
            return String.format("%s%spull-request/%s/?fulldiff=1&api_key=%s", checkPath(configTools.getKallitheaRootPath()),
                    checkPath(targetRepositoryBaseUrl), configTools.getPullRequestId(), apiKeyParams);
        }
    }

    public static String buildCommentMessage(PostJobIssue issue, ConfigTools configTools, boolean withLink,
            LineComment lineComment) {
        RuleKey ruleKey = issue.ruleKey();
        final String ruleKeyString = ruleKey.toString();
        String sonarUrl = configTools.getSonarUrl();
        if (sonarUrl != null) {
            sonarUrl = String.format("%scoding_rules#rule_key=%s", checkPath(sonarUrl),
                    ruleKeyString.replace(":", "%3A"));
        } else {
            sonarUrl = "";
        }
        String lineCommentLink = "";
        if (lineComment != null) {
            String filePath = lineComment.getFilePath();
            if (lineComment.getLine() == null || lineComment.getMessage() == null) {
                lineCommentLink = String.format(" (%s%s)", FilenameUtils.getName(filePath), lineComment.getLine() == null
                        ? "" : (":" + lineComment.getLine().replace("n", "")));
            } else {
                String link = String.format("%s_%s", filePath.replace(".", "").replace("/", "")
                        .toLowerCase(Locale.getDefault()), lineComment.getLine());
                lineCommentLink = String.format(" (`%s:%s <#comments-%s>`_)", FilenameUtils.getName(filePath),
                        lineComment.getLine().replace("n", ""), link);
            }
        }
        return withLink ? String.format("`%s` - %s [%s_]%n%n.. _%s: %s", issue.severity().name(), issue.message(),
                ruleKeyString, ruleKeyString, sonarUrl)
                : String.format("`%s` - %s [%s_]%s%n", issue.severity().name(), issue.message(), ruleKeyString,
                        lineCommentLink);
    }

    public static Object buildRuleLink(PostJobIssue issue, ConfigTools configTools) {
        RuleKey ruleKey = issue.ruleKey();
        final String ruleKeyString = ruleKey.toString();
        String sonarUrl = configTools.getSonarUrl();
        if (sonarUrl != null) {
            sonarUrl = String.format("%scoding_rules#rule_key=%s", checkPath(sonarUrl),
                    ruleKeyString.replace(":", "%3A"));
        } else {
            sonarUrl = "";
        }
        return String.format(".. _%s: %s%n", ruleKeyString, sonarUrl);
    }

    public static String getFilePath(PostJobIssue issue) {
        String[] keyParts = issue.inputComponent().key().split(":");
        return keyParts[keyParts.length - 1];
    }

    /**
     * Парсинг пришедшей страницы html.
     *
     * @param content содержимое
     */
    public static List<PullRequestFile> parseHtml(byte[] content) {
        HtmlDocParser document = new HtmlDocParser();
        document.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            HTMLEditorKit editorKit = new HTMLEditorKit();
            editorKit.read(new InputStreamReader(inputStream, ConfigTools.UTF_8), document, 0);
        } catch (IOException | BadLocationException e) {
            throw new IllegalStateException(e);
        }
        return document.getPullRequestFiles();
    }
}
