package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TutorialModClient implements ClientModInitializer {

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {

        // Print message when joining a world
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register(
                (handler, sender, client) -> {
                    client.inGameHud.getChatHud().addMessage(
                            Text.literal("It Works!")
                    );
                }
        );

        // Register M key
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tutorialmod.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.tutorialmod"
        ));

        // M toggles the map
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {

                if (client.currentScreen instanceof MapScreen) {
                    // Map is open -> close it
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    // No screen open -> open map
                    client.setScreen(new MapScreen());
                }
            }
        });
    }
}
