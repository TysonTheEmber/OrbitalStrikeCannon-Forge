package com.mishkis.orbitalrailgun;

import com.mishkis.orbitalrailgun.network.ModNetworking;
import com.mishkis.orbitalrailgun.registry.ModCreativeModeTabs;
import com.mishkis.orbitalrailgun.registry.ModItems;
import com.mishkis.orbitalrailgun.util.OrbitalRailgunStrikeManager;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

@Mod(OrbitalRailgunMod.MOD_ID)
public class OrbitalRailgunMod {
    public static final String MOD_ID = "orbitalrailgun";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OrbitalRailgunMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register our mod's forge bus listeners
        modEventBus.addListener(this::commonSetup);
        
        // Register deferred registers
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModConfigs.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.COMMON_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("Orbital Railgun Forge port loading...");
        event.enqueueWork(() -> {
            // Initialize networking
            ModNetworking.init();
            // Initialize strike manager
            OrbitalRailgunStrikeManager.initialize();
        });
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            OrbitalRailgunStrikeManager.tick(event.getServer());
        }
    }
}