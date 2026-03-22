package rx.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Планировщик для вычислительных операций
 * Использует FixedThreadPool с количеством потоков = количеству ядер процессора
 */
public class ComputationScheduler implements Scheduler {
    private final ExecutorService executor;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public ComputationScheduler() {
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("computation-thread-" + counter.incrementAndGet());
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
