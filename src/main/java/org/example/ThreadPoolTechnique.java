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

        // Dodawanie nazw miast do listy
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
        // Tworzenie puli wątków
        ExecutorService executor = Executors.newCachedThreadPool();

        // Profilowanie: stan wątków i obciążenie CPU przed wykonaniem
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long[] threadIdsBefore = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfosBefore = threadBean.getThreadInfo(threadIdsBefore);

        double cpuLoadBefore = osBean.getSystemCpuLoad();

        // Wątek monitorujący
        Thread monitoringThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    double currentCpuLoad = osBean.getSystemCpuLoad() * 100;
                    long[] threadIds = threadBean.getAllThreadIds();
                    ThreadInfo[] infos = threadBean.getThreadInfo(threadIds);

                    int runnable = 0, blocked = 0, waiting = 0, timedWaiting = 0;

                    for (ThreadInfo info : infos) {
                        if (info == null) continue;
                        switch (info.getThreadState()) {
                            case RUNNABLE -> runnable++;
                            case BLOCKED -> blocked++;
                            case WAITING -> waiting++;
                            case TIMED_WAITING -> timedWaiting++;
                            default -> {}
                        }
                    }

                    System.out.printf("[Monitor] CPU: %.2f%% | Threads - RUNNABLE: %d, BLOCKED: %d, WAITING: %d, TIMED_WAITING: %d%n",
                            currentCpuLoad, runnable, blocked, waiting, timedWaiting);

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                // Monitoring zakończony
            }
        });
        monitoringThread.setDaemon(true); // Nie blokuje zakończenia JVM
        monitoringThread.start();

        long startTime = System.nanoTime();

        // Przesyłanie zadań do puli wątków
        for (String cityN : cityNames) {
            Runnable task = new TemperatureApiCall(cityN, temperatureResults);
            executor.submit(task);
        }

        System.out.println("Wszystkie zadania przesłane. Oczekiwanie na zakończenie...");
        executor.shutdown();

        // Oczekiwanie na zakończenie wszystkich zadań
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
        monitoringThread.interrupt(); // Zakończenie monitoringu po zakończeniu puli

        System.out.println("Pula wątków zakończyła pracę.");

        // Profilowanie: stan wątków i obciążenie CPU po wykonaniu
        long[] threadIdsAfter = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfosAfter = threadBean.getThreadInfo(threadIdsAfter);

        double cpuLoadAfter = osBean.getSystemCpuLoad();

        // Obliczanie średniej temperatury na podstawie uzyskanych wyników
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

        // Wyświetlanie wyników profilowania
        System.out.println("\n=== Profiling Summary ===");

        System.out.printf("Całkowity czas wykonania (podejście ExecutorService): %.3f s%n", durationInSeconds);
        System.out.printf("Obciążenie CPU przed wykonaniem: %.2f%%%n", cpuLoadBefore * 100);
        System.out.printf("Obciążenie CPU po wykonaniu: %.2f%%%n", cpuLoadAfter * 100);

        int newThreadsCreated = threadIdsAfter.length - threadIdsBefore.length;
        System.out.println("Liczba wątków przed: " + threadIdsBefore.length);
        System.out.println("Liczba wątków po: " + threadIdsAfter.length);

        // Liczenie stanów wątków
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

        System.out.println("Stany wątków po wykonaniu:");
        System.out.println("Runnable: " + runnableCount);
        System.out.println("Blocked: " + blockedCount);
        System.out.println("Waiting: " + waitingCount);
        System.out.println("Timed Waiting: " + timedWaitingCount);
        System.out.println("Terminated: " + terminatedCount);
    }
}