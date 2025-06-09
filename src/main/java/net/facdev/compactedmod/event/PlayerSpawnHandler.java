package net.facdev.compactedmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class PlayerSpawnHandler {

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        CompoundTag data = player.getPersistentData();

        // Check if this is the player's first time joining this world
        if (data.getBoolean("hasSpawned")) return;

        // Find the lowest possible Y position (bedrock level + buffer)
        int minY = level.getMinBuildHeight() + 10; // Usually -64 + 10 = -54
        BlockPos spawnPos = new BlockPos(0, minY, 0);

        // Create the structure at bedrock level
        createTestStructure(level, spawnPos);

        // Force lighting updates for the entire structure area
        updateLighting(level, spawnPos);

        // Set game mode
        player.setGameMode(GameType.ADVENTURE);

        // Teleport player to the CENTER of the innermost 5x5x5 glass cube
        // The glass cube center is at the same position as spawnPos
        player.teleportTo(
                level,
                spawnPos.getX() + 0.5, // Center of block X
                spawnPos.getY() + 0.5, // Center of block Y (not +1, since we want center)
                spawnPos.getZ() + 0.5, // Center of block Z
                player.getYRot(),
                player.getXRot()
        );

        // Mark that the player has spawned
        data.putBoolean("hasSpawned", true);

        System.out.println("Player spawned deep underground in glass cube at: " + spawnPos);
    }

    private void updateLighting(ServerLevel level, BlockPos center) {
        // Force lighting updates for the entire structure area
        // Update a slightly larger area to ensure proper light propagation
        for (int x = -6; x <= 6; x++) {
            for (int y = -6; y <= 6; y++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    level.getChunkSource().getLightEngine().checkBlock(pos);
                }
            }
        }

        // Force lighting recalculation for the affected chunks
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;

        // Update lighting for current chunk and surrounding chunks
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(chunkX + dx, chunkZ + dz);
                if (chunk != null) {
                    chunk.setUnsaved(true);
                }
            }
        }
    }

    private void createTestStructure(ServerLevel level, BlockPos center) {
        // Outer 11x11x11 bedrock box
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    // Outer shell
                    if (x == -5 || x == 5 || y == -5 || y == 5 || z == -5 || z == 5) {
                        level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 3);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Inner 9x9x9 hollow cube - use CompactMachines wall if available
        Block wallBlock = getWallBlock();

        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    // Only surface of the 9x9 cube
                    if (x == -4 || x == 4 || y == -4 || y == 4 || z == -4 || z == 4) {
                        level.setBlock(pos, wallBlock.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Replace center blocks of each face with sea lanterns
        // Top face (y = 4)
        level.setBlock(center.offset(0, 4, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        // Bottom face (y = -4)
        level.setBlock(center.offset(0, -4, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        // North face (z = -4)
        level.setBlock(center.offset(0, 0, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        // South face (z = 4)
        level.setBlock(center.offset(0, 0, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        // West face (x = -4)
        level.setBlock(center.offset(-4, 0, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        // East face (x = 4)
        level.setBlock(center.offset(4, 0, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);

        // Add sea lanterns to corners of visible 7x7 faces
        // Top face visible corners (y = 4)
        level.setBlock(center.offset(-3, 4, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(-3, 4, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, 4, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, 4, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);

        // Bottom face visible corners (y = -4)
        level.setBlock(center.offset(-3, -4, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(-3, -4, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, -4, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, -4, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);

        // North face visible corners (z = -4)
        level.setBlock(center.offset(-3, -3, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(-3, 3, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, -3, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, 3, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11);

        // South face visible corners (z = 4)
        level.setBlock(center.offset(-3, -3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(-3, 3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, -3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(3, 3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11);

        // West face visible corners (x = -4)
        level.setBlock(center.offset(-4, -3, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(-4, -3, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(-4, 3, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(-4, 3, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);

        // East face visible corners (x = 4)
        level.setBlock(center.offset(4, -3, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(4, -3, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(4, 3, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11);
        level.setBlock(center.offset(4, 3, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);

        // Add sea lanterns to cardinal directions of visible 7x7 faces
        // Top face cardinal directions (y = 4)
        level.setBlock(center.offset(0, 4, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11); // North
        level.setBlock(center.offset(3, 4, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // East
        level.setBlock(center.offset(0, 4, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // South
        level.setBlock(center.offset(-3, 4, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11); // West

        // Bottom face cardinal directions (y = -4)
        level.setBlock(center.offset(0, -4, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11); // North
        level.setBlock(center.offset(3, -4, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // East
        level.setBlock(center.offset(0, -4, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // South
        level.setBlock(center.offset(-3, -4, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11); // West

        // North face cardinal directions (z = -4)
        level.setBlock(center.offset(0, 3, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // Up
        level.setBlock(center.offset(3, 0, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // East
        level.setBlock(center.offset(0, -3, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11); // Down
        level.setBlock(center.offset(-3, 0, -4), Blocks.SEA_LANTERN.defaultBlockState(), 11); // West

        // South face cardinal directions (z = 4)
        level.setBlock(center.offset(0, 3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // Up
        level.setBlock(center.offset(3, 0, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // East
        level.setBlock(center.offset(0, -3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11); // Down
        level.setBlock(center.offset(-3, 0, 4), Blocks.SEA_LANTERN.defaultBlockState(), 11); // West

        // West face cardinal directions (x = -4)
        level.setBlock(center.offset(-4, 3, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // Up
        level.setBlock(center.offset(-4, 0, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // South
        level.setBlock(center.offset(-4, -3, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11); // Down
        level.setBlock(center.offset(-4, 0, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11); // North

        // East face cardinal directions (x = 4)
        level.setBlock(center.offset(4, 3, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // Up
        level.setBlock(center.offset(4, 0, 3), Blocks.SEA_LANTERN.defaultBlockState(), 11);  // South
        level.setBlock(center.offset(4, -3, 0), Blocks.SEA_LANTERN.defaultBlockState(), 11); // Down
        level.setBlock(center.offset(4, 0, -3), Blocks.SEA_LANTERN.defaultBlockState(), 11); // North

        // Innermost 5x5x5 hollow glass cube
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    // Only surface of the 5x5 cube
                    if (x == -2 || x == 2 || y == -2 || y == 2 || z == -2 || z == 2) {
                        level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private Block getWallBlock() {
        // Since CompactMachines is a required dependency, it will always be loaded
        ResourceLocation wallId = ResourceLocation.fromNamespaceAndPath("compactmachines", "wall");
        Block wallBlock = BuiltInRegistries.BLOCK.get(wallId);

        // Verify the block actually exists (not just air)
        if (wallBlock != Blocks.AIR) {
            return wallBlock;
        }

        // Fallback just in case (shouldn't happen with required dependency)
        System.err.println("Warning: CompactMachines wall block not found despite required dependency!");
        return Blocks.OBSIDIAN;
    }
}