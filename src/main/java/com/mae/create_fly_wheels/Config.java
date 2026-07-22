package com.mae.create_fly_wheels;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;

import com.mae.create_fly_wheels.MaterialMultipliers;


import java.util.HashMap;
import java.util.Map;

public final class Config {

    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.DoubleValue stressPerSpeed;
    public static ModConfigSpec.DoubleValue packetThrottleTicks;
    public static ModConfigSpec.DoubleValue physicsThrottleTicks;

    public static ModConfigSpec.BooleanValue debug;
    public static ModConfigSpec.BooleanValue shareTheLoad;
    public static ModConfigSpec.BooleanValue flywheelsAreDangerous;
    public static ModConfigSpec.BooleanValue flywheelsAreDeadly;
    public static ModConfigSpec.BooleanValue overstressedIsHalt;

    public static final Map<String, MaterialMultipliers> MATERIALS = new HashMap<>();
    public static final Map<ResourceLocation, String> CTFMG_MATERIALS = Map.of(
        ResourceLocation.parse("tfmg:steel_flywheel"), "ingots/steel",
        ResourceLocation.parse("tfmg:lead_flywheel"), "ingots/lead",
        ResourceLocation.parse("tfmg:cast_iron_flywheel"), "ingots/cast_iron",
        ResourceLocation.parse("tfmg:aluminum_flywheel"), "ingots/aluminum",
        ResourceLocation.parse("tfmg:nickel_flywheel"), "ingots/nickel"
        );

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Global Settings")
                .push("fly_wheels");

        stressPerSpeed = builder
                .comment("Stress generated per speed unit")
                .defineInRange("stress_per_speed", (double) 150, 0, 100000);
        
        packetThrottleTicks = builder
                .comment("number of ticks to wait before sending visual data to the client")
                .defineInRange("packet_throtle_ticks", (double) 9, 0, 100);

        physicsThrottleTicks = builder
                .comment("number of ticks to wait before calculating bounding box physics interactions")
                .defineInRange("physics_throtle_ticks", (double) 3, 0, 100);

        debug = builder
                .comment("print debug messages to console for development")
                .define("debug", true);

        shareTheLoad = builder
                .comment("Distribute extra stress amoung as many flywheels as possible. This is the worst performance case but looks cooler probably.")
                .define("share_the_load", true);

        flywheelsAreDangerous = builder
                .comment("Flywheels impart momentum")
                .define("flywheels_are_dangerous", true);

        flywheelsAreDeadly = builder
                .comment("Flywheels do damage based on stores")
                .define("flywheels_are_deadly", true);

        overstressedIsHalt = builder
                .comment("flywheels lose stored momentum when the network overstresses")
                .define("overstressed_is_halt", true);

        builder.pop();

        builder.push("materials");
        builder.comment("(global is applied as a multiplier to all other materials)");

        addMaterial(builder, "global", "invalid",               1, 1, 1,        1, 1, 0.5f,                1, 1, 1);
        addMaterial(builder, "default", "Normal",               3, 1, 0,        150, 500, 1,            1, 1, 1); //useful middle ground

        //hypnothetical modded wheels, setting up like this for maybe doing a copycat flywheel later
        addMaterial(builder, "ingots/iron", "Iron",             3, 1, 0,        150, 150, 1,            1, 1, 1);
        addMaterial(builder, "ingots/brass", "Brass",           1, 1, 0,        150, 150, 1,            1, 1, 1);
        addMaterial(builder, "ingots/copper", "Copper",         1, 1, 0,        150, 150, 1,            1, 1, 1);

        //CTFMG compat materials
        addMaterial(builder, "ingots/steel", "Steel",           2, 1, 0,        10, 500, 5,            3, 0.5f, 1.3f);//rocket jumper
        addMaterial(builder, "ingots/lead", "Lead",             9, 1, 0,        150, 300, 3,            1, 1, 1);//battery
        addMaterial(builder, "ingots/cast_iron", "Cast Iron",   3, 1, 0,        250, 250, 9,            1.5f, 1.5f, 1.3f);//mob grinder
        addMaterial(builder, "ingots/aluminum", "Aluminum",     3, 1, 0,        1500, 1500, 1,         1, 1, 1); //capacitor
        addMaterial(builder, "ingots/nickel", "Nickel",         4, 1, 0,        300, 300, 0.01f,        0.1f, 0.1f, 0.5f); //sub for brass, low friction & soft & safe

