package ru.krista.sonar.plugins.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;
import ru.krista.sonar.plugins.PluginConsts;
import ru.krista.sonar.plugins.model.LineComment;
import ru.krista.sonar.plugins.model.PrintedComments;
import ru.krista.sonar.plugins.model.PullRequestFile;
import ru.krista.sonar.plugins.model.PullRequestFileType;
import ru.krista.sonar.plugins.model.PullRequestStatus;
import ru.krista.sonar.plugins.tools.HttpManager;
import ru.krista.sonar.plugins.tools.PluginUtils;

/**
 * Построитель комментариев.
 */
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class PluginCommentBuilder {

    private static final String BR = "<br/>";

    private static final String COMMENT_TABLE_ROW_PATTERN = "   \"%s\", \"%d\"%n";

    private final ConfigTools configTools;
    private final List<PostJobIssue> allIssues = new ArrayList<>();
    private final List<PostJobIssue> resolvedIssues = new ArrayList<>();
    private final Map<Severity, Integer> severityResolvedCount = new EnumMap<>(Severity.class);
    private final Map<Severity, List<PostJobIssue>> severityCount = new EnumMap<>(Severity.class);
    private Map<PullRequestFile, List<LineComment>> lineComments = new HashMap<>();

    public PluginCommentBuilder(ConfigTools configTools) {
        this.configTools = configTools;
        Arrays.stream(Severity.values()).forEach(severity -> {
            severityCount.put(severity, new ArrayList<>());
            severityResolvedCount.put(severity, 0);
        });

    }

    public void appendComments(PostJobIssue issue, PullRequestFile pullRequestFile, boolean isResolvedIssue) {
        if (isResolvedIssue) {
            resolvedIssues.add(issue);
            severityResolvedCount.put(issue.severity(), severityResolvedCount.get(issue.severity()) + 1);
            return;
        }
        Integer line = issue.line();
        boolean isLineComment = line != null && ((pullRequestFile.getType() == PullRequestFileType.MODIFY
                && pullRequestFile.getRowsNums().contains(line.toString()))
                || pullRequestFile.getType() == PullRequestFileType.ADDED);
        if (isLineComment) {
            if (!lineComments.containsKey(pullRequestFile)) {
                lineComments.put(pullRequestFile, new ArrayList<>());
            }
            lineComments.get(pullRequestFile).add(new LineComment(issue, pullRequestFile.getRelativePath(),
                    PluginUtils.buildCommentMessage(issue, configTools, true, null)));
        }
        allIssues.add(issue);
        severityCount.get(issue.severity()).add(issue);
    }

    private LineComment findLineComment(PostJobIssue issue) {
        for (List<LineComment> lLineComments : lineComments.values()) {
            LineComment result = lLineComments.stream()
                    .filter(lineComment -> lineComment.getIssueKey().equals(issue.key()))
                    .findFirst().orElse(null);
            if (result != null) {
                return result;
            }
        }
        return new LineComment(issue, PluginUtils.getFilePath(issue), null);
    }

    private String buildCommonComment() {
        StringBuilder result = new StringBuilder();
        List<PostJobIssue> onlyNewIssue = allIssues.stream().filter(PostJobIssue::isNew).collect(Collectors.toList());
        List<PostJobIssue> otherIssue = allIssues.stream().filter(issue -> !issue.isNew()).collect(Collectors.toList());
        // заголовок
        result.append(String.format("SonarQube overview%n==================%n%n|%n|%n%n"));
        // таблица с количеством и подробный список задач
        result.append(String.format(".. csv-table::%n   :header: \"Total new issue\", \"%d\"%n%n",
                onlyNewIssue.size()));
        Arrays.stream(Severity.values()).sorted(Comparator.reverseOrder()).forEach(severity ->
                result.append(String.format(COMMENT_TABLE_ROW_PATTERN, severity.name(),
                        severityCount.get(severity).stream().filter(PostJobIssue::isNew).count()))
        );
        result.append(String.format("%n**New issue list**%n"));
        onlyNewIssue.sort(Comparator.comparing(PostJobIssue::severity, Comparator.reverseOrder()));
        onlyNewIssue.forEach(issue -> result.append(String.format("%n----%n%n%s",
                PluginUtils.buildCommentMessage(issue, configTools, false, findLineComment(issue))))
        );
        result.append(String.format("%n----%n%n"));
        onlyNewIssue.forEach(issue -> result.append(PluginUtils.buildRuleLink(issue, configTools)));
        result.append(String.format("%n|%n|%n%n"));
        if (!configTools.onlyNewIssue()) {
            result.append(String.format(".. csv-table::%n   :header: \"Total other issue\", \"%d\"%n%n",
                    otherIssue.size()));
            Arrays.stream(Severity.values()).sorted(Comparator.reverseOrder()).forEach(severity ->
                    result.append(String.format(COMMENT_TABLE_ROW_PATTERN, severity.name(),
                            severityCount.get(severity).stream().filter(issue -> !issue.isNew()).count()))
            );
            result.append(String.format("%n**Other issue list**%n"));
            otherIssue.sort(Comparator.comparing(PostJobIssue::severity, Comparator.reverseOrder()));
            otherIssue.forEach(issue -> result.append(String.format("%n----%n%n%s",
                    PluginUtils.buildCommentMessage(issue, configTools, false, findLineComment(issue))))
            );
            result.append(String.format("%n----%n%n"));
            otherIssue.forEach(issue -> result.append(PluginUtils.buildRuleLink(issue, configTools)));
            result.append(String.format("%n|%n|%n%n"));
        }
        // таблица разрешенных задач
        result.append(String.format("%n**Resolved issue**%n%n"));
        result.append(
                String.format(".. csv-table::%n   :header: \"Total resolved\", \"%d\"%n%n", resolvedIssues.size()));
        Arrays.stream(Severity.values()).sorted(Comparator.reverseOrder()).forEach(severity -> result.append(
                String.format(COMMENT_TABLE_ROW_PATTERN, severity.name(), severityResolvedCount.get(severity)))
        );
        return result.toString();
    }

    private static void printHtmlTable(List<PostJobIssue> issueList, String headerName, Map<Severity,
            List<PostJobIssue>> severityCount, Map<Severity, Integer> severityResolvedCount,
            boolean isNew, StringBuilder result) {
        result.append("<table border=\"1\"><tr><th>").append(headerName).append("</th><th>").append(issueList.size())
                .append("</th></tr>");
        if (severityCount != null) {
            Arrays.stream(Severity.values()).sorted(Comparator.reverseOrder()).forEach(severity ->
                    result.append(String.format("<tr><td>%s</td><td>%d</td></tr>", severity.name(),
                            severityCount.get(severity).stream()
                                    .filter(issue -> (isNew && issue.isNew()) || (!isNew && !issue.isNew())).count()))
            );
        } else if (severityResolvedCount != null) {
            Arrays.stream(Severity.values()).sorted(Comparator.reverseOrder()).forEach(severity -> result.append(
                    String.format("<tr><td>%s</td><td>%d</td></tr>", severity.name(),
                            severityResolvedCount.get(severity)))
            );
        }
        result.append("</table>");
    }

    private String buildMailBody(String ruStatus) {
        StringBuilder result = new StringBuilder();
        List<PostJobIssue> onlyNewIssue = allIssues.stream().filter(PostJobIssue::isNew).collect(Collectors.toList());
        List<PostJobIssue> otherIssue = allIssues.stream().filter(issue -> !issue.isNew()).collect(Collectors.toList());
        result.append(String.format("<p>Комментарий оставлен со статусом: <b>%s</b></p>", ruStatus));
        printHtmlTable(onlyNewIssue, "Новых задач", severityCount, null, true, result);
        if (!configTools.onlyNewIssue()) {
            result.append(BR);
            printHtmlTable(otherIssue, "Остальных задач", severityCount, null, false, result);
        }
        result.append(BR);
        printHtmlTable(resolvedIssues, "Разрешенных задач", null, severityResolvedCount, false, result);
        result.append(BR);
        result.append(String.format(
                "Комментарий: <a href=\"%s%%pullRequestRepo%%pull-request/%s#comment-%%commentId%%\">%s</a>",
                PluginUtils.checkPath(configTools.getKallitheaRootPath()), configTools.getPullRequestId(),
                configTools.getPullRequestId()));
        return result.toString();
    }

    private String toRequestBody() {
        JSONObject jResult = new JSONObject();
        JSONArray jLineComments = new JSONArray();
        lineComments.values().forEach(fileLineComments -> {
            fileLineComments.sort(Comparator.comparing(LineComment::getSeverity, Comparator.reverseOrder()));
            for (int i = 0; i < fileLineComments.size(); i++) {
                JSONObject jLineComment = fileLineComments.get(i).toRequestBody();
                jLineComment.put("order", i);
                jLineComments.add(jLineComment);
            }
        });
        PrintedComments printedComments = configTools.getPrintedComments();
        if (printedComments == PrintedComments.ALL || printedComments == PrintedComments.LINES) {
            jResult.put("line", jLineComments);
        }
        if (printedComments == PrintedComments.ALL || printedComments == PrintedComments.COMMON) {
            jResult.put("common", buildCommonComment());
        }
        if (configTools.needChangeStatus()) {
            boolean success = checkIssues();
            jResult.put("status", success ? PullRequestStatus.APPROVED : PullRequestStatus.REJECTED);
            // письмо
            if (configTools.needSendStatusMail()) {
                JSONObject jMailInfo = new JSONObject();
                String ruStatus = success ? "Одобрено" : "Отклонено";
                jMailInfo.put("ruStatus", ruStatus);
                jMailInfo.put("body", buildMailBody(ruStatus));
                jResult.put("mailInfo", jMailInfo);
            }
        }
        return jResult.toString();
    }

    private boolean checkIssue(PostJobIssue issue) {
        if (!issue.isNew()) {
            return false;
        }
        String thresholdSeverityStr = configTools.getStatusThreshold();
        if (!PluginConsts.SEVERITY_NONE.equals(thresholdSeverityStr)) {
            Severity severity = issue.severity();
            Severity thresholdSeverity = Severity.valueOf(thresholdSeverityStr);
            return severity.compareTo(thresholdSeverity) >= 0;
        }
        return true;
    }

    private boolean checkIssues() {
        return allIssues.stream().noneMatch(this::checkIssue);
    }

    public void send(HttpManager httpManager) {
        String body = toRequestBody();
        String url = PluginUtils.prepareCommentUrl(configTools);
        httpManager.send(url, body);
    }
}
