package org.example;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadTechnique {
    public static void main(String[] args) {

        List<String> cityNames = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

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

        System.out.println("Uruchamianie wątków...");
        long startTime = System.nanoTime();

        for (String cityN : cityNames) {
            Runnable task = new TemperatureApiCall(cityN, temperatureResults);
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Wszystkie wątki zakończone.");

        double sum = 0;
        int count = 0;

        for (Double temp : temperatureResults) {
            sum += temp;
            count++;
        }
        System.out.println(sum/count);

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("Całkowity czas wykonania (podejście klasyczne): %.3f s%n", durationInSeconds);


    }
}
