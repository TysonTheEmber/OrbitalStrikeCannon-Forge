package com.mishkis.orbitalrailgun.client.render;

import com.mishkis.orbitalrailgun.ModConfigs;
import com.mishkis.orbitalrailgun.item.OrbitalRailgunItem;
import com.mishkis.orbitalrailgun.client.shader.OrbitalShaders;
import com.mishkis.orbitalrailgun.network.ClientStrikeEffectManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import com.mishkis.orbitalrailgun.item.OrbitalRailgunItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = "orbitalrailgun", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeRenderHandler {
    
    private static final Minecraft minecraft = Minecraft.getInstance();
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (!ModConfigs.ENABLE_SHADER_EFFECTS.get()) return;
        
        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();
        
        // Run post-processing chains if available (pure post like Satin)
        OrbitalShaders.renderStrike(camera.getPosition(), partialTick);
        OrbitalShaders.renderGui(partialTick);
    }
    
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Player player = minecraft.player;
        if (player == null) return;

        boolean aiming = player.isUsingItem() && player.getUseItem().getItem() instanceof OrbitalRailgunItem;
        if (ModConfigs.HIDE_HUD_WHEN_AIMING.get()) {
            minecraft.options.hideGui = aiming;
        }
        // We no longer draw a 2D fallback overlay here for parity; GUI post is handled in world stage
    }
    
    // Removed geometry fallback to match Satin post-only pipeline
    private static void renderWorldStrikeEffects(Camera camera, PoseStack poseStack, float partialTick) {
        if (minecraft.level == null) return;
        
        ClientStrikeEffectManager.StrikeEffect strike = 
                ClientStrikeEffectManager.getCurrentStrike(minecraft.level.dimension());
        
        if (strike == null) return;
        
        long currentTime = System.currentTimeMillis();
        float age = strike.getAgeInTicks(currentTime);
        
        Vec3 cameraPos = camera.getPosition();
        Vec3 strikePos = Vec3.atCenterOf(strike.position);
        
        poseStack.pushPose();
        poseStack.translate(
            strikePos.x - cameraPos.x,
            strikePos.y - cameraPos.y,
            strikePos.z - cameraPos.z
        );
        
        // Render strike beam effect
        renderStrikeBeam(poseStack, age, partialTick);
        
        // Render explosion effect
        if (age >= 700) {
            renderExplosionEffect(poseStack, age - 700, partialTick);
        }
        
        poseStack.popPose();
    }
    
    private static void renderStrikeBeam(PoseStack poseStack, float age, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Render vertical beam from sky to ground
        float beamRadius = Math.min(age * 0.1f, 2.0f);
        float intensity = Math.max(0.0f, 1.0f - age / 800.0f);
        
        // Blue beam color
        float r = 0.62f * intensity;
        float g = 0.93f * intensity;
        float b = 0.93f * intensity;
        float a = 0.8f * intensity;
        
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = (float) Math.cos(angle) * beamRadius;
            float z = (float) Math.sin(angle) * beamRadius;
            
            // Top vertex (sky)
            buffer.vertex(matrix, x, 200, z).color(r, g, b, 0.0f).endVertex();
            // Bottom vertex (ground)
            buffer.vertex(matrix, x, -50, z).color(r, g, b, a).endVertex();
        }
        
        tesselator.end();
        
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    
    private static void renderExplosionEffect(PoseStack poseStack, float explosionAge, float partialTick) {
        float radius = Math.min(explosionAge * 2.0f, 24.0f);
        float intensity = Math.max(0.0f, 1.0f - explosionAge / 100.0f);
        
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        Matrix4f matrix = poseStack.last().pose();
        
        // White explosion color
        float r = 1.0f * intensity;
        float g = 1.0f * intensity;
        float b = 1.0f * intensity;
        float a = 0.6f * intensity;
        
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        
        int segments = 32;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            
            // Center vertex
            buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a).endVertex();
            // Outer vertex
            buffer.vertex(matrix, x, 0, z).color(r, g, b, 0.0f).endVertex();
        }
        
        tesselator.end();
        
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    
    private static void renderTargetingOverlay(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTick) {
        if (minecraft.player == null) return;
        
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // Perform raycast to find target
        HitResult hitResult = minecraft.player.pick(ModConfigs.RAYCAST_DISTANCE.get(), partialTick, false);
        
        // Render crosshair
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        // Crosshair color based on hit result
        int color = switch (hitResult.getType()) {
            case BLOCK, ENTITY -> 0xFF00FF00; // Green for valid targets
            case MISS -> 0xFFFF0000; // Red for no target
        };
        
        // Draw crosshair
        guiGraphics.fill(centerX - 10, centerY - 1, centerX + 10, centerY + 1, color);
        guiGraphics.fill(centerX - 1, centerY - 10, centerX + 1, centerY + 10, color);
        
        // Draw targeting circle
        // Note: This is a simplified circle - in a real implementation you might use a texture
        int circleRadius = 20;
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            int x = centerX + (int) (Math.cos(angle) * circleRadius);
            int y = centerY + (int) (Math.sin(angle) * circleRadius);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, color);
        }
        
        // Display distance to target
        if (hitResult.getType() != HitResult.Type.MISS) {
            Vec3 targetPos = hitResult.getLocation();
            double distance = minecraft.player.position().distanceTo(targetPos);
            String distanceText = String.format("Distance: %.1fm", distance);
            
            guiGraphics.drawCenteredString(
                minecraft.font,
                distanceText,
                centerX,
                centerY + 40,
                0xFFFFFFFF
            );
        }
    }
}
