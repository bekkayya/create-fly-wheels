package com.mae.create_fly_wheels.mixin_access;

import com.mae.create_fly_wheels.Config;
import com.mae.create_fly_wheels.MaterialMultipliers;

import java.util.Map;

public interface FlywheelBlockEntityAccess {
    Map<String, String> flwls$getNetworkedInfo();
    Map<String, Float> flwls$getNetworkedValues();
    
    MaterialMultipliers flwls$getConfigMultipliers();

    void flwls$setCurrentStores(float stress);
    float flwls$getCurrentStores();

    void flwls$setStoresOutPerTick(float value);
    float flwls$getStoresOutPerTick();

    void flwls$setStoresInPerTick(float value);
    float flwls$getStoresInPerTick();

    void flwls$setStressPerSpeed (float value);
    float flwls$getStressPerSpeed();

    float flwls$consumeCurrentStores(float pillaged);
}