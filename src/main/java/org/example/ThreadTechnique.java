package org.example;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadTechnique {

    public static void main(String[] args) {

        List<String> cityNames = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        // Dodawanie nazw miast do listy
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

        // Profilowanie: stan CPU i wątków PRZED wykonaniem
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long[] threadIdsBefore = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfosBefore = threadBean.getThreadInfo(threadIdsBefore);

        double cpuLoadBefore = osBean.getSystemCpuLoad();

        // Wątek monitorujący działanie aplikacji
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
                // Monitoring zakończony poprawnie
            }
        });
        monitoringThread.setDaemon(true); // Nie blokuje zakończenia JVM
        monitoringThread.start();


        System.out.println("Uruchamianie wątków...");
        long startTime = System.nanoTime();

        // Tworzenie i uruchamianie nowych wątków dla każdego miasta
        for (String cityN : cityNames) {
            Runnable task = new TemperatureApiCall(cityN, temperatureResults);
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        // Oczekiwanie na zakończenie wszystkich wątków
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Wszystkie wątki zakończone.");
        monitoringThread.interrupt(); // zatrzymanie wątku monitorującego


        // Profilowanie: stan CPU i wątków PO wykonaniu
        long[] threadIdsAfter = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfosAfter = threadBean.getThreadInfo(threadIdsAfter);

        double cpuLoadAfter = osBean.getSystemCpuLoad();

        // Obliczanie średniej temperatury na podstawie wyników
        double sum = 0;
        int count = 0;
        for (Double temp : temperatureResults) {
            sum += temp;
            count++;
        }

        double average = sum / count;
        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.printf("\nUdało się pobrać %d wyników.%n", count);
        System.out.printf("Średnia temperatura: %.2f °C%n", average);

        // Wyświetlanie wyników profilowania
        System.out.println("\n=== Podsumowanie profilowania ===");

        System.out.printf("Całkowity czas wykonania (podejście klasyczne): %.3f s%n", durationInSeconds);
        System.out.printf("Obciążenie CPU przed wykonaniem: %.2f%%%n", cpuLoadBefore * 100);
        System.out.printf("Obciążenie CPU po wykonaniu: %.2f%%%n", cpuLoadAfter * 100);

        int newThreadsCreated = threadIdsAfter.length - threadIdsBefore.length;
        System.out.println("Liczba wątków przed: " + threadIdsBefore.length);
        System.out.println("Liczba wątków po: " + threadIdsAfter.length);

        // Liczenie stanów wątków po wykonaniu
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