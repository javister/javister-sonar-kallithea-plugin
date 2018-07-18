package ru.krista.sonar.plugins;

import java.util.List;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import ru.krista.sonar.plugins.components.ConfigTools;
import ru.krista.sonar.plugins.components.PluginCommentBuilder;
import ru.krista.sonar.plugins.components.PullRequestHandler;
import ru.krista.sonar.plugins.model.PullRequestFileType;
import ru.krista.sonar.plugins.model.PullRequestFile;
import ru.krista.sonar.plugins.tools.HttpManager;
import ru.krista.sonar.plugins.tools.PluginUtils;

/**
 * Точка входа.
 */
public class KallitheaPluginPostJob implements PostJob {

    private static final Logger LOGGER = Loggers.get(KallitheaPluginPostJob.class);

    private final PullRequestHandler pullRequestHandler;
    private final PluginCommentBuilder commentBuilder;
    private final ConfigTools configTools;

    public KallitheaPluginPostJob(PullRequestHandler pullRequestHandler, PluginCommentBuilder commentBuilder,
            ConfigTools configTools) {
        this.pullRequestHandler = pullRequestHandler;
        this.commentBuilder = commentBuilder;
        this.configTools = configTools;
    }

    @Override
    public void describe(PostJobDescriptor postJobDescriptor) {
        postJobDescriptor.name(getClass().getName())
                .requireProperty(PluginConsts.PLUGIN_PULL_REQUEST_ID_PROP_NAME);
    }

    private void processIssue(PostJobIssue issue, List<PullRequestFile> reportFiles, boolean isResolvedIssue) {
        try {
            InputComponent component = issue.inputComponent();
            if (component == null || !component.isFile() || component.key() == null
                    || (configTools.onlyNewIssue() && !issue.isNew())) {
                return;
            }
            /*LOGGER.info("issue - {} - {} - {} - {} - status {} - line {}", issue.severity(), issue.message(), issue.componentKey(),
                    isResolvedIssue, issue.isNew(), issue.line());*/
            String thresholdSeverityStr = configTools.getIssueThreshold();
            if (!PluginConsts.SEVERITY_NONE.equals(thresholdSeverityStr)) {
                Severity severity = issue.severity();
                Severity thresholdSeverity = Severity.valueOf(thresholdSeverityStr);
                if (severity.compareTo(thresholdSeverity) < 0) {
                    return;
                }
            }
            String relativeFilePath = PluginUtils.getFilePath(issue);
            PullRequestFile pullRequestFile = reportFiles.stream()
                    .filter(file -> file.getRelativePath().endsWith(relativeFilePath))
                    .findFirst().orElse(null);
            if (pullRequestFile != null && isResolvedIssue) {
                commentBuilder.appendComments(issue, pullRequestFile, true);
                return;
            }
            if (pullRequestFile == null || pullRequestFile.getType() == PullRequestFileType.REMOVED) {
                return;
            }
            commentBuilder.appendComments(issue, pullRequestFile, false);
        } catch (Exception e) {
            LOGGER.warn("Error processing issue {} : {}", issue, e);
        }
    }

    @Override
    public void execute(PostJobContext postJobContext) {
        if (configTools.check()) {
            LOGGER.info("PostJob execute");
            try (HttpManager httpManager = new HttpManager(configTools)) {
                List<PullRequestFile> reportFiles = pullRequestHandler.buildFiles(configTools, httpManager);
                postJobContext.issues().forEach(issue -> processIssue(issue, reportFiles, false));
                postJobContext.resolvedIssues().forEach(issue -> processIssue(issue, reportFiles, true));
                commentBuilder.send(httpManager);
            } catch (Exception e) {
                LOGGER.info("Error executing {}", e);
                throw new IllegalStateException(e);
            }
            LOGGER.info("Successfulled");
        }
    }
}
