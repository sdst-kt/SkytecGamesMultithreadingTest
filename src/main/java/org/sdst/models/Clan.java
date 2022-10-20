package org.sdst.models;

import org.sdst.dao.ClanDAO;
import org.sdst.enums.GoldSource;
import org.sdst.services.ClanService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Clan {
    private static int globalIdentifier;
    private int id;
    private int balance;
    private AtomicInteger atomicBalance = new AtomicInteger(0);
    private String name;
    private static List<Clan> clans;
    private static final Random random = new Random();

    public Clan (String name) {
        this.id             = ++globalIdentifier;
        this.name           = name;
        this.balance        = 0;
    }


    public Clan() {
        this("");
    }

    private void onChange(int value) {
        ClanService.update(this);
        ClanService.log(this, GoldSource.values()[random.nextInt(GoldSource.values().length)], value);
    }

    public static List<Clan> getClans() {
        clans = ClanService.getAll();

        if (clans == null || clans.isEmpty()) {
            fillClans();
        }

        return clans;
    }

    private static void fillClans() {
        clans = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            Clan clan = new Clan("ClanName_" + i);

            ClanService.save(clan);

            clans.add(clan);
        }
    }

    public synchronized int getBalance() {
        return balance;
    }

    public void setBalance(int value) {
        this.balance = value;;

        onChange(value);
    }

    //it has no synchronize cause I want to use it at outer code
    public void addBalance(int value) {
        this.balance += value;

        onChange(value);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AtomicInteger getAtomicBalance() {
        return atomicBalance;
    }

    public void setAtomicBalance(int value) {
        atomicBalance.set(value);

        onChange(value);
    }

    public void addAtomicBalance(int value) {
        atomicBalance.addAndGet(value);

        onChange(value);
    }
}
