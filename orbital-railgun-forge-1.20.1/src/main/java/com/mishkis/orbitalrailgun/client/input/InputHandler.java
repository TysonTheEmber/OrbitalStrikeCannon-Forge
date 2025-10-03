package com.mishkis.orbitalrailgun.client.input;

import com.mishkis.orbitalrailgun.item.OrbitalRailgunItem;
import com.mishkis.orbitalrailgun.network.ClientStrikeEffectManager;
import com.mishkis.orbitalrailgun.network.ModNetworking;
import com.mishkis.orbitalrailgun.network.ShootPacket;
import com.mishkis.orbitalrailgun.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "orbitalrailgun", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InputHandler {

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        
        if (player == null || minecraft.screen != null) return;
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT || event.getAction() != GLFW.GLFW_PRESS) return;
        
        ItemStack activeItem = player.getUseItem();
        if (!(activeItem.getItem() instanceof OrbitalRailgunItem)) return;
        
        // Check if there's already an active strike
        ClientStrikeEffectManager.StrikeEffect currentStrike = 
                ClientStrikeEffectManager.getCurrentStrike(minecraft.level.dimension());
        if (currentStrike != null) return;
        
        // Perform raycast to find target
        HitResult hitResult = player.pick(300.0, minecraft.getFrameTime(), false);
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos targetPos = blockHitResult.getBlockPos();
            
            // Stop using the item
            if (minecraft.gameMode != null) {
                minecraft.gameMode.releaseUsingItem(player);
            }
            
            // Apply local effects immediately for responsiveness
            if (activeItem.getItem() instanceof OrbitalRailgunItem railgun) {
                railgun.shoot(player);
            }
            
            // Add client-side strike effect
            ClientStrikeEffectManager.addStrike(targetPos, minecraft.level.dimension());
            
            // Send packet to server
            ModNetworking.sendToServer(new ShootPacket(activeItem, targetPos));
            
            // Consume the event to prevent other handlers from processing it
            event.setCanceled(true);
        }
    }
}