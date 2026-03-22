package rx.operators;

import rx.core.Observable;
import rx.core.Observer;
import rx.core.Subscription;
import rx.functions.Function;

import java.util.concurrent.atomic.AtomicInteger;

public class FlatMapOperator<T, R> extends Observable<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends Observable<? extends R>> mapper;

    public FlatMapOperator(Observable<T> source,
                           Function<? super T, ? extends Observable<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        AtomicInteger pending = new AtomicInteger(1);
        Subscription subscription = new Subscription();

        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                if (subscription.isDisposed()) return;

                try {
                    pending.incrementAndGet();
                    Observable<? extends R> observable = mapper.apply(item);

                    observable.subscribe(new Observer<R>() {
                        @Override
                        public void onNext(R value) {
                            if (!subscription.isDisposed()) {
                                observer.onNext(value);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            if (!subscription.isDisposed()) {
                                subscription.dispose();
                                observer.onError(t);
                            }
                        }

                        @Override
                        public void onComplete() {
                            if (!subscription.isDisposed()) {
                                if (pending.decrementAndGet() == 0) {
                                    observer.onComplete();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    observer.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!subscription.isDisposed()) {
                    subscription.dispose();
                    observer.onError(t);
                }
            }

            @Override
            public void onComplete() {
                if (!subscription.isDisposed()) {
                    if (pending.decrementAndGet() == 0) {
                        observer.onComplete();
                    }
                }
            }
        });
    }
}