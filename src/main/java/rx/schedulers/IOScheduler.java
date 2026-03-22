package rx.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Планировщик для операций ввода-вывода
 * Использует CachedThreadPool - создает потоки по мере необходимости
 */
public class IOScheduler implements Scheduler {
    private final ExecutorService executor;

    public IOScheduler() {
        this.executor = Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("io-thread-" + counter.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
