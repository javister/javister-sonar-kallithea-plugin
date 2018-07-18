package ru.krista.sonar.plugins.exceptions;

/**
 * Ошибки задания настроек плагина.
 */
public class ConfigException extends RuntimeException {

    /**
     * Пустой конструктор.
     */
    public ConfigException() {
        super();
    }

    /**
     * Конструктор с простым сообщением.
     *
     * @param message сообщение
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * Конструктор с форматированием.
     *
     * @param message сообщение
     * @param args аргументы
     */
    public ConfigException(String message, Object... args) {
        super(String.format(message, args));
    }

    /**
     * Конструктор с исключением.
     *
     * @param cause исключение
     */
    public ConfigException(Throwable cause) {
        super(cause);
    }

    /**
     * Конструктор с детальным сообщением и исключением.
     *
     * @param message детальное сообщение
     * @param cause исключение
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
