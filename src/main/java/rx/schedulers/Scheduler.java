package rx.schedulers;

/**
 * Интерфейс планировщика - управляет выполнением задач в потоках
 */
public interface Scheduler {
    /**
     * Выполняет задачу в соответствующем потоке
     * @param task задача для выполнения
     */
    void execute(Runnable task);

    /**
     * Завершает работу планировщика
     */
    default void shutdown() {
        // По умолчанию ничего не делает
    }
}
