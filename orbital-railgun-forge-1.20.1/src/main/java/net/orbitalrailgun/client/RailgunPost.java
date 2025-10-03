package net.orbitalrailgun.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.orbitalrailgun.ForgeOrbitalRailgunMod;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RailgunPost {
    private static PostChain worldChain;
    private static PostChain guiChain;

    private static final Map<String, ShaderInstance> worldEffects = new HashMap<>();
    private static final Map<String, ShaderInstance> guiEffects = new HashMap<>();

    public static void reload(Minecraft mc) {
        dispose();
        try {
            worldChain = new PostChain(
                    mc.getTextureManager(),
                    mc.getResourceManager(),
                    mc.getMainRenderTarget(),
                    new ResourceLocation(ForgeOrbitalRailgunMod.MODID, "shaders/post/orbital_railgun.json")
            );
            worldChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            cacheEffects(worldChain, worldEffects);
        } catch (Exception ignored) {
            worldChain = null;
            worldEffects.clear();
        }
        try {
            guiChain = new PostChain(
                    mc.getTextureManager(),
                    mc.getResourceManager(),
                    mc.getMainRenderTarget(),
                    new ResourceLocation(ForgeOrbitalRailgunMod.MODID, "shaders/post/orbital_railgun_gui.json")
            );
            guiChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            cacheEffects(guiChain, guiEffects);
        } catch (Exception ignored) {
            guiChain = null;
            guiEffects.clear();
        }
    }

    public static void dispose() {
        if (worldChain != null) { worldChain.close(); worldChain = null; }
        if (guiChain != null) { guiChain.close(); guiChain = null; }
        worldEffects.clear();
        guiEffects.clear();
    }

    public static void resize(int w, int h) {
        if (worldChain != null) worldChain.resize(w, h);
        if (guiChain != null) guiChain.resize(w, h);
    }

    public static void process(float partialTicks, boolean renderWorld, boolean renderGuiOverlay) {
        Minecraft mc = Minecraft.getInstance();
        if (renderWorld && worldChain != null) {
            updateCommonUniforms(worldEffects, partialTicks);
            // World-specific uniforms
            if (RailgunState.strikePos != null) setVec3(worldEffects, "BlockPosition", RailgunState.strikePos);
            worldChain.process(partialTicks);
        }
        if (renderGuiOverlay && guiChain != null) {
            updateCommonUniforms(guiEffects, partialTicks);
            // GUI-specific uniforms
            setFloat(guiEffects, "IsBlockHit", RailgunState.isBlockHit ? 1f : 0f);
            if (RailgunState.guiBlockPos != null) setVec3(guiEffects, "BlockPosition", RailgunState.guiBlockPos);
            guiChain.process(partialTicks);
        }
    }

    private static void updateCommonUniforms(Map<String, ShaderInstance> effects, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        var cam = mc.gameRenderer.getMainCamera();
        setFloat(effects, "iTime", RailgunState.getTimeSeconds(partialTicks));
        setVec3(effects, "CameraPosition", cam.getPosition());
    }

    private static void cacheEffects(PostChain chain, Map<String, ShaderInstance> out) throws Exception {
        // Reflect PostChain.passes and PostPass.effect to collect EffectInstance per pass name
        Field passesField = PostChain.class.getDeclaredField("passes");
        passesField.setAccessible(true);
        List<?> passes = (List<?>) passesField.get(chain);
        for (Object pass : passes) {
            Field nameF = pass.getClass().getDeclaredField("name");
            nameF.setAccessible(true);
            String name = (String) nameF.get(pass);

            Field effectF = pass.getClass().getDeclaredField("effect");
            effectF.setAccessible(true);
            ShaderInstance effect = (ShaderInstance) effectF.get(pass);
            out.put(name, effect);
        }
    }

    private static void setFloat(Map<String, ShaderInstance> effects, String uniform, float v) {
        for (ShaderInstance e : effects.values()) {
            var u = e.getUniform(uniform);
            if (u != null) { u.set(v); u.upload(); }
        }
    }
    private static void setVec3(Map<String, ShaderInstance> effects, String uniform, Vec3 v) {
        for (ShaderInstance e : effects.values()) {
            var u = e.getUniform(uniform);
            if (u != null) { u.set((float) v.x, (float) v.y, (float) v.z); u.upload(); }
        }
    }
}
