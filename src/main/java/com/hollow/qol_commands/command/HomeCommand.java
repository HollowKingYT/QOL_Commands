package com.hollow.qol_commands.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HomeCommand {
    private static BlockPos pos = null;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("home")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    if (player == null) {
                        context.getSource().sendError(Text.of("Player Null!"));
                        return 1;
                    }

                    if (pos == null) {
                        context.getSource().sendError(Text.of("Home not set yet!"));
                        return 1;
                    }

                    Vec3d coords = returnHome(player);
                    context.getSource().sendFeedback(() -> Text.literal("Returned to home at: " + coords), false);

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
        pos = player.getBlockPos();


        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    private static Vec3d returnHome(ServerPlayerEntity player) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        player.teleport(x, y, z, false);

        return new Vec3d(x, y, z);
    }
}
