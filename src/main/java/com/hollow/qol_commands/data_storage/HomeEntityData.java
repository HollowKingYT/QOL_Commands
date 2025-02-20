package com.hollow.qol_commands.data_storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player home locations in a Minecraft world using Fabric's PersistentState system.
 * <p>
 * This class stores and retrieves home locations for players, ensuring the data is persistent
 * across world saves and reloads.
 * </p>
 */
public class HomeEntityData extends PersistentState {
    /**
     * Stores home locations, mapping a player's UUID to their home position.
     */
    private final HashMap<UUID, BlockPos> homeLocations = new HashMap<>();

    /**
     * The PersistentState type for Fabric, handling instance creation and NBT serialization.
     */
    public static final Type<HomeEntityData> TYPE = new Type<>(
            HomeEntityData::new,       // Creates a new empty instance
            HomeEntityData::createFromNbt, // Loads from NBT data
            null // No registry deserialization needed
    );

    /**
     * Default constructor. Required for Fabric's PersistentState system.
     */
    public HomeEntityData() {}

    /**
     * Loads a HomeEntityData instance from NBT data.
     *
     * @param nbt The NBT compound containing saved home locations.
     * @param registries The game's registry lookup (unused in this case).
     * @return A new HomeEntityData instance populated with stored home locations.
     */
    public static HomeEntityData createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        HomeEntityData state = new HomeEntityData();
        NbtList list = nbt.getList("HomeLocations", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            UUID uuid = entry.getUuid("UUID");
            BlockPos pos = new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
            state.homeLocations.put(uuid, pos);
        }
        return state;
    }

    /**
     * Saves home location data to NBT format for persistence.
     *
     * @param nbt The NBT compound to write data to.
     * @param registries The game's registry lookup (unused in this case).
     * @return The modified NBT compound containing saved home locations.
     */
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, BlockPos> entry : homeLocations.entrySet()) {
            NbtCompound compound = new NbtCompound();
            compound.putUuid("UUID", entry.getKey());
            compound.putInt("X", entry.getValue().getX());
            compound.putInt("Y", entry.getValue().getY());
            compound.putInt("Z", entry.getValue().getZ());
            list.add(compound);
        }
        nbt.put("HomeLocations", list);
        return nbt;
    }

    /**
     * Sets or updates a player's home location.
     *
     * @param playerId The UUID of the player.
     * @param pos The BlockPos representing the home location.
     */
    public void setHome(UUID playerId, BlockPos pos) {
        homeLocations.put(playerId, pos);
        markDirty(); // Marks the state as changed, so it gets saved
    }

    /**
     * Retrieves a player's home location.
     *
     * @param playerId The UUID of the player.
     * @return The BlockPos of the player's home, or null if not set.
     */
    public BlockPos getHome(UUID playerId) {
        return homeLocations.get(playerId);
    }

    /**
     * Removes a player's home location.
     *
     * @param playerId The UUID of the player.
     */
    public void removeHome(UUID playerId) {
        homeLocations.remove(playerId);
        markDirty(); // Marks the state as changed, so it gets saved
    }

    /**
     * Checks if a player has a home location set.
     *
     * @param playerId The UUID of the player.
     * @return True if the player has a home location, false otherwise.
     */
    public boolean hasHome(UUID playerId) {
        return homeLocations.containsKey(playerId);
    }
}
