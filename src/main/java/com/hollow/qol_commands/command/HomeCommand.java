package com.hollow.qol_commands.command;

import com.hollow.qol_commands.data_storage.HomeEntityDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import com.hollow.qol_commands.data_storage.HomeEntityData;

public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("home")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    if (player == null) {
                        context.getSource().sendError(Text.of("Player Null!"));
                        return 1;
                    }


                    Vec3d coords = returnHome(player, context);
                    String returnString = coords != null ? "Returned to home at: " + coords : "Unable to go home...";
                    context.getSource().sendFeedback(() -> Text.literal(returnString), false);

                    return 1;
                })
                .then(CommandManager.literal("set")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();

                            if (player == null) {
                                context.getSource().sendError(Text.of("Player Null!"));
                                return -1;
                            }

                            Vec3d coords = setHome(player);
                            context.getSource().sendFeedback(() -> Text.literal("Set home at: " + coords), false);

                            return 1;
                        })
                )
        );
    }

    private static Vec3d setHome(ServerPlayerEntity player) {
        if(!player.getWorld().isClient) {
            ServerWorld world = (ServerWorld) player.getWorld();
            HomeEntityData storage = HomeEntityDataManager.getState(world);

            BlockPos playerPos = player.getBlockPos();
            storage.setHome(player.getUuid(), playerPos);

            return new Vec3d(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        }

        return null;
    }

    private static Vec3d returnHome(ServerPlayerEntity player, CommandContext<ServerCommandSource> context) {
        ServerWorld world = (ServerWorld) player.getWorld();
        HomeEntityData storage = HomeEntityDataManager.getState(world);

        BlockPos homePos = storage.getHome(player.getUuid());

        if (homePos != null) {
            player.teleport(homePos.getX(), homePos.getY(), homePos.getZ(), false);
            return new Vec3d(homePos.getX(), homePos.getY(), homePos.getZ());
        } else {
            context.getSource().sendError(Text.of("Player has not set home yet!"));
            return null;
        }
    }
}
