package com.mishkis.orbitalrailgun.client.shader;

import com.mishkis.orbitalrailgun.OrbitalRailgunMod;
import com.mishkis.orbitalrailgun.ModConfigs;
import com.mishkis.orbitalrailgun.network.ClientStrikeEffectManager;
import com.mishkis.orbitalrailgun.item.OrbitalRailgunItem;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = OrbitalRailgunMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class OrbitalShaders {
    private static PostChain STRIKE_CHAIN;
    private static PostChain GUI_CHAIN;
    private static int lastW = -1, lastH = -1;
    private static int guiTicks = 0;

    private static final ResourceLocation STRIKE_POST = new ResourceLocation(OrbitalRailgunMod.MOD_ID, "shaders/post/orbital_railgun.json");
    private static final ResourceLocation GUI_POST = new ResourceLocation(OrbitalRailgunMod.MOD_ID, "shaders/post/orbital_railgun_gui.json");

    // No shader registrations required for post-processing programs;
    // PostChain will load assets/orbitalrailgun/shaders/program/*.json directly.

    private static void ensureChains() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        RenderTarget main = mc.getMainRenderTarget();
        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();

        try {
            if (STRIKE_CHAIN == null) {
                STRIKE_CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), main, STRIKE_POST);
            }
            if (GUI_CHAIN == null) {
                GUI_CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), main, GUI_POST);
            }
        } catch (Exception e) {
            OrbitalRailgunMod.LOGGER.error("Failed to create post chains", e);
        }

        if (w != lastW || h != lastH) {
            lastW = w; lastH = h;
            if (STRIKE_CHAIN != null) STRIKE_CHAIN.resize(w, h);
            if (GUI_CHAIN != null) GUI_CHAIN.resize(w, h);
        }
    }

    public static void renderStrike(Vec3 cameraPos, float partialTick) {
        if (!ModConfigs.ENABLE_SHADER_EFFECTS.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ClientStrikeEffectManager.StrikeEffect effect = ClientStrikeEffectManager.getCurrentStrike(mc.level.dimension());
        if (effect == null) return;

        ensureChains();
        if (STRIKE_CHAIN == null) return;

        float time = effect.getAgeInTicks(System.currentTimeMillis()) / 20.0f; // seconds
        Vector3f cam = cameraPos.toVector3f();
        Vector3f blk = effect.positionVector;

        // Current matrices
        org.joml.Matrix4f proj = new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
        org.joml.Matrix4f invTransform = new org.joml.Matrix4f(proj).invert();
        org.joml.Matrix4f modelView = new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrix());

        // Set common uniforms on all passes in the chain if present (via reflection)
        java.util.List<PostPass> passes = getPasses(STRIKE_CHAIN);
        if (passes != null) {
            for (PostPass pass : passes) {
                EffectInstance eff = pass.getEffect();
                if (eff != null) {
                    setUniforms(eff, time, cam, blk, invTransform, modelView, -1f);
                }
            }
        }

        STRIKE_CHAIN.process(partialTick);
    }

    public static void renderGui(float partialTick) {
        if (!ModConfigs.ENABLE_SHADER_EFFECTS.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Only render GUI post when actively aiming with the railgun (parity with Fabric)
        var player = mc.player;
        boolean aiming = player.isUsingItem() && player.getUseItem().getItem() instanceof OrbitalRailgunItem;
        if (!aiming) { guiTicks = 0; return; }

        ensureChains();
        if (GUI_CHAIN == null) return;

        // Build uniforms similar to original GUI shader
        var hit = player.pick(ModConfigs.RAYCAST_DISTANCE.get(), mc.getFrameTime(), false);
        float isBlockHit = hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? 0f : 1f;
        Vector3f hitPos = hit.getLocation() != null ? hit.getLocation().toVector3f() : new Vector3f();
        Vector3f cam = player.position().toVector3f();

        guiTicks++;
        float time = (guiTicks + partialTick) / 20.0f;

        org.joml.Matrix4f proj = new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
        org.joml.Matrix4f invTransform = new org.joml.Matrix4f(proj).invert();
        org.joml.Matrix4f modelView = new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrix());

        java.util.List<PostPass> passes = getPasses(GUI_CHAIN);
        if (passes != null) {
            for (PostPass pass : passes) {
                EffectInstance eff = pass.getEffect();
                if (eff != null) {
                    setUniforms(eff, time, cam, hitPos, invTransform, modelView, isBlockHit);
                }
            }
        }

        GUI_CHAIN.process(partialTick);
    }
    private static void setUniforms(EffectInstance eff, float time, Vector3f cam, Vector3f blockPos, org.joml.Matrix4f invTransform, org.joml.Matrix4f modelView, float isBlockHit) {
        Uniform u;
        u = eff.getUniform("iTime"); if (u != null) u.set(time);
        u = eff.getUniform("CameraPosition"); if (u != null) u.set(cam);
        u = eff.getUniform("BlockPosition"); if (u != null) u.set(blockPos);
        u = eff.getUniform("IsBlockHit"); if (u != null && isBlockHit >= 0f) u.set(isBlockHit);
        u = eff.getUniform("InverseTransformMatrix"); if (u != null) u.set(invTransform);
        u = eff.getUniform("ModelViewMat"); if (u != null) u.set(modelView);
    }

    private static java.util.List<PostPass> getPasses(PostChain chain) {
        try {
            java.lang.reflect.Field f = PostChain.class.getDeclaredField("passes");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<PostPass> list = (java.util.List<PostPass>) f.get(chain);
            return list;
        } catch (Exception e) {
            OrbitalRailgunMod.LOGGER.warn("Unable to access PostChain passes via reflection", e);
            return null;
        }
    }
}
