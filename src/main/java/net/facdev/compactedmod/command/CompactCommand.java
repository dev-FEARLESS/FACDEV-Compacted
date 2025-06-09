package net.facdev.compactedmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.facdev.compactedmod.data.PlayerCompactData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class CompactCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("compact")
                .then(Commands.literal("create")
                        .executes(CompactCommand::executeCreate))
                .then(Commands.literal("go")
                        .executes(CompactCommand::executeGo))
        );
    }

    private static int executeCreate(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        ServerLevel level = player.serverLevel();

        // Check if player already has a compact cube
        PlayerCompactData data = new PlayerCompactData(player);
        if (data.hasCompactCube()) {
            source.sendFailure(Component.literal("You already have a compact cube! Use /compact go to return to it."));
            return 0;
        }

        // Generate random position (avoid spawn area and stay reasonable)
        Random random = new Random();
        int x = random.nextInt(20000) - 10000; // -10k to +10k
        int z = random.nextInt(20000) - 10000; // -10k to +10k
        int y = random.nextInt(100) + 50; // Y 50-150 for reasonable height

        BlockPos cubePos = new BlockPos(x, y, z);

        // Create the hollow glass cube
        createGlassCube(level, cubePos);

        // Save the cube position for this player
        data.setCompactCubePosition(cubePos);

        // Teleport player to center of cube
        player.teleportTo(
                level,
                cubePos.getX() + 0.5,
                cubePos.getY() + 0.5,
                cubePos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );

        // Set to survival mode
        player.setGameMode(GameType.SURVIVAL);

        source.sendSuccess(() -> Component.literal("Compact cube created at " + cubePos.getX() + ", " + cubePos.getY() + ", " + cubePos.getZ() + "!"), false);

        return 1;
    }

    private static int executeGo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        PlayerCompactData data = new PlayerCompactData(player);
        BlockPos cubePos = data.getCompactCubePosition();

        if (cubePos == null) {
            source.sendFailure(Component.literal("You don't have a compact cube! Use /compact create first."));
            return 0;
        }

        ServerLevel level = player.serverLevel();

        // Teleport player back to their cube
        player.teleportTo(
                level,
                cubePos.getX() + 0.5,
                cubePos.getY() + 0.5,
                cubePos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );

        // Set to survival mode
        player.setGameMode(GameType.SURVIVAL);

        source.sendSuccess(() -> Component.literal("Teleported back to your compact cube!"), false);

        return 1;
    }

    private static void createGlassCube(ServerLevel level, BlockPos center) {
        // Create hollow 5x5x5 glass cube
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    // Only place glass on the outer shell (hollow inside)
                    if (x == -2 || x == 2 || y == -2 || y == 2 || z == -2 || z == 2) {
                        level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
                    } else {
                        // Ensure inside is air
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Force lighting update
        updateLighting(level, center);
    }

    private static void updateLighting(ServerLevel level, BlockPos center) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    level.getChunkSource().getLightEngine().checkBlock(pos);
                }
            }
        }
    }
}