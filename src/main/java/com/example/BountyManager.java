package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyManager {

    private static final Map<UUID, Integer> bounties =
            new HashMap<>();

    public static int getBounty(UUID playerId) {

        return bounties.getOrDefault(
                playerId,
                0
        );
    }

    public static void setBounty(
            UUID playerId,
            int amount
    ) {

        bounties.put(
                playerId,
                amount
        );
    }

    public static void addBounty(
            UUID playerId,
            int amount
    ) {

        int current =
                getBounty(playerId);

        setBounty(
                playerId,
                current + amount
        );
    }

    public static void removeBounty(
            UUID playerId,
            int amount
    ) {

        int current =
                getBounty(playerId);

        setBounty(
                playerId,
                Math.max(
                        0,
                        current - amount
                )
        );
    }
}