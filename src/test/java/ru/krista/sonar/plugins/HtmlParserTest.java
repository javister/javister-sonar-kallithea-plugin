package ru.krista.sonar.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import ru.krista.sonar.plugins.model.PullRequestFile;
import ru.krista.sonar.plugins.tools.PluginUtils;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверка парсинга html страницы.
 */
public class HtmlParserTest {

    private byte[] htmlContent;

    /**
     * Получить данные файла ресурса.
     *
     * @param resourceName имя файла ресурса
     *
     * @return данные файла ресурса
     *
     * @throws IOException ошибки чтения/записи
     */
    private byte[] getResource(String resourceName) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourceName);
        return IOUtils.toByteArray(inputStream);
    }

    @Before
    public void prepare() {
        try {
            htmlContent = getResource("/pullRequestResponseExample.html");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void checkParseHtml() {
        List<PullRequestFile> pullRequestFiles = PluginUtils.parseHtml(htmlContent);
        assertThat(pullRequestFiles.size()).isEqualTo(16);
    }
}
