package com.mishkis.orbitalrailgun.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.concurrent.ConcurrentHashMap;

public class ClientStrikeEffectManager {
    private static final ConcurrentHashMap<BlockPos, StrikeEffect> activeStrikes = new ConcurrentHashMap<>();
    
    public static void addStrike(BlockPos pos, ResourceKey<Level> dimension) {
        activeStrikes.put(pos, new StrikeEffect(pos, dimension, System.currentTimeMillis()));
    }
    
    public static void tick() {
        long currentTime = System.currentTimeMillis();
        activeStrikes.entrySet().removeIf(entry -> {
            StrikeEffect effect = entry.getValue();
            // Remove strikes after 80 seconds (1600 ticks * 50ms = 80000ms)
            return currentTime - effect.startTime > 80000;
        });
    }
    
    public static StrikeEffect getCurrentStrike(ResourceKey<Level> dimension) {
        return activeStrikes.values().stream()
                .filter(effect -> effect.dimension.equals(dimension))
                .findFirst()
                .orElse(null);
    }
    
    public static class StrikeEffect {
        public final BlockPos position;
        public final ResourceKey<Level> dimension;
        public final long startTime;
        public final Vector3f positionVector;
        
        public StrikeEffect(BlockPos position, ResourceKey<Level> dimension, long startTime) {
            this.position = position;
            this.dimension = dimension;
            this.startTime = startTime;
            this.positionVector = Vec3.atCenterOf(position).toVector3f();
        }
        
        public float getAgeInTicks(long currentTime) {
            return (currentTime - startTime) / 50.0f; // Convert ms to ticks (50ms per tick)
        }
    }
}