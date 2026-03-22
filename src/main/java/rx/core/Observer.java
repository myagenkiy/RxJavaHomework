package rx.core;

/**
 * Интерфейс Наблюдателя - получает события из потока
 * @param <T> тип данных
 */
public interface Observer<T> {
    /**
     * Получает следующий элемент потока
     * @param item элемент данных
     */
    void onNext(T item);

    /**
     * Обрабатывает ошибку в потоке
     * @param t исключение
     */
    void onError(Throwable t);

    /**
     * Вызывается при успешном завершении потока
     */
    void onComplete();
}
