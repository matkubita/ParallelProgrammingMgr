package org.example;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolTechnique {

    public static void main(String[] args) {

        List<String> cityNames = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cityNames.add("Austin");
            cityNames.add("Houston");
            cityNames.add("Dallas");
            cityNames.add("Jacksonville");
            cityNames.add("Indianapolis");
            cityNames.add("Charlotte");
            cityNames.add("Washington");
            cityNames.add("Seattle");
            cityNames.add("Chicago");
            cityNames.add("Phoenix");
            cityNames.add("Philadelphia");
            cityNames.add("Denver");
            cityNames.add("Albuquerque");
            cityNames.add("Tucson");
            cityNames.add("Fresno");
            cityNames.add("Mesa");
            cityNames.add("Sacramento");
            cityNames.add("Atlanta");
            cityNames.add("Miami");
        }

        ConcurrentLinkedQueue<Double> temperatureResults = new ConcurrentLinkedQueue<>();
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Profiler: Thread state and CPU usage
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long[] threadIdsBefore = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfosBefore = threadBean.getThreadInfo(threadIdsBefore);

        double cpuLoadBefore = osBean.getSystemCpuLoad(); // Between 0.0 and 1.0

        System.out.println("Przesyłanie zadań do puli wątków (rozmiar puli: " + numberOfThreads + ")...");
        long startTime = System.nanoTime();

        for (String cityN : cityNames) {
            Runnable task = new TemperatureApiCall(cityN, temperatureResults);
            executor.submit(task);
        }

        System.out.println("Wszystkie zadania przesłane. Oczekiwanie na zakończenie...");
        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                System.err.println("Pula wątków nie zakończyła pracy w wyznaczonym czasie!");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Oczekiwanie na zakończenie puli przerwane!");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long endTime = System.nanoTime();
        System.out.println("Pula wątków zakończyła pracę.");

        // Profiler after execution
        long[] threadIdsAfter = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfosAfter = threadBean.getThreadInfo(threadIdsAfter);

        double cpuLoadAfter = osBean.getSystemCpuLoad();

        double sum = 0;
        int count = 0;
        for (Double temp : temperatureResults) {
            if (temp != null) {
                sum += temp;
                count++;
            }
        }
        double average = sum / count;

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.printf("\nUdało się pobrać %d wyników.%n", count);
        System.out.printf("Średnia temperatura: %.2f °C%n", average);

        // Display profiling results
        System.out.println("\n=== Profiling Summary ===");

        System.out.printf("Całkowity czas wykonania (podejście ExecutorService): %.3f s%n", durationInSeconds);
        System.out.printf("CPU load before: %.2f%%%n", cpuLoadBefore * 100);
        System.out.printf("CPU load after: %.2f%%%n", cpuLoadAfter * 100);

        int newThreadsCreated = threadIdsAfter.length - threadIdsBefore.length;
        System.out.println("Number of threads before: " + threadIdsBefore.length);
        System.out.println("Number of threads after: " + threadIdsAfter.length);
        System.out.println("New threads created during execution: " + newThreadsCreated);

        int runnableCount = 0;
        int blockedCount = 0;
        int waitingCount = 0;
        int timedWaitingCount = 0;
        int terminatedCount = 0;

        for (ThreadInfo info : threadInfosAfter) {
            if (info == null) continue;
            switch (info.getThreadState()) {
                case RUNNABLE -> runnableCount++;
                case BLOCKED -> blockedCount++;
                case WAITING -> waitingCount++;
                case TIMED_WAITING -> timedWaitingCount++;
                case TERMINATED -> terminatedCount++;
                default -> {}
            }
        }

        System.out.println("Thread States After Execution:");
        System.out.println("Runnable: " + runnableCount);
        System.out.println("Blocked: " + blockedCount);
        System.out.println("Waiting: " + waitingCount);
        System.out.println("Timed Waiting: " + timedWaitingCount);
        System.out.println("Terminated: " + terminatedCount);
    }
}
