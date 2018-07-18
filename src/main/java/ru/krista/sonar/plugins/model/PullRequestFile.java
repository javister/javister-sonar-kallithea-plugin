package ru.krista.sonar.plugins.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Изменный пул-реквестом файл.
 */
public class PullRequestFile {

    private final PullRequestFileType type;
    private String refToDiff;
    private String relativePath = "";
    private final List<String> rowsNums = new ArrayList<>();

    public PullRequestFile(PullRequestFileType type) {
        this.type = type;
    }

    public PullRequestFileType getType() {
        return type;
    }

    public void setRefToDiff(String refToDiff) {
        this.refToDiff = refToDiff.startsWith("#") ? refToDiff.substring(1) : refToDiff;
    }

    public String getRefToDiff() {
        return refToDiff;
    }

    public void appendRelativePath(String path) {
        relativePath = String.format("%s%s", relativePath, path);
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void addRowNum(String rowNum) {
        if (StringUtils.isNotBlank(rowNum)) {
            rowsNums.add(rowNum.trim());
        }
    }

    public List<String> getRowsNums() {
        return rowsNums;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PullRequestFile && ((PullRequestFile) obj).getRelativePath().equals(relativePath);
    }

    @Override
    public int hashCode() {
        return relativePath.hashCode();
    }
}
