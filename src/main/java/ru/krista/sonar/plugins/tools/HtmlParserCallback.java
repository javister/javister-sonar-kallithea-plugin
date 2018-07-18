package ru.krista.sonar.plugins.tools;

import java.util.List;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang.StringUtils;
import ru.krista.sonar.plugins.model.PullRequestFileType;
import ru.krista.sonar.plugins.model.PullRequestFile;

/**
 * Парсер элементов.
 */
public class HtmlParserCallback extends HTMLEditorKit.ParserCallback {

    private static final String LIST_ALL_FILES_DIV_CLASS = "cs_files";
    private static final String ADD_FILE_DIV_CLASS = "cs_A";
    private static final String MODIFY_FILE_DIV_CLASS = "cs_M";
    private static final String DELETE_FILE_DIV_CLASS = "cs_D";
    private static final String FILE_INFO_DIV_CLASS = "node";
    private static final String LIST_DIFF_FILES_DIV_CLASS = "commentable-diff";
    private static final String END_FILE_INFO_DIV_CLASS = "changes";
    private static final String DIFF_TABLE_CLASS_NAME = "code-difftable";
    private static final String DIFF_ROW_ADD_CLASS_NAME = "line add";
    private static final String DIFF_ROW_COL_CLASS_NAME = "lineno new";

    private StringBuilder textContent = new StringBuilder();
    private boolean startAllChangeFiles;
    private boolean startDiffTable;
    private boolean startDiffRow;
    private boolean startDiffCol;
    private boolean startARowNum;
    private int fileInfoIndex = -1;
    private int filesDiffsIndex = -1;

    private PullRequestFile currentFile;
    private PullRequestFile currentDiffFile;

    private final List<PullRequestFile> pullRequestFiles;

    /**
     * Инициализирующий конструктор.
     *
     * @param pullRequestFiles список файлов (куда заполнять)
     */
    HtmlParserCallback(List<PullRequestFile> pullRequestFiles) {
        super();
        this.pullRequestFiles = pullRequestFiles;
    }

    private PullRequestFileType getFileType(MutableAttributeSet attributes) {
        PullRequestFileType result = null;
        if (attributes.containsAttribute(HTML.Attribute.CLASS, ADD_FILE_DIV_CLASS)) {
            result = PullRequestFileType.ADDED;
        } else if (attributes.containsAttribute(HTML.Attribute.CLASS, MODIFY_FILE_DIV_CLASS)) {
            result = PullRequestFileType.MODIFY;
        } else if (attributes.containsAttribute(HTML.Attribute.CLASS, DELETE_FILE_DIV_CLASS)) {
            result = PullRequestFileType.REMOVED;
        }
        return result;
    }

    private PullRequestFile findFileByRef(String ref) {
        return pullRequestFiles.stream().filter(file -> file.getType() == PullRequestFileType.MODIFY
                && StringUtils.isNotBlank(file.getRefToDiff())
                && file.getRefToDiff().equals(ref)).findFirst().orElse(null);
    }

    private void processDivTag(MutableAttributeSet attributes) {
        if (attributes.containsAttribute(HTML.Attribute.CLASS, LIST_ALL_FILES_DIV_CLASS)) {
            startAllChangeFiles = true;
            return;
        }
        if (attributes.containsAttribute(HTML.Attribute.CLASS, LIST_DIFF_FILES_DIV_CLASS)) {
            startAllChangeFiles = false;
            currentFile = null;
            filesDiffsIndex = 0;
            return;
        }
        if (startAllChangeFiles) {
            PullRequestFileType fileType = getFileType(attributes);
            if (fileType != null) {
                currentFile = new PullRequestFile(fileType);
                pullRequestFiles.add(currentFile);
                return;
            }
            if (currentFile == null) {
                return;
            }
            // node таг
            if (attributes.containsAttribute(HTML.Attribute.CLASS, FILE_INFO_DIV_CLASS)) {
                fileInfoIndex = 0;
                return;
            }
            if (attributes.containsAttribute(HTML.Attribute.CLASS, END_FILE_INFO_DIV_CLASS)) {
                currentFile = null;
            }
            return;
        }
        if (filesDiffsIndex > -1 && currentDiffFile == null) {
            currentDiffFile = findFileByRef(getAttributeValue(attributes, HTML.Attribute.ID));
        }
    }

    private String getAttributeValue(MutableAttributeSet attributes, HTML.Attribute attribute) {
        Object result = attributes.getAttribute(attribute);
        return result == null ? null : result.toString();
    }

    @Override
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        //
        if (fileInfoIndex > -1) {
            fileInfoIndex++;
        }
        if (HTML.Tag.A.equals(t) && fileInfoIndex > -1 && currentFile != null) {
            currentFile.setRefToDiff(getAttributeValue(a, HTML.Attribute.HREF));
        }
        //
        if (filesDiffsIndex > -1) {
            filesDiffsIndex++;
        }
        if (HTML.Tag.DIV.equals(t)) {
            processDivTag(a);
        }
        if (HTML.Tag.TABLE.equals(t) && currentDiffFile != null
                && a.containsAttribute(HTML.Attribute.CLASS, DIFF_TABLE_CLASS_NAME)) {
            startDiffTable = true;
        }
        if (HTML.Tag.TR.equals(t) && startDiffTable
                && a.containsAttribute(HTML.Attribute.CLASS, DIFF_ROW_ADD_CLASS_NAME)) {
            startDiffRow = true;
        }
        if (HTML.Tag.TD.equals(t) && startDiffRow
                && a.containsAttribute(HTML.Attribute.CLASS, DIFF_ROW_COL_CLASS_NAME)) {
            startDiffCol = true;
        }
        if (HTML.Tag.A.equals(t) && startDiffCol) {
            startARowNum = true;
        }
    }

    @Override
    public void handleText(char[] data, int pos) {
        textContent.append(new String(data));
    }

    @Override
    public void handleEndTag(HTML.Tag t, int pos) {
        //
        if (HTML.Tag.DIV.equals(t) && fileInfoIndex == 0) {
            fileInfoIndex = -1;
        }
        if (fileInfoIndex > -1) {
            fileInfoIndex--;
        }
        if (HTML.Tag.A.equals(t) && fileInfoIndex > -1) {
            currentFile.setRelativePath(textContent.toString());
        }
        //
        if (HTML.Tag.DIV.equals(t) && filesDiffsIndex == 0) {
            filesDiffsIndex = -1;
        }
        if (filesDiffsIndex > -1) {
            filesDiffsIndex--;
        }
        if (HTML.Tag.TABLE.equals(t) && currentDiffFile != null) {
            currentDiffFile = null;
            startDiffTable = false;
        }
        if (HTML.Tag.TR.equals(t) && startDiffRow) {
            startDiffRow = false;
        }
        if (HTML.Tag.TD.equals(t) && startDiffCol) {
            startDiffCol = false;
        }
        if (HTML.Tag.A.equals(t) && startARowNum && currentDiffFile != null) {
            startARowNum = false;
            currentDiffFile.addRowNum(textContent.toString());
        }
        textContent = new StringBuilder();
    }
}
