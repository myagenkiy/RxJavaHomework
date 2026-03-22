package rx.core;

import rx.functions.Function;
import rx.functions.Predicate;
import rx.operators.FilterOperator;
import rx.operators.FlatMapOperator;
import rx.operators.MapOperator;
import rx.schedulers.Scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Observable<T> {

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                try {
                    source.subscribe(observer);
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        };
    }

    public static <T> Observable<T> just(T item) {
        return create(observer -> {
            if (observer instanceof Disposable && ((Disposable) observer).isDisposed()) {
                return;
            }
            observer.onNext(item);
            observer.onComplete();
        });
    }

    public static <T> Observable<T> fromIterable(Iterable<T> iterable) {
        return create(observer -> {
            try {
                for (T item : iterable) {
                    if (observer instanceof Disposable && ((Disposable) observer).isDisposed()) {
                        break;
                    }
                    observer.onNext(item);
                }
                observer.onComplete();
            } catch (Exception e) {
                observer.onError(e);
            }
        });
    }

    @SafeVarargs
    public static <T> Observable<T> fromArray(T... items) {
        return create(observer -> {
            try {
                for (T item : items) {
                    if (observer instanceof Disposable && ((Disposable) observer).isDisposed()) {
                        break;
                    }
                    observer.onNext(item);
                }
                observer.onComplete();
            } catch (Exception e) {
                observer.onError(e);
            }
        });
    }

    public static Observable<Integer> range(int start, int count) {
        return create(observer -> {
            try {
                for (int i = start; i < start + count; i++) {
                    if (observer instanceof Disposable && ((Disposable) observer).isDisposed()) {
                        break;
                    }
                    observer.onNext(i);
                }
                observer.onComplete();
            } catch (Exception e) {
                observer.onError(e);
            }
        });
    }

    public Disposable subscribe(Observer<? super T> observer) {
        Subscription subscription = new Subscription();

        Observer<? super T> wrappedObserver = new Observer<T>() {
            @Override
            public void onNext(T item) {
                if (!subscription.isDisposed()) {
                    observer.onNext(item);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!subscription.isDisposed()) {
                    observer.onError(t);
                }
            }

            @Override
            public void onComplete() {
                if (!subscription.isDisposed()) {
                    observer.onComplete();
                }
            }
        };

        try {
            subscribeActual(wrappedObserver);
        } catch (Exception e) {
            wrappedObserver.onError(e);
        }

        return subscription;
    }

    public Disposable subscribe(
            java.util.function.Consumer<? super T> onNext,
            java.util.function.Consumer<Throwable> onError,
            Runnable onComplete) {

        return subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });
    }

    public Disposable subscribe(java.util.function.Consumer<? super T> onNext) {
        return subscribe(onNext, Throwable::printStackTrace, () -> {});
    }

    protected abstract void subscribeActual(Observer<? super T> observer);

    // ============ ОПЕРАТОРЫ ============

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new MapOperator<>(this, mapper);
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return new FilterOperator<>(this, predicate);
    }

    public <R> Observable<R> flatMap(Function<? super T, ? extends Observable<? extends R>> mapper) {
        return new FlatMapOperator<>(this, mapper);
    }

    // ============ ПЛАНИРОВЩИКИ ============

    /**
     * Исправленный subscribeOn - вся подписка выполняется в указанном планировщике
     */
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                scheduler.execute(() -> {
                    Observable.this.subscribeActual(observer);
                });
            }
        };
    }

    /**
     * Исправленный observeOn - обработка элементов выполняется в указанном планировщике
     */
    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                Observable.this.subscribeActual(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                });
            }
        };
    }
}