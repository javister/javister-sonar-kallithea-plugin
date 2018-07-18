package ru.krista.sonar.plugins.tools;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import ru.krista.sonar.plugins.model.PullRequestFile;

/**
 * Парсер html-страницы пул-реквеста.
 * <p>
 * Собирает измененные файлы.
 */
public class HtmlDocParser extends HTMLDocument {

    private static final long serialVersionUID = 1L;

    private final List<PullRequestFile> pullRequestFiles = new ArrayList<>();

    @Override
    public HTMLEditorKit.ParserCallback getReader(int pos) {
        return new HtmlParserCallback(pullRequestFiles);
    }

    public List<PullRequestFile> getPullRequestFiles() {
        return pullRequestFiles;
    }
}
