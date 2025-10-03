package com.mishkis.orbitalrailgun.client;

import com.mishkis.orbitalrailgun.OrbitalRailgunMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// This class listens on the MOD bus for lifecycle events only.
@Mod.EventBusSubscriber(modid = OrbitalRailgunMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class OrbitalRailgunClientMod {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        OrbitalRailgunMod.LOGGER.info("Orbital Railgun client setup complete");
    }
}
