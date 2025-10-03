package net.orbitalrailgun.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RailgunState {
    // Strike state (world chain)
    public static Vec3 strikePos = null; // world coordinates
    public static ResourceKey<Level> strikeDimension = null;

    // GUI targeting state
    public static boolean isBlockHit = false;
    public static Vec3 guiBlockPos = null;

    // Time tracking
    private static int ticks = 0;

    public static void tickClient() { ticks++; }
    public static float getTimeSeconds(float partial) { return (ticks + partial) / 20.0f; }

    public static void clearStrike() {
        strikePos = null;
        strikeDimension = null;
    }
}
