package com.mishkis.orbitalrailgun.item;

import com.mishkis.orbitalrailgun.OrbitalRailgunMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class OrbitalRailgunRenderer extends GeoItemRenderer<OrbitalRailgunItem> {
    public OrbitalRailgunRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(OrbitalRailgunMod.MOD_ID, "orbital_railgun")));
    }
}