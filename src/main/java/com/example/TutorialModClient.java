package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;

public class TutorialModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.inGameHud.getChatHud().addMessage(
                    Text.literal("Hello from my mod!")
            );
        });
    }
}

