package com.mishkis.orbitalrailgun.network;

import com.mishkis.orbitalrailgun.item.OrbitalRailgunItem;
import com.mishkis.orbitalrailgun.util.OrbitalRailgunStrikeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ShootPacket {
    private final ItemStack itemStack;
    private final BlockPos blockPos;

    public ShootPacket(ItemStack itemStack, BlockPos blockPos) {
        this.itemStack = itemStack;
        this.blockPos = blockPos;
    }

    public static void encode(ShootPacket message, FriendlyByteBuf buffer) {
        buffer.writeItem(message.itemStack);
        buffer.writeBlockPos(message.blockPos);
    }

    public static ShootPacket decode(FriendlyByteBuf buffer) {
        return new ShootPacket(buffer.readItem(), buffer.readBlockPos());
    }

    public static void handle(ShootPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (message.itemStack.getItem() instanceof OrbitalRailgunItem railgun) {
                railgun.shoot(player);

                List<Entity> nearby = player.serverLevel().getEntities(null, 
                        new AABB(message.blockPos).inflate(500.0, 500.0, 500.0));

                OrbitalRailgunStrikeManager.activeStrikes.put(
                        new Tuple<>(message.blockPos, nearby),
                        new Tuple<>(player.server.getTickCount(), player.level().dimension())
                );

                // Notify nearby players about the strike
                nearby.forEach(entity -> {
                    if (entity instanceof ServerPlayer serverPlayer) {
                        ModNetworking.sendToPlayer(new ClientSyncPacket(message.blockPos), serverPlayer);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}