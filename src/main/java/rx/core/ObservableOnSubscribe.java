package rx.core;

/**
 * Интерфейс источника данных
 * @param <T> тип данных
 */
public interface ObservableOnSubscribe<T> {
    void subscribe(Observer<? super T> observer) throws Exception;
}
