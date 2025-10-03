package com.mishkis.orbitalrailgun.util;

import com.mishkis.orbitalrailgun.OrbitalRailgunMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OrbitalRailgunStrikeManager {
    public static final ConcurrentHashMap<Tuple<BlockPos, List<Entity>>, Tuple<Integer, ResourceKey<Level>>> activeStrikes = 
            new ConcurrentHashMap<>();
            
    private static final ResourceKey<DamageType> STRIKE_DAMAGE = 
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(OrbitalRailgunMod.MOD_ID, "strike"));
            
    private static final int RADIUS = 24;
    private static final int RADIUS_SQUARED = RADIUS * RADIUS;
    private static final Boolean[][] mask = new Boolean[RADIUS * 2 + 1][RADIUS * 2 + 1];

    public static void tick(MinecraftServer server) {
        activeStrikes.forEach((keyPair1, keyPair2) -> {
            float age = server.getTickCount() - keyPair2.getA();
            BlockPos blockPos = keyPair1.getA();
            List<Entity> entities = keyPair1.getB();
            ResourceKey<Level> dimension = keyPair2.getB();
            
            if (age >= 700) {
                activeStrikes.remove(keyPair1);

                ServerLevel world = server.getLevel(dimension);
                if (world != null) {
                    entities.forEach(entity -> {
                        if (entity.level().dimension() == dimension && 
                            entity.position().subtract(Vec3.atCenterOf(blockPos)).lengthSqr() <= RADIUS_SQUARED) {
                            DamageSource damageSource = new DamageSource(
                                    world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                            .getHolderOrThrow(STRIKE_DAMAGE)
                            );
                            entity.hurt(damageSource, 100000f);
                        }
                    });

                    explode(blockPos, world);
                }
            } else if (age >= 400) {
                entities.forEach(entity -> {
                    if (entity.level().dimension() == dimension) {
                        Vec3 dir = Vec3.atCenterOf(blockPos).subtract(entity.position());
                        double mag = Math.min(1.0 / Math.abs(dir.length() - 20.0) * 4.0 * (age - 400.0) / 300.0, 5.0);
                        dir = dir.normalize();

                        entity.push(dir.x * mag, dir.y * mag, dir.z * mag);
                        entity.hurtMarked = true;
                    }
                });
            }
        });
    }

    private static void explode(BlockPos origin, Level world) {
        for (int y = world.getMinBuildHeight(); y <= world.getMaxBuildHeight(); y++) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (mask[x + RADIUS][z + RADIUS]) {
                        world.setBlockAndUpdate(new BlockPos(origin.getX() + x, y, origin.getZ() + z), 
                                Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    public static void initialize() {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                mask[x + RADIUS][z + RADIUS] = Vector2i.lengthSquared(x, z) <= RADIUS_SQUARED;
            }
        }
    }
}