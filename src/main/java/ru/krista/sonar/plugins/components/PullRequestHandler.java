package ru.krista.sonar.plugins.components;

import java.util.List;

import net.sf.json.JSONObject;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import ru.krista.sonar.plugins.model.PullRequest;
import ru.krista.sonar.plugins.model.PullRequestFile;
import ru.krista.sonar.plugins.model.ResponseInfo;
import ru.krista.sonar.plugins.tools.HttpManager;
import ru.krista.sonar.plugins.tools.PluginUtils;

/**
 * Утилитный класс для работы с пул-реквестом.
 */
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class PullRequestHandler {

    /**
     * Получить измененные пул-реквестом файлы.
     *
     * @param configTools конфигурация плагина
     * @param httpManager менеджер подключений
     *
     * @return измененные пул-реквестом файлы
     */
    public List<PullRequestFile> buildFiles(ConfigTools configTools, HttpManager httpManager) {
        // 1. получаем информацию по ПР
        ResponseInfo responseInfo = httpManager.select(PluginUtils.prepareUrl(configTools));
        JSONObject jPullRequestInfo = JSONObject
                .fromObject(new String(responseInfo.getContent(), ConfigTools.UTF_8));
        // 2. составляем url html-страницы
        PullRequest pullRequest = new PullRequest(jPullRequestInfo);
        ResponseInfo httpPageInfo = httpManager.select(PluginUtils.buildPRHttpPageUrl(pullRequest, configTools));
        // 3. парсим и собираем файлы
        return PluginUtils.parseHtml(httpPageInfo.getContent());
    }
}
