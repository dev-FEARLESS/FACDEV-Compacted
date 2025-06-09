package net.facdev.compactedmod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerCompactData {
    private final ServerPlayer player;
    private final CompoundTag data;

    public PlayerCompactData(ServerPlayer player) {
        this.player = player;
        this.data = player.getPersistentData();
    }

    public boolean hasCompactCube() {
        return data.contains("compactCubeX") && data.contains("compactCubeY") && data.contains("compactCubeZ");
    }

    public void setCompactCubePosition(BlockPos pos) {
        data.putInt("compactCubeX", pos.getX());
        data.putInt("compactCubeY", pos.getY());
        data.putInt("compactCubeZ", pos.getZ());
    }

    public BlockPos getCompactCubePosition() {
        if (!hasCompactCube()) {
            return null;
        }

        int x = data.getInt("compactCubeX");
        int y = data.getInt("compactCubeY");
        int z = data.getInt("compactCubeZ");

        return new BlockPos(x, y, z);
    }

    public BlockPos getSpawnRoomPosition() {
        // Return the spawn room position (always at 0, minY+10, 0)
        int minY = player.serverLevel().getMinBuildHeight() + 10;
        return new BlockPos(0, minY, 0);
    }

    public boolean isInDeathState() {
        return data.getBoolean("inDeathState");
    }

    public void setDeathState(boolean inDeathState) {
        data.putBoolean("inDeathState", inDeathState);
    }
}