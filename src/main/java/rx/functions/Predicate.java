package rx.functions;

/**
 * Функциональный интерфейс для фильтрации данных
 * @param <T> тип данных
 */
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t) throws Exception;
}
