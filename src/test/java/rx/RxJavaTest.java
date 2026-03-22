package rx;

import rx.core.Disposable;
import rx.core.Observable;
import rx.schedulers.ComputationScheduler;
import rx.schedulers.IOScheduler;
import rx.schedulers.SingleThreadScheduler;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class RxJavaTest {

    // ==================== ТЕСТЫ БАЗОВЫХ КОМПОНЕНТОВ ====================

    @Test
    public void testJust() {
        AtomicInteger counter = new AtomicInteger(0);

        Observable.just(42).subscribe(value -> {
            assertEquals(42, value.intValue());
            counter.incrementAndGet();
        });

        assertEquals(1, counter.get());
    }

    @Test
    public void testFromIterable() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        AtomicInteger sum = new AtomicInteger(0);

        Observable.fromIterable(list).subscribe(sum::addAndGet);

        assertEquals(15, sum.get());
    }

    @Test
    public void testFromArray() {
        AtomicInteger counter = new AtomicInteger(0);

        Observable.fromArray(10, 20, 30).subscribe(value -> counter.incrementAndGet());

        assertEquals(3, counter.get());
    }

    @Test
    public void testRange() {
        AtomicInteger sum = new AtomicInteger(0);

        Observable.range(1, 5).subscribe(sum::addAndGet);

        assertEquals(15, sum.get());
    }

    // ==================== ТЕСТЫ ОПЕРАТОРОВ ====================

    @Test
    public void testMap() {
        Observable.just(5)
                .map(x -> x * 2)
                .subscribe(value -> assertEquals(10, value.intValue()));
    }

    @Test
    public void testFilter() {
        AtomicInteger counter = new AtomicInteger(0);

        Observable.range(1, 10)
                .filter(x -> x % 2 == 0)
                .subscribe(value -> counter.incrementAndGet());

        assertEquals(5, counter.get());
    }

    @Test
    public void testMapAndFilter() {
        AtomicInteger sum = new AtomicInteger(0);

        Observable.range(1, 10)
                .filter(x -> x % 2 == 0)
                .map(x -> x * 2)
                .subscribe(sum::addAndGet);

        assertEquals(60, sum.get());
    }

    @Test
    public void testFlatMap() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger(0);

        Observable.just(3)
                .flatMap(x -> Observable.range(1, x))
                .subscribe(
                        result::addAndGet,
                        Throwable::printStackTrace,
                        latch::countDown
                );

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(6, result.get());
    }

    @Test
    public void testFlatMapWithMultipleItems() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger sum = new AtomicInteger(0);

        Observable.fromArray(2, 3)
                .flatMap(x -> Observable.range(1, x))
                .subscribe(
                        sum::addAndGet,
                        Throwable::printStackTrace,
                        latch::countDown
                );

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(9, sum.get());
    }

    // ==================== ТЕСТЫ ОБРАБОТКИ ОШИБОК ====================

    @Test
    public void testErrorHandling() {
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Observable.<Integer>create(observer -> {
            observer.onNext(1);
            throw new RuntimeException("Test error");
        }).subscribe(
                value -> {},
                errorRef::set,
                () -> {}
        );

        assertNotNull(errorRef.get());
        assertEquals("Test error", errorRef.get().getMessage());
    }

    @Test
    public void testMapErrorHandling() {
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Observable.just(10)
                .map(x -> {
                    if (x > 5) {
                        throw new RuntimeException("Value too large");
                    }
                    return x;
                })
                .subscribe(
                        value -> {},
                        errorRef::set,
                        () -> {}
                );

        assertNotNull(errorRef.get());
        assertEquals("Value too large", errorRef.get().getMessage());
    }

    // ==================== ТЕСТЫ ПЛАНИРОВЩИКОВ ====================

    @Test
    public void testIOScheduler() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        Observable.just("test")
                .subscribeOn(new IOScheduler())
                .subscribe(value -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        latch.await(1, TimeUnit.SECONDS);
        assertTrue(threadName.get().contains("io-thread"));
    }

    @Test
    public void testComputationScheduler() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        Observable.just("test")
                .subscribeOn(new ComputationScheduler())
                .subscribe(value -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        latch.await(1, TimeUnit.SECONDS);
        assertTrue(threadName.get().contains("computation-thread"));
    }

    @Test
    public void testSingleThreadScheduler() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> threadName1 = new AtomicReference<>();
        AtomicReference<String> threadName2 = new AtomicReference<>();

        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        Observable.just(1)
                .subscribeOn(scheduler)
                .subscribe(value -> {
                    threadName1.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        Observable.just(2)
                .subscribeOn(scheduler)
                .subscribe(value -> {
                    threadName2.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(threadName1.get(), threadName2.get());
    }

    @Test
    public void testSubscribeOnAndObserveOn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> generationThread = new AtomicReference<>();
        AtomicReference<String> processingThread = new AtomicReference<>();

        System.out.println("=== Test Started ===");

        Observable.just("test")
                .map(s -> {
                    generationThread.set(Thread.currentThread().getName());
                    System.out.println("MAP (generation) in thread: " + Thread.currentThread().getName());
                    return s.toUpperCase();
                })
                .subscribeOn(new ComputationScheduler())
                .observeOn(new IOScheduler())
                .subscribe(
                        value -> {
                            processingThread.set(Thread.currentThread().getName());
                            System.out.println("SUBSCRIBE (processing) in thread: " + Thread.currentThread().getName());
                            System.out.println("Value: " + value);
                            latch.countDown();
                        },
                        Throwable::printStackTrace,
                        () -> System.out.println("Complete")
                );

        latch.await(2, TimeUnit.SECONDS);

        System.out.println("Generation thread: " + generationThread.get());
        System.out.println("Processing thread: " + processingThread.get());

        assertNotNull("Generation thread should be set", generationThread.get());
        assertNotNull("Processing thread should be set", processingThread.get());

        // Проверяем, что генерация (map) выполняется в ComputationScheduler
        assertTrue("Generation should be in computation thread, but was: " + generationThread.get(),
                generationThread.get().contains("computation"));

        // Проверяем, что обработка (subscribe) выполняется в IOScheduler
        assertTrue("Processing should be in IO thread, but was: " + processingThread.get(),
                processingThread.get().contains("io"));

        // Проверяем, что потоки разные
        assertNotEquals("Generation and processing threads should be different",
                generationThread.get(), processingThread.get());
    }

    // ==================== ТЕСТЫ ОТМЕНЫ ПОДПИСКИ ====================

    @Test
    public void testDisposable() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);

        Disposable disposable = Observable.range(1, 100)
                .subscribe(value -> {
                    counter.incrementAndGet();
                });

        Thread.sleep(10);
        disposable.dispose();
        assertTrue(disposable.isDisposed());

        int countAfterDispose = counter.get();
        Thread.sleep(50);
        assertEquals(countAfterDispose, counter.get());
    }

    // ==================== КОМПЛЕКСНЫЕ ТЕСТЫ ====================

    @Test
    public void testComplexPipeline() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger(0);
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");

        System.out.println("=== Complex Pipeline Test ===");
        System.out.println("Input names: " + names);

        Observable.fromIterable(names)
                .filter(name -> {
                    boolean pass = name.length() > 3;
                    System.out.println("Filter '" + name + "': length=" + name.length() + ", pass=" + pass);
                    return pass;
                })
                .map(name -> {
                    String upper = name.toUpperCase();
                    System.out.println("Map '" + name + "' -> '" + upper + "'");
                    return upper;
                })
                .flatMap(name -> {
                    System.out.println("FlatMap: " + name + " -> length=" + name.length());
                    return Observable.just(name.length());
                })
                .subscribeOn(new ComputationScheduler())
                .observeOn(new IOScheduler())
                .subscribe(
                        value -> {
                            System.out.println("Result value: " + value);
                            result.addAndGet(value);
                        },
                        error -> {
                            System.err.println("Error: " + error);
                            error.printStackTrace();
                            latch.countDown();
                        },
                        () -> {
                            System.out.println("Complete! Total sum: " + result.get());
                            latch.countDown();
                        }
                );

        latch.await(3, TimeUnit.SECONDS);
        System.out.println("Expected: 17, Actual: " + result.get());
        assertEquals("Sum of name lengths should be 17", 17, result.get());
    }

    @Test
    public void testEmptyObservable() {
        AtomicInteger nextCount = new AtomicInteger(0);
        AtomicReference<Boolean> completed = new AtomicReference<>(false);

        Observable.create(observer -> {
            observer.onComplete();
        }).subscribe(
                value -> nextCount.incrementAndGet(),
                Throwable::printStackTrace,
                () -> completed.set(true)
        );

        assertEquals(0, nextCount.get());
        assertTrue(completed.get());
    }
}
