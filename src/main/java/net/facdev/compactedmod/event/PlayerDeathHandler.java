package net.facdev.compactedmod.event;

import net.facdev.compactedmod.data.PlayerCompactData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerDeathHandler {

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerCompactData data = new PlayerCompactData(player);

        // Only handle respawn if player has a compact cube
        if (!data.hasCompactCube()) return;

        ServerLevel level = player.serverLevel();
        BlockPos spawnRoomPos = data.getSpawnRoomPosition();

        // Teleport player back to spawn room
        player.teleportTo(
                level,
                spawnRoomPos.getX() + 0.5,
                spawnRoomPos.getY() + 0.5,
                spawnRoomPos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );

        // Set to adventure mode (they're "dead" until they use /compact go)
        player.setGameMode(GameType.ADVENTURE);

        // Mark them as in death state
        data.setDeathState(true);

        System.out.println("Player " + player.getName().getString() + " died and was returned to spawn room");
    }
}