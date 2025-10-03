package net.orbitalrailgun.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import net.orbitalrailgun.ForgeOrbitalRailgunMod;

@Mod.EventBusSubscriber(modid = ForgeOrbitalRailgunMod.MODID, value = Dist.CLIENT)
public final class DevKeybind {
    private static KeyMapping TOGGLE;

    @SubscribeEvent
    public static void reg(RegisterKeyMappingsEvent e) {
        TOGGLE = new KeyMapping("key.or.toggle_strike", GLFW.GLFW_KEY_G, "key.categories.orbital");
        e.register(TOGGLE);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || TOGGLE == null) return;
        if (TOGGLE.consumeClick()) {
            var mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) return;
            HitResult hr = mc.hitResult;
            if (hr != null && hr.getType() == HitResult.Type.BLOCK) {
                var pos = ((BlockHitResult) hr).getBlockPos();
                RailgunState.strikePos = Vec3.atCenterOf(pos);
                RailgunState.strikeDimension = mc.level.dimension();
            } else if (mc.player != null) {
                RailgunState.strikePos = mc.player.position();
                RailgunState.strikeDimension = mc.level.dimension();
            }
        }
    }
}
