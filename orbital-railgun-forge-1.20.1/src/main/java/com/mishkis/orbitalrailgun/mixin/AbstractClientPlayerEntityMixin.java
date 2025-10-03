package com.mishkis.orbitalrailgun.mixin;

import com.mishkis.orbitalrailgun.item.OrbitalRailgunItem;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin {

    @ModifyVariable(method = "getFieldOfViewModifier", at = @At("STORE"), ordinal = 0)
    private boolean modifySpyglassCheck(boolean isUsingSpyglass) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        return isUsingSpyglass || player.getUseItem().getItem() instanceof OrbitalRailgunItem;
    }
}