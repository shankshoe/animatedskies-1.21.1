package com.animatedskies;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnimatedSkies implements ModInitializer {
	public static final String MOD_ID = "animatedskies";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {
		LOGGER.info("Painting the skies, 20 frames at a time");
	
	}
}