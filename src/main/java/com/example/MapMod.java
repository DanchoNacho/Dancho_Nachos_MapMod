package com.example;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class MapMod implements ModInitializer {
	public static final String MOD_ID = "mapmod";


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

		ServerLivingEntityEvents.AFTER_DEATH.register(
				(entity, damageSource) -> {

					if (!(entity instanceof VillagerEntity)) {
						return;
					}

					if (!(damageSource.getAttacker()
							instanceof ServerPlayerEntity player)) {
						return;
					}

					BountyManager.addBounty(
							player.getUuid(),
							10
					);
				}
		);
	}
}
