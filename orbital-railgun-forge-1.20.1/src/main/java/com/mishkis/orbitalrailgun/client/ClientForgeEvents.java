package com.mishkis.orbitalrailgun.client;

import com.mishkis.orbitalrailgun.OrbitalRailgunMod;
import com.mishkis.orbitalrailgun.network.ClientStrikeEffectManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// This class listens on the FORGE bus for gameplay/runtime events.
@Mod.EventBusSubscriber(modid = OrbitalRailgunMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientStrikeEffectManager.tick();
        }
    }
}
