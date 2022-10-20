package org.sdst.services;

import org.sdst.dao.ClanDAO;
import org.sdst.enums.GoldSource;
import org.sdst.models.Clan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ClanService {
    static Optional<Clan> getItem(int id) {
        Optional<Clan> clan = null;

        try {
            clan = ClanDAO.getItem(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clan;
    }

    static List<Clan> getAll() {
        List<Clan> clans = new ArrayList<>();
        try {
             clans = ClanDAO.get();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clans;
    }

    static void save(Clan clan) {
        try {
            ClanDAO.save(clan);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void update(Clan clan) {
        try {
            ClanDAO.update(clan);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void log(Clan clan, GoldSource source, int goldAdded) {
        try {
            ClanDAO.log(clan, source, goldAdded);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void getClanLogs(Clan clan) {
        List<String> logs = new ArrayList<>();

        try {
            logs = ClanDAO.getLogs(clan);

            for(String log : logs)
                System.out.println(log);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
