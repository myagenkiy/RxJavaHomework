package rx.functions;

/**
 * Функциональный интерфейс для преобразования данных
 * @param <T> входной тип
 * @param <R> выходной тип
 */
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t) throws Exception;
}