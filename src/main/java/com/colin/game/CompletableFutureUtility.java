package com.colin.game;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * CompletableFutureUtility class provides utility methods for working with CompletableFuture.
 *
 * <p>
 * Includes functionalities for:
 * - Running tasks asynchronously.
 * - Running tasks in parallel.
 * - Running tasks sequentially.
 * - Running a task with a timeout.
 * - Running multiple tasks and taking the first result.
 * - Waiting for a button press to complete a future and perform a custom action.
 * - Waiting for a button press to complete a future with a single result.
 * - Waiting for a button press to complete a future with a list of results.
 * </p>
 *
 * @version 1.6
 * @author Colin Jokisch
 */
public class CompletableFutureUtility {

    /**
     * Runs a given list of tasks asynchronously and in parallel, and returns a CompletableFuture containing a list of results.
     *
     * @param tasks    A list of tasks to execute.
     * @param executor The executor to run the tasks.
     * @param <T>      The type of the results.
     * @return A CompletableFuture containing a list of results.
     */
    public static <T> CompletableFuture<List<T>> runAllAsync(List<Supplier<T>> tasks, Executor executor) {
        List<CompletableFuture<T>> futures = tasks.stream()
                                                  .map(task -> CompletableFuture.supplyAsync(task, executor))
                                                  .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                .thenApply(v -> futures.stream()
                                                       .map(CompletableFuture::join)
                                                       .collect(Collectors.toList()));
    }

    /**
     * Runs a given list of tasks sequentially, one after another.
     *
     * @param tasks    A list of tasks to execute.
     * @param executor The executor to run the tasks.
     * @param <T>      The type of the results.
     * @return A CompletableFuture containing a list of results.
     */
    public static <T> CompletableFuture<List<T>> runSequentially(List<Supplier<T>> tasks, Executor executor) {
        List<T> results = new ArrayList<>(Collections.nCopies(tasks.size(), null));

        return CompletableFuture.completedFuture(null).thenApplyAsync(v -> {
            for (int i = 0; i < tasks.size(); i++) {
                results.set(i, tasks.get(i).get());
            }
            return results;
        }, executor);
    }

    /**
     * Runs a given task and completes it within the given timeout.
     *
     * @param task     The task to execute.
     * @param timeout  The timeout duration.
     * @param timeUnit The time unit of the timeout duration.
     * @param executor The executor to run the task.
     * @param <T>      The type of the result.
     * @return A CompletableFuture containing the result or null if the timeout occurs.
     */
    public static <T> CompletableFuture<T> runWithTimeout(Supplier<T> task, long timeout, TimeUnit timeUnit, Executor executor) {
        CompletableFuture<T> result = CompletableFuture.supplyAsync(task, executor);
        CompletableFuture<T> timeoutFuture = CompletableFuture.supplyAsync(() -> {
            try {
                timeUnit.sleep(timeout);
            } catch (InterruptedException ignored) {
            }
            return null;
        }, executor);

        return CompletableFuture.anyOf(result, timeoutFuture).thenApplyAsync(future -> {
            if (future == result) {
                return result.join();
            }
            return null;
        }, executor);
    }

    /**
     * Creates a CompletableFuture that completes when a JavaFX button is pressed and executes a custom action.
     *
     * @param button   The JavaFX Button to listen for presses.
     * @param action   Custom action to be executed when button is pressed.
     * @param executor Executor to run tasks.
     * @return CompletableFuture<Void> that completes when the button is pressed.
     */
    public static CompletableFuture<Void> futureFromButtonPress(Button button, Consumer<ActionEvent> action, Executor executor) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        button.setOnAction(event -> {
            action.accept(event);
            future.complete(null);
        });
        return future;
    }

    /**
     * Creates a CompletableFuture that completes when a JavaFX button is pressed and returns a single result.
     *
     * @param button   The JavaFX Button to listen for presses.
     * @param task     Task (Supplier) to execute when the button is pressed.
     * @param executor Executor to run the task.
     * @param <T>      Type of the result produced by the task.
     * @return CompletableFuture<T> that completes with the result when the button is pressed.
     */
    public static <T> CompletableFuture<T> futureFromButtonPressWithTask(Button button, Supplier<T> task, Executor executor) {
        CompletableFuture<Void> buttonFuture = new CompletableFuture<>();
        button.setOnAction(event -> buttonFuture.complete(null));
        return buttonFuture.thenApplyAsync(v -> task.get(), executor);
    }

    /**
     * Creates a CompletableFuture that completes when a JavaFX button is pressed and returns a list of results.
     *
     * @param button   The JavaFX Button to listen for presses.
     * @param tasks    List of tasks (Suppliers) to execute when the button is pressed.
     * @param executor Executor to run tasks.
     * @param <T>      Type of the result produced by tasks.
     * @return CompletableFuture<List<T>> that completes with the list of results when the button is pressed.
     */
    public static <T> CompletableFuture<List<T>> futureFromButtonPressWithTasks(Button button, List<Supplier<T>> tasks, Executor executor) {
        CompletableFuture<Void> buttonFuture = new CompletableFuture<>();
        button.setOnAction(event -> buttonFuture.complete(null));
        return buttonFuture.thenComposeAsync(v -> runAllAsync(tasks, executor), executor);
    }
}
