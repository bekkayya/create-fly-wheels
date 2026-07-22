package com.mae.create_fly_wheels.mixin;

import static com.mae.create_fly_wheels.CreateFlyWheels.MODID;

import com.mae.create_fly_wheels.mixin_access.KineticNetworkAccess;
import com.mae.create_fly_wheels.mixin_access.KineticBlockEntityAccess;
import com.mae.create_fly_wheels.mixin_access.FlywheelBlockEntityAccess;

import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;
import com.mae.create_fly_wheels.Config;


import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.LangBuilder;

import static net.minecraft.ChatFormatting.GRAY;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.List;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;




@Mixin(KineticNetwork.class)
public abstract class KineticNetworkMixin implements KineticNetworkAccess {

    @Shadow 
    public Map<KineticBlockEntity, Float> sources;

    @Shadow 
	public Map<KineticBlockEntity, Float> members;

    @Shadow 
	private float currentCapacity;

    @Shadow 
	private float unloadedCapacity;

    @Shadow 
	private float currentStress;

    @Shadow 
	private float unloadedStress;

    @Shadow 
	private int unloadedMembers;

    @Unique
    private Map<FlywheelBlockEntity, Float> flwls$stores = new HashMap<>();

    @Unique 
    private float flwls$currentStores;

    @Unique 
	private float flwls$unloadedStores;

    @Shadow
    public abstract void sync();

    @Shadow 
    public abstract float calculateStress();

    @Shadow 
    public abstract float calculateCapacity();

    @Shadow 
    public abstract float getActualCapacityOf(KineticBlockEntity be);

    @Shadow 
    public abstract float getActualStressOf(KineticBlockEntity be);

