package com.mishkis.orbitalrailgun.network;

import com.mishkis.orbitalrailgun.OrbitalRailgunMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    
    private static int id() {
        return packetId++;
    }
    
    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(OrbitalRailgunMod.MOD_ID, "messages"))
                .serverAcceptedVersions(s -> true)
                .clientAcceptedVersions(s -> true)
                .networkProtocolVersion(() -> "1.0.0")
                .simpleChannel();
        
        INSTANCE = net;
        
        net.messageBuilder(ShootPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ShootPacket::decode)
                .encoder(ShootPacket::encode)
                .consumerMainThread(ShootPacket::handle)
                .add();
        
        net.messageBuilder(ClientSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientSyncPacket::decode)
                .encoder(ClientSyncPacket::encode)
                .consumerMainThread(ClientSyncPacket::handle)
                .add();
    }
    
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }
    
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
    
    public static void init() {
        register();
    }
}