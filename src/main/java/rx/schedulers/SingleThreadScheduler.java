package rx.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Планировщик для однопоточного выполнения
 * Использует один выделенный поток
 */
public class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executor;

    public SingleThreadScheduler() {
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("single-thread-" + counter.incrementAndGet());
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
