package org.example;

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

        System.out.println("Przesyłanie zadań do puli wątków (rozmiar puli: " + numberOfThreads + ")...");
        long startTime = System.nanoTime();

        for (String cityN : cityNames) {
            Runnable task = new TemperatureApiCall(cityN, temperatureResults);
            executor.submit(task);
        }

        System.out.println("Wszystkie zadania przesłane. Oczekiwanie na zakończenie...");
        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) { // Czekaj max 5 minut
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

        double sum = 0;
        int count = 0;

        for (Double temp : temperatureResults) {
            if (temp != null) {
                sum += temp;
                count++;
            }
        }

        double average = sum / count;
        System.out.printf("\nUdało się pobrać %d wyników.%n", count);
        System.out.printf("Średnia temperatura: %.2f °C%n", average);

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("Całkowity czas wykonania (podejście ExecutorService): %.3f s%n", durationInSeconds);
    }
}