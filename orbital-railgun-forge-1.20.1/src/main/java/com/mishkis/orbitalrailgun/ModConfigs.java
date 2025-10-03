package com.mishkis.orbitalrailgun;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OrbitalRailgunMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfigs {
    
    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_SPEC;

    // Client-side configurations
    public static final ForgeConfigSpec.BooleanValue ENABLE_SHADER_EFFECTS;
    public static final ForgeConfigSpec.DoubleValue EFFECT_INTENSITY;
    public static final ForgeConfigSpec.BooleanValue HIDE_HUD_WHEN_AIMING;

    // Common configurations (both client and server)
    public static final ForgeConfigSpec.IntValue STRIKE_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue STRIKE_DAMAGE;
    public static final ForgeConfigSpec.IntValue STRIKE_RADIUS;
    public static final ForgeConfigSpec.DoubleValue RAYCAST_DISTANCE;

    static {
        CLIENT_BUILDER.comment("Client-side configuration settings");

        ENABLE_SHADER_EFFECTS = CLIENT_BUILDER
                .comment("Enable visual shader effects for orbital strikes")
                .define("enableShaderEffects", true);

        EFFECT_INTENSITY = CLIENT_BUILDER
                .comment("Intensity of visual effects (0.0 to 2.0)")
                .defineInRange("effectIntensity", 1.0, 0.0, 2.0);

        HIDE_HUD_WHEN_AIMING = CLIENT_BUILDER
                .comment("Hide HUD when aiming the railgun")
                .define("hideHudWhenAiming", true);

        CLIENT_SPEC = CLIENT_BUILDER.build();

        COMMON_BUILDER.comment("Common configuration settings");

        STRIKE_COOLDOWN = COMMON_BUILDER
                .comment("Cooldown time for orbital railgun in ticks (20 ticks = 1 second)")
                .defineInRange("strikeCooldown", 2400, 200, 12000);

        STRIKE_DAMAGE = COMMON_BUILDER
                .comment("Damage dealt by orbital strikes")
                .defineInRange("strikeDamage", 100000.0, 1.0, 1000000.0);

        STRIKE_RADIUS = COMMON_BUILDER
                .comment("Radius of orbital strike explosion in blocks")
                .defineInRange("strikeRadius", 24, 1, 100);

        RAYCAST_DISTANCE = COMMON_BUILDER
                .comment("Maximum raycast distance for targeting")
                .defineInRange("raycastDistance", 300.0, 50.0, 1000.0);

        COMMON_SPEC = COMMON_BUILDER.build();
    }
}