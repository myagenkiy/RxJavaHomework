package rx.operators;

import rx.core.Observable;
import rx.core.Observer;
import rx.functions.Function;

/**
 * Оператор преобразования map
 */
public class MapOperator<T, R> extends Observable<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends R> mapper;

    public MapOperator(Observable<T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    R result = mapper.apply(item);
                    observer.onNext(result);
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
