package com.mishkis.orbitalrailgun.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ClientSyncPacket {
    private final BlockPos blockPos;

    public ClientSyncPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static void encode(ClientSyncPacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
    }

    public static ClientSyncPacket decode(FriendlyByteBuf buffer) {
        return new ClientSyncPacket(buffer.readBlockPos());
    }

    public static void handle(ClientSyncPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Only execute on client side
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null) {
                    // We'll implement the client-side strike effect handler here
                    // For now, just store the position - the rendering system will handle the rest
                    ClientStrikeEffectManager.addStrike(message.blockPos, minecraft.level.dimension());
                }
            });
        });
        context.setPacketHandled(true);
    }
}