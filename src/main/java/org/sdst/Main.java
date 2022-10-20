package org.sdst;

import org.sdst.enums.MultithreadingPatterns;
import org.sdst.models.Clan;
import org.sdst.services.ClanService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final String delimiter = "========================================";
    private static final List<Clan> clans = Clan.getClans();
    private static int iterations = 1000;
    private static int iterationsEndpoint = iterations * 1;//00;
    private static List<Integer> generatedValues;
    private static ExecutorService executorService;
    private static HashMap<MultithreadingPatterns, Long> executionTime = new HashMap<>();
    private static boolean useGenerated = true; //spicy feelings inc

    public static void main(String[] args) {
        if(useGenerated)
            generateValues();

        //high-load multithreading test
        while(iterations <= iterationsEndpoint) {
            for(MultithreadingPatterns pattern : MultithreadingPatterns.values()) {
                try {
                    doTask(pattern);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            //I want to compare and show results depended on execution time each-to-each
            for(MultithreadingPatterns pattern : executionTime.keySet()) {
                for(MultithreadingPatterns comparablePattern : executionTime.keySet()) {
                    if(pattern == comparablePattern)
                        continue;

                    report(pattern, comparablePattern);
                }

                System.out.println(delimiter);
            }

            //preparation for the new high-load iteration
            resetClanBalance();

            iterations *= 10;
        }
    }

    private static void generateValues() {
        Random random = new Random();
        generatedValues = new ArrayList<>();

        for(int i = 0; i < iterationsEndpoint; i++)
            generatedValues.add(random.nextInt(1000));
    }

    private static void resetClanBalance() {
        for (Clan clan : clans) {
            ClanService.getClanLogs(clan);

            synchronized (clan) {
                clan.setBalance(0);
                clan.setAtomicBalance(0);
            }
        }
    }

    //compare tool
    private static void report(MultithreadingPatterns p1, MultithreadingPatterns p2) {
        double p1val = executionTime.get(p1).doubleValue();
        double p2val = executionTime.get(p2).doubleValue();

        System.out.printf("%-22s: %.2f\n",
                p1 + " (time = " + p1val + ") / " +
                p2 + " (time = " + p2val + ") ",
                p1val/p2val);
    }

    private static void doTask(MultithreadingPatterns pattern) throws InterruptedException {
        printTime(true);

        if(pattern != MultithreadingPatterns.ANONYMOUS_RUNNABLE) {
            executorService = Executors.newCachedThreadPool();
        }

        long startTime = System.currentTimeMillis();
        
        switch (pattern) {
            case ANONYMOUS_RUNNABLE ->
                    caseAnonymousRunnable();
            case EXECUTOR_SERVICE ->
                    caseExecutorService();
            case ATOMIC_INTEGER ->
                    caseAtomicInteger();
            case REENTRANT_LOCKS ->
                    caseReentrantLocks();
        }
        
        printTime(false);

        System.out.println("Pattern: " + pattern);
        System.out.println("it took " + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println(delimiter);

        executionTime.put(pattern, (System.currentTimeMillis() - startTime));

        for(Clan clan : clans) {
            System.out.println("ID: " +
                    String.format("%1$03d", clan.getId()) +
                    "; name: " + clan.getName() +
                    "; balance: " + clan.getBalance() +
                    "; atomic: " + clan.getAtomicBalance());
        }

        System.out.println(delimiter);
    }

    private static void printTime(boolean withDate) {
        ZonedDateTime preset = Instant
                .ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault());

        System.out.println(delimiter);
        System.out.println(withDate ? preset.toLocalDateTime() : preset.toLocalTime());
        System.out.println(delimiter);
    }

    //synchronizes the addBalance() method
    private static void caseAnonymousRunnable() throws InterruptedException {
        for(Clan clan : clans) {

            Thread clanProcessor = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    //according to the task, many users can push gold to the clan balance at same time, so
                    //we're emulating actions called by users
                    int genVal = useGenerated ? generatedValues.get(j) : 1;

                    Thread addBalanceTask = new Thread(() -> {
                        synchronized (clan) {
                            clan.addBalance(genVal);
                        }
                    });
                    addBalanceTask.start();
                }
            });
            clanProcessor.start();
            clanProcessor.join();
        }
    }

    private static void caseExecutorService() throws InterruptedException {
        for(Clan clan : clans) {
            executorService.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    int genVal = useGenerated ? generatedValues.get(j) : 1;

                    Thread addBalanceTask = new Thread(() -> {
                        synchronized (clan) {
                            clan.addBalance(genVal);
                        }
                    });

                    addBalanceTask.start();
                    try {
                        addBalanceTask.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(150, TimeUnit.SECONDS);
    }

    private static void caseAtomicInteger() throws InterruptedException {
        for (Clan clan : clans) {
            executorService.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    int genVal = useGenerated ? generatedValues.get(j) : 1;

                    Thread addBalanceTask = new Thread(() -> clan.addAtomicBalance(genVal));

                    addBalanceTask.start();
                    try {
                        addBalanceTask.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(150, TimeUnit.SECONDS);

        for(Clan clan : clans) {
            synchronized (clan) {
                clan.addBalance(clan.getAtomicBalance().intValue());
            }
        }
    }

    private static void caseReentrantLocks() throws InterruptedException {
        HashMap<Clan, Lock> lockMap = new HashMap<>();

        for (Clan clan : clans) {
            lockMap.put(clan, new ReentrantLock());

            executorService.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    int genVal = useGenerated ? generatedValues.get(j) : 1;

                    Thread addBalanceTask = new Thread(() -> {
                        lockMap.get(clan).lock();
                        try{
                            clan.addBalance(genVal);
                        } finally {
                            lockMap.get(clan).unlock();
                        }
                    });

                    addBalanceTask.start();
                    try {
                        addBalanceTask.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(150, TimeUnit.SECONDS);
    }
}