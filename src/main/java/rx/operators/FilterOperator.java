package rx.operators;

import rx.core.Observable;
import rx.core.Observer;
import rx.functions.Predicate;

/**
 * Оператор фильтрации filter
 */
public class FilterOperator<T> extends Observable<T> {
    private final Observable<T> source;
    private final Predicate<? super T> predicate;

    public FilterOperator(Observable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (predicate.test(item)) {
                        observer.onNext(item);
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        });
    }
}
