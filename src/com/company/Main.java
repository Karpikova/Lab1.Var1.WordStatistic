package com.company;

import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        ResultStatistics resultStatistics = new ResultStatistics();
        Set<Thread> allThreads = new HashSet<>();
        for (String fileName:
             args) {
            WordStatistic reading = new WordStatistic(fileName, resultStatistics);
            Thread readingThread = new Thread(reading);
            readingThread.start();
            allThreads.add(readingThread);
        }
        for (Thread thread:
                allThreads) {
            thread.join();
            WordStatistic.PrintStatistics(resultStatistics);
        }
    }
}