        builder.pop(); 

        SPEC = builder.build();
    }

    private static void addMaterial(
        ModConfigSpec.Builder builder, String type, String prettyName, 
        float weight, float chargingLoss, float internalFriction, 
        float storesInPerTick, float storesOutPerTick, float baseLoad, 
        float pushStrength, float damage, float hitbox
        ){

        builder.push(type);

        builder.comment("Flywheel material settings for " +
                (prettyName.isEmpty() ? type : prettyName));

        ModConfigSpec.ConfigValue<String> typeValue = builder
                .comment("copycat material (not implemented)")
                .define("type", type);

        ModConfigSpec.ConfigValue<String> prettyNameValue = builder
                .comment("display name")
                .define("pretty_name", prettyName);

        ModConfigSpec.ConfigValue<Double> weightValue = builder
                .comment("effectively a multipler on maximum internal stress stored, also multiplied by shaft speed")
                .defineInRange("weight", (double) weight, 0.0, 1000.0);

        ModConfigSpec.ConfigValue<Double> chargingLossValue = builder
                .comment("chargingLoss multiplier")
                .defineInRange("chargingLoss", (double) chargingLoss, 0.0, 1000.0);

        ModConfigSpec.ConfigValue<Double> internalFrictionValue = builder
                .comment("Internal Friction multiplier (not implemented, use base load for similar functionality)")
                .defineInRange("internal_friction", (double) internalFriction, 0.0, 1000.0);

        ModConfigSpec.ConfigValue<Double> storesInValue = builder
                .comment("input limiter in stores / tick")
                .defineInRange("limit_in", (double) storesInPerTick, 0.0, 100000.0);

        ModConfigSpec.ConfigValue<Double> storesOutValue = builder
                .comment("output limiter in stores / tick")
                .defineInRange("limit_out", (double) storesOutPerTick, 0.0, 100000.0);

        ModConfigSpec.ConfigValue<Double> baseLoadValue = builder
                .comment("Base mechanical load, scales with shaft speed")
                .defineInRange("base_load", (double) baseLoad, 0.0, 100000.0);

        ModConfigSpec.ConfigValue<Double> pushStrengthValue = builder
                .comment("push strength multiplier")
                .defineInRange("push_strength", (double) pushStrength, 0.0, 100000.0);

        ModConfigSpec.ConfigValue<Double> damageValue = builder
                .comment("damage multiplier")
                .defineInRange("damage", (double) damage, 0.0, 100000.0);

        ModConfigSpec.ConfigValue<Double> hitboxValue = builder
                .comment("hitbox size multiplier")
                .defineInRange("hitbox_size", (double) hitbox, 0.0, 100000.0);

        builder.pop();

        MATERIALS.put(type, new MaterialMultipliers(
                typeValue,
                prettyNameValue,
                weightValue,
                chargingLossValue,
                internalFrictionValue,
                baseLoadValue,
                storesInValue,
                storesOutValue,
                pushStrengthValue,
                damageValue,
                hitboxValue
        ));
    }

    public static float stressPerSpeed() {
        return stressPerSpeed.get().floatValue();
    }

    public static float packetThrottleTicks() {
        return packetThrottleTicks.get().floatValue();
    }

    public static float physicsThrottleTicks() {
        return physicsThrottleTicks.get().floatValue();
    }

    public static boolean shareTheLoad() {
        return shareTheLoad.get().booleanValue();
    }
    public static boolean flywheelsAreDangerous() {
        return flywheelsAreDangerous.get().booleanValue();
    }
    public static boolean flywheelsAreDeadly() {
        return flywheelsAreDeadly.get().booleanValue();
    }
    public static boolean overstressedIsHalt() {
        return overstressedIsHalt.get().booleanValue();
    }
    public static boolean debug() {
        return debug.get().booleanValue();
    }
    public static void debugLog(Object value){
        if (!debug())
            return;

        System.out.println("[Fly Wheels] " + String.valueOf(value));
    }

}