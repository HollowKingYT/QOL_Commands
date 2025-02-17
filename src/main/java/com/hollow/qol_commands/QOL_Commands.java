package com.hollow.qol_commands;

import com.hollow.qol_commands.command.ModCommands;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QOL_Commands implements ModInitializer {
	public static final String MOD_ID = "qol_commands";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Registering Commands...!");
		ModCommands.registerCommands();
	}
}