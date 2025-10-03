package com.mishkis.orbitalrailgun.registry;

import com.mishkis.orbitalrailgun.OrbitalRailgunMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OrbitalRailgunMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ORBITAL_RAILGUN_TAB = CREATIVE_MODE_TABS.register("orbital_railgun_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ORBITAL_RAILGUN.get()))
                    .title(Component.translatable("creativetab.orbitalrailgun.orbital_railgun_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.ORBITAL_RAILGUN.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}