package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreditsManager {

    private static final Map<UUID, Double> credits =
            new HashMap<>();

    public static double getCredits(UUID playerId) {

        return credits.getOrDefault(
                playerId,
                0.0
        );
    }

    public static void setCredits(
            UUID playerId,
            double amount
    ) {

        credits.put(
                playerId,
                amount
        );
    }

    public static void addCredits(
            UUID playerId,
            double amount
    ) {

        setCredits(
                playerId,
                getCredits(playerId) + amount
        );
    }

    public static void removeCredits(
            UUID playerId,
            double amount
    ) {

        setCredits(
                playerId,
                Math.max(
                        0.0,
                        getCredits(playerId) - amount
                )
        );
    }
}