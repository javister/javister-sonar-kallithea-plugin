package ru.krista.sonar.plugins.model;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * Пул-реквест.
 */
public class PullRequest {

    private final String repository;

    private final String targetRepository;

    private final String revision;

    private static String readRepoName(JSONObject jPullRequest, String repoKey) {
        JSONObject jRepository = jPullRequest.optJSONObject(repoKey);
        if (jRepository == null) {
            throw new IllegalStateException(String.format("Для пул-реквеста %s не задан репозиторий (%s)",
                    jPullRequest.toString(), repoKey));
        }
        String result = jRepository.optString("name");
        if (StringUtils.isBlank(result)) {
            throw new IllegalStateException(String.format("Для пул-реквеста %s задан репозиторий (%s) с пустым именем",
                    jPullRequest.toString(), repoKey));
        }
        return result;
    }

    public PullRequest(JSONObject jPullRequest) {
        this.repository = readRepoName(jPullRequest, "repository");
        this.targetRepository = readRepoName(jPullRequest, "otherRepository");
        this.revision = jPullRequest.optString("revisions");
    }

    public String getRepository() {
        return repository;
    }

    public String getTargetRepository() {
        return targetRepository;
    }

    public String getRevision() {
        return revision;
    }
}
