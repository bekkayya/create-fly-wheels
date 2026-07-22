package com.mae.create_fly_wheels;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

public record MaterialMultipliers(
        ModConfigSpec.ConfigValue<String> typeVal,
        ModConfigSpec.ConfigValue<String> prettyNameVal,

        ModConfigSpec.ConfigValue<Double> weightVal,
        ModConfigSpec.ConfigValue<Double> chargingLossVal,
        ModConfigSpec.ConfigValue<Double> internalFrictionVal,

        ModConfigSpec.ConfigValue<Double> baseLoadVal,
        ModConfigSpec.ConfigValue<Double> storesInPerTickVal,
        ModConfigSpec.ConfigValue<Double> storesOutPerTickVal,

        ModConfigSpec.ConfigValue<Double> pushStrengthVal,
        ModConfigSpec.ConfigValue<Double> baseDamageVal,
        ModConfigSpec.ConfigValue<Double> hitboxSizeVal
) {

    public String type() {
        return typeVal.get();
    }

    public String prettyName() {
        return prettyNameVal.get();
    }

    public float weight() {
        return weightVal.get().floatValue();
    }

    public float weight(MaterialMultipliers other) {
        return weight() * other.weight();
    }

    public float chargingLoss() {
        return chargingLossVal.get().floatValue();
    }

    public float chargingLoss(MaterialMultipliers other) {
        return chargingLoss() * other.chargingLoss();
    }

    public float internalFriction() {
        return internalFrictionVal.get().floatValue();
    }

    public float internalFriction(MaterialMultipliers other) {
        return internalFriction() * other.internalFriction();
    }

    public float baseLoad() {
        return baseLoadVal.get().floatValue();
    }

    public float baseLoad(MaterialMultipliers other) {
        return baseLoad() * other.baseLoad();
    }

    public float storesIn() {
        return storesInPerTickVal.get().floatValue();
    }

    public float storesIn(MaterialMultipliers other) {
        return storesIn() * other.storesIn();
    }

    public float storesOut() {
        return storesOutPerTickVal.get().floatValue();
    }

    public float storesOut(MaterialMultipliers other) {
        return storesOut() * other.storesOut();
    }

    public float pushStrength() {
        return pushStrengthVal.get().floatValue();
    }

    public float pushStrength(MaterialMultipliers other) {
        return pushStrength() * other.pushStrength();
    }

    public float baseDamage() {
        return baseDamageVal.get().floatValue();
    }

    public float baseDamage(MaterialMultipliers other) {
        return baseDamage() * other.baseDamage();
    }

    public float hitboxSize() {
        return hitboxSizeVal.get().floatValue();
    }

    public float hitboxSize(MaterialMultipliers other) {
        return hitboxSize() * other.hitboxSize();
    }
}