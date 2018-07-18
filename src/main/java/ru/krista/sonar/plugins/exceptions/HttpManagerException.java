package ru.krista.sonar.plugins.exceptions;

/**
 * Исключения при работе с менеджером закачек.
 */
public class HttpManagerException extends RuntimeException {

    /**
     * Пустой конструктор.
     */
    public HttpManagerException() {
        super();
    }

    /**
     * Конструктор с простым сообщением.
     *
     * @param message сообщение
     */
    public HttpManagerException(String message) {
        super(message);
    }

    /**
     * Конструктор с форматированием.
     *
     * @param message сообщение
     * @param args аргументы
     */
    public HttpManagerException(String message, Object... args) {
        super(String.format(message, args));
    }

    /**
     * Конструктор с исключением.
     *
     * @param cause исключение
     */
    public HttpManagerException(Throwable cause) {
        super(cause);
    }

    /**
     * Конструктор с детальным сообщением и исключением.
     *
     * @param message детальное сообщение
     * @param cause исключение
     */
    public HttpManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
