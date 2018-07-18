package ru.krista.sonar.plugins.model;

/**
 * Ответ сервера.
 */
public class ResponseInfo {

    private final byte[] content;
    private final String statusCode;

    /**
     * Инициализирующий конструктор.
     *
     * @param statusCode статус ответа
     * @param content    содержимое ответа
     */
    public ResponseInfo(String statusCode, byte[] content) {
        this.content = content;
        this.statusCode = statusCode;
    }

    public byte[] getContent() {
        return content;
    }

    public String getStatusCode() {
        return statusCode;
    }
}