    @Unique
    protected float flwls$getStressMultiplierForSpeed(float speed) {
        return Math.abs(speed);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void flwls$init(CallbackInfo ci) {
        Config.debugLog("[fly wheels network] init");
        this.flwls$stores = new HashMap<>();
    }

    @Inject(method = "initFromTE", at = @At("TAIL"))
    private void flwls$beforeInitFromTE(float maxStress, float currentStress, int members, CallbackInfo ci) {
        Config.debugLog("[fly wheels network] init from TE");
        this.flwls$unloadedStores = this.flwls$currentStores;
        updateStores();
        calculateStress();
    }

    @Inject(method = "addSilently", at = @At("TAIL"))
    public void flwls$addSilently(KineticBlockEntity be, float lastCapacity, float lastStress, CallbackInfo ci) {
        
        KineticNetwork flwls$self = (KineticNetwork)(Object)this;

        if (be instanceof FlywheelBlockEntity flwls$store) {
            //float stored = store.getflwls$currentStores(); 
            Config.debugLog("addSilently flywhel: " + be);
            flwls$stores.put(flwls$store, 0f);
            calculateStress();
            // this.flwls$currentStores += stored;
            // this.flwls$unloadedStores -= stored;

            // if (this.flwls$unloadedStores < 0)
            //     this.flwls$unloadedStores = 0;
        }
    }

    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/KineticNetwork;updateFromNetwork(Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;)V", shift = At.Shift.BEFORE))
    private void flwls$beforeUpdateFromNetwork(KineticBlockEntity be, CallbackInfo ci) {
        KineticNetwork flwls$self = (KineticNetwork)(Object)this;
        if (be instanceof FlywheelBlockEntity flwls$store) {
            this.flwls$stores.put(flwls$store, 0f);
        }
    }

    @Unique 
    public void updateStoresFor(KineticBlockEntity be, float stored) {
        if (be instanceof FlywheelBlockEntity store) {
            this.flwls$stores.put(store, stored);
            updateStores();
        }
	}

    @Inject(method = "remove", at = @At("HEAD"))
    public void flwls$remove(KineticBlockEntity be, CallbackInfo ci) {
        if (!members.containsKey(be))
			return;

        KineticNetwork flwls$self = (KineticNetwork)(Object)this;
        if (be instanceof FlywheelBlockEntity flwls$store) {
            this.flwls$stores.remove(flwls$store);
        }
    }

    @Unique
	public void updateStores() {
		float newStores = calculateStores();
		if (this.flwls$currentStores != newStores) {
			this.flwls$currentStores = newStores;
			sync();
		}
	}

    @Inject(method = "updateNetwork", at = @At("HEAD"), cancellable = true)
    public void flwls$updateNetwork(CallbackInfo cir) {
		float flwls$newStress = calculateStress();
		float flwls$newMaxStress = calculateCapacity();
        float flwls$newStores = calculateStores();

        Config.debugLog("[fly wheels network] update network");

		if (currentStress != flwls$newStress || currentCapacity != flwls$newMaxStress || flwls$currentStores != flwls$newStores) {
			this.currentStress = flwls$newStress;
			this.currentCapacity = flwls$newMaxStress;
            this.flwls$currentStores = flwls$newStores;
			sync();
		}
        cir.cancel();
	}

    @Unique 
    public float calculateStores() {
		float presentStores = 0;
		for (Iterator<FlywheelBlockEntity> iterator = this.flwls$stores.keySet().iterator(); iterator.hasNext();) {
			FlywheelBlockEntity be = iterator.next();
			if (be.getLevel().getBlockEntity(be.getBlockPos()) != be) {
				iterator.remove();
				continue;
			}
			presentStores += getActualStoresOf(be);
		}
		float newMaxStores = presentStores + flwls$unloadedStores;
		return newMaxStores;
	}

    @Unique 
    public float getActualStoresOf(KineticBlockEntity be) {
        if (be instanceof FlywheelBlockEntityAccess access) {
            return flwls$stores.get(be) * flwls$getStressMultiplierForSpeed(access.flwls$getCurrentStores());
        }
		
        return -1;
	}

    @Unique 
    public void resetActiveFlywheels(){
        for (Iterator<FlywheelBlockEntity> iterator = flwls$stores.keySet().iterator(); iterator.hasNext();) {
            FlywheelBlockEntity be = iterator.next();
            if (be instanceof FlywheelBlockEntityAccess access) {
                access.flwls$consumeCurrentStores(0);
            }
        }
    }


    @Inject(method = "calculateStress", at = @At("RETURN"), cancellable = true)
    public void flwls$calculateStressss(CallbackInfoReturnable<Float> cir) {
        float flwls$newStress = cir.getReturnValue();

        Config.debugLog("[fly wheels network] calc stress " + flwls$newStress + " (" + currentCapacity + ")" + flwls$stores.keySet().size());

        resetActiveFlywheels();

        float flwls$stressDelta = currentCapacity - flwls$newStress;
        float flwls$consumed = 0;

        for (Iterator<FlywheelBlockEntity> iterator = flwls$stores.keySet().iterator(); iterator.hasNext();) {
            FlywheelBlockEntity flwls$be = iterator.next();
            
            float flwls$bite = 0;
            if (flwls$be instanceof FlywheelBlockEntityAccess flwls$access) {
                Config.debugLog("seeking a home for " + flwls$stressDelta);
                
                flwls$bite = flwls$access.flwls$consumeCurrentStores(flwls$stressDelta);

                flwls$stressDelta -= flwls$bite;
                flwls$consumed += flwls$bite;

                if (flwls$stressDelta == 0){
                    break;
                }
            }
        }

        Config.debugLog("flywheels consumed total of " + flwls$consumed);

        if(currentCapacity - flwls$newStress < 0) {
            flwls$newStress += flwls$consumed;
        }
        
        Config.debugLog("------------------done recalculating stress ---------------------");

        cir.setReturnValue(flwls$newStress);

    }

    @Unique
    public float flwls$getCurrentStressMixin() {
        return this.currentStress;
    }

    @Unique
    public float flwls$getCurrentCapacityMixin() {
        return this.currentCapacity;
    }

    @Unique
    public float flwls$getCurrentStoresMixin() {
        return this.flwls$currentStores;
    }
}