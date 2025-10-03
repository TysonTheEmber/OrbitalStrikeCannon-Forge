package net.orbitalrailgun;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraft.client.Minecraft;
import net.orbitalrailgun.client.RailgunPost;

@Mod(ForgeOrbitalRailgunMod.MODID)
public class ForgeOrbitalRailgunMod {
    public static final String MODID = "orbitalrailgun";

    public ForgeOrbitalRailgunMod() {
        // Nothing yet. Client-only features are registered via subscribers below.
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModBus {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> RailgunPost.reload(Minecraft.getInstance()));
        }
    }
}
