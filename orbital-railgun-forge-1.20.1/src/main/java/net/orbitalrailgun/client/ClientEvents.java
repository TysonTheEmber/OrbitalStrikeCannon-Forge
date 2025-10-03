package net.orbitalrailgun.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.orbitalrailgun.ForgeOrbitalRailgunMod;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ForgeOrbitalRailgunMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && FMLEnvironment.dist == Dist.CLIENT) {
            RailgunState.tickClient();
        }
    }

    @SubscribeEvent
    public static void onRegisterReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new net.minecraft.server.packs.resources.SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(net.minecraft.server.packs.resources.ResourceManager resourceManager, net.minecraft.util.profiling.ProfilerFiller profiler) {
                return null;
            }
            @Override
            protected void apply(Void unused, net.minecraft.server.packs.resources.ResourceManager resourceManager, net.minecraft.util.profiling.ProfilerFiller profiler) {
                Minecraft mc = Minecraft.getInstance();
                RailgunPost.reload(mc);
            }
        });
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Update GUI targeting from current hit result
        HitResult hr = mc.hitResult;
        RailgunState.isBlockHit = false;
        RailgunState.guiBlockPos = null;
        if (hr != null && hr.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hr;
            RailgunState.isBlockHit = true;
            RailgunState.guiBlockPos = Vec3.atCenterOf(bhr.getBlockPos());
        }

        // Optionally ensure strike state is still valid for current dimension
        if (RailgunState.strikePos != null) {
            ResourceKey<Level> dim = mc.level.dimension();
            if (RailgunState.strikeDimension != null && RailgunState.strikeDimension != dim) {
                RailgunState.clearStrike();
            }
        }

        // Resize safety on window changes
        RailgunPost.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());

        // Process chains
        float partial = event.getPartialTick();
        RailgunPost.process(partial, true, true);
    }
}
