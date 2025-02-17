package com.hollow.qol_commands.command;

import com.hollow.qol_commands.QOL_Commands;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;

public class CloneEntityCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("cloneEntity")
                .requires(source -> source.hasPermissionLevel(2)) // Require OP level 2
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        context.getSource().sendError(Text.of("Player Null!"));
                    } else {
                        Entity targetEntity = getEntityLookingAt(player); // Check within 20 blocks

                        if (targetEntity != null) {
                            CloneTargetEntity(targetEntity, player);
                            context.getSource().sendFeedback(() -> Text.literal("Cloned entity: " + targetEntity.getType().getName()), false);
                        } else {
                            context.getSource().sendError(Text.of("No entity found!"));
                        }
                    }
                    return 1;
                })
        );
    }

    private static Entity getEntityLookingAt(ServerPlayerEntity player) {
        try {
            Vec3d eyePos = player.getEyePos();
            Vec3d lookVec = player.getRotationVec(1.0F);
            Vec3d endPos = eyePos.add(lookVec.multiply(20));

            // Debug message
            System.out.println("[DEBUG] Performing entity raycast...");

            EntityHitResult entityHit = raycastEntities(player, eyePos, endPos);

            if (entityHit != null) {
                System.out.println("[DEBUG] Entity found: " + entityHit.getEntity().getType().getName());
                return entityHit.getEntity();
            }

            return null;
        } catch (Exception e) {
            QOL_Commands.LOGGER.info("[DEBUG] error at");
            e.printStackTrace();
            return null;
        }
    }

    private static EntityHitResult raycastEntities(ServerPlayerEntity player, Vec3d start, Vec3d end) {
        Entity closestEntity = null;
        double closestDistance = 20;
        Vec3d hitPos = null;

        for (Entity entity : player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(20))) {
            if (entity.isSpectator()) continue;

            // Debug entity positions
            System.out.println("[DEBUG] Checking entity: " + entity.getType().getName());

            Box entityBox = entity.getBoundingBox().expand(0.1);
            Optional<Vec3d> optionalHit = entityBox.raycast(start, end);

            if (optionalHit.isPresent()) {
                double distance = start.squaredDistanceTo(optionalHit.get());

                if (distance < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distance;
                    hitPos = optionalHit.get();
                }
            }
        }

        return closestEntity != null ? new EntityHitResult(closestEntity, hitPos) : null;
    }

    private static void CloneTargetEntity(Entity targetEntity, ServerPlayerEntity player) {
        if (!player.getWorld().isClient) {
            // Copy NBT data from the original entity
            NbtCompound nbt = new NbtCompound();
            targetEntity.saveNbt(nbt);

            // Ensure the correct entity type
            EntityType<?> entityType = targetEntity.getType();
            ServerWorld world = (ServerWorld) player.getWorld();

            // Create the cloned entity
            Entity clonedEntity = entityType.spawn(world, player.getBlockPos(), SpawnReason.COMMAND);

            if (clonedEntity != null) {
                // Load the saved NBT into the cloned entity
                clonedEntity.readNbt(nbt);

                // Set its position near the player
                clonedEntity.setPos(player.getX(), player.getY(), player.getZ());

                // Spawn the cloned entity in the world
                world.spawnEntity(clonedEntity);
            } else {
                player.sendMessage(Text.of("Failed to clone entity."), false);
            }
        }
    }

}
