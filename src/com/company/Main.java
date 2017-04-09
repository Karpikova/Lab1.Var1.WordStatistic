package com.company;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        ResultStatistics resultStatistics = new ResultStatistics();
        int countOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(countOfThreads);
        for (String fileName:
             args) {
            WordStatistic reading = new WordStatistic(fileName, resultStatistics);
            Thread readingThread = new Thread(reading);
            executor.execute(readingThread);
            WordStatistic.PrintStatistics(resultStatistics);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        WordStatistic.PrintStatistics(resultStatistics);
    }
}
