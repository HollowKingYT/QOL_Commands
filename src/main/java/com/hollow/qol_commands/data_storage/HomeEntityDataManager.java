package com.hollow.qol_commands.data_storage;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateManager;

public class HomeEntityDataManager {
    public static final String STATE_KEY = "HomeLocations";

    public static HomeEntityData getState(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        return manager.getOrCreate(HomeEntityData.TYPE, STATE_KEY);
    }
}
