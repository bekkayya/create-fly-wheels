package com.mae.create_fly_wheels.mixin;

import com.mae.create_fly_wheels.mixin_access.KineticNetworkAccess;
import com.mae.create_fly_wheels.mixin_access.FlywheelBlockEntityAccess;
import com.mae.create_fly_wheels.mixin_access.KineticBlockEntityAccess;


import static com.mae.create_fly_wheels.CreateFlyWheels.MODID;
import static net.minecraft.ChatFormatting.GRAY;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.lang.LangBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;

import com.llamalad7.mixinextras.sugar.Local;

import com.mae.create_fly_wheels.Config;
import com.mae.create_fly_wheels.MaterialMultipliers;

import net.minecraft.world.level.block.Block;
import java.lang.reflect.Field;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;



@Mixin(FlywheelBlockEntity.class)
public abstract class FlywheelBlockEntityMixin implements FlywheelBlockEntityAccess {
    //shadows

    @Shadow 
    LerpedFloat visualSpeed;

    @Shadow
    protected float angle;

    //variable uniques

    @Unique
    boolean flwls$paused;

    @Unique
    boolean flwls$pushing;

    @Unique
    float flwls$wheelSpeed;

    @Unique 
    float flwls$shaftSpeed;

    @Unique 
    float flwls$stores;

    @Unique 
    float flwls$maxStores;

    @Unique
    float flwls$storesPerTick;

    @Unique
    AABB flwls$renderBoundingBoxCached;

    @Unique 
    float flwls$packetThrottleTicksCounter;

    @Unique 
    float flwls$physicsThrottleTicksCounter;

    @Unique 
    Direction.Axis flwls$renderBoundingBoxCachedAxis;

    @Unique
    private Map<String, Float> flwls$networkedValues = new LinkedHashMap<>();

    @Unique
    private Map<String, String> flwls$networkedInfo = new LinkedHashMap<>();

    @Unique 
    private MaterialMultipliers flwls$flwlConfig;

    @Unique 
    private MaterialMultipliers flwls$flwlGlobal;

    @Unique 
    public MaterialMultipliers flwls$getConfigMultipliers() {
        return this.flwls$flwlConfig;
    }

    @Unique
    private void flwls$networkedValue(String key, Float value) {
        this.flwls$networkedValues.put(key, value);
    }

    @Unique
    public Map<String, Float> flwls$getNetworkedValues() {
        return this.flwls$networkedValues;
    }

    @Unique
    private void flwls$networkedInfo(String key, Object value) {
        this.flwls$networkedInfo.put(key, String.valueOf(value));
    }

    @Unique
    public Map<String, String> flwls$getNetworkedInfo() {
        return this.flwls$networkedInfo;
    }

    @Unique
    public void flwls$setCurrentStores(float stores) {
        this.flwls$stores = stores;
    }

    @Unique 
    public float flwls$getCurrentStores() {
        return this.flwls$stores;
    }

    @Unique
    public float flwls$getMaxStores() {
        return this.flwls$maxStores;
    }

    @Unique 
    public AABB getCustomFlywheelBounds() {
        FlywheelBlockEntity self = (FlywheelBlockEntity) (Object) this;

        BlockPos pos = self.getBlockPos();
        BlockState state = self.getBlockState();

        Vec3 center = Vec3.atCenterOf(pos);

        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        this.flwls$renderBoundingBoxCachedAxis = axis;

        double radius = 1.5 * this.flwls$flwlConfig.hitboxSize(this.flwls$flwlGlobal);
        double thickness = 0.25;

        return switch (axis) {
            case X -> new AABB(
                    center.x - thickness,
                    center.y - radius,
                    center.z - radius,
                    center.x + thickness,
                    center.y + radius,
                    center.z + radius
            );
            case Y -> new AABB(
                    center.x - radius,
                    center.y - thickness,
                    center.z - radius,
                    center.x + radius,
                    center.y + thickness,
                    center.z + radius
            );
            case Z -> new AABB(
                    center.x - radius,
                    center.y - radius,
                    center.z - thickness,
                    center.x + radius,
                    center.y + radius,
                    center.z + thickness
            );
        };
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        KineticBlockEntity flwls$self = (KineticBlockEntity)(Object)this;

        this.flwls$paused = false;
        this.flwls$pushing = false;

        this.flwls$wheelSpeed = 0;
        this.flwls$shaftSpeed = 0;
        this.flwls$stores = 0;
        this.flwls$maxStores = 0;
        this.flwls$storesPerTick = 0;
        this.flwls$packetThrottleTicksCounter = 0;
        this.flwls$physicsThrottleTicksCounter = 0;

        this.flwls$flwlConfig = Config.MATERIALS.get("default");
        this.flwls$flwlGlobal = Config.MATERIALS.get("global");

        //create the factory must grow compat
        if (ModList.get().isLoaded("tfmg")) {
            Block block = flwls$self.getBlockState().getBlock();
            ResourceLocation flwls$id = BuiltInRegistries.BLOCK.getKey(block);
            
            String flwls$material = Config.CTFMG_MATERIALS.get(flwls$id);
            if (flwls$material != null)
                this.flwls$flwlConfig = Config.MATERIALS.get(flwls$material);
        }

        this.flwls$renderBoundingBoxCached =  this.getCustomFlywheelBounds();

        flwls$networkedValue("internal_stores", 0f);
        flwls$networkedValue("wheel_speed", 0f);
        flwls$networkedValue("stores_per_tick", 0f);

        flwls$networkedInfo("material", this.flwls$flwlConfig.prettyName());
        flwls$networkedInfo("weight", this.flwls$flwlConfig.weight(this.flwls$flwlGlobal));
        flwls$networkedInfo("charging_loss", this.flwls$flwlConfig.chargingLoss(this.flwls$flwlGlobal));
        flwls$networkedInfo("internal_friction", this.flwls$flwlConfig.internalFriction(this.flwls$flwlGlobal));
        flwls$networkedInfo("max_in", this.flwls$flwlConfig.storesIn(this.flwls$flwlGlobal));
        flwls$networkedInfo("max_out", this.flwls$flwlConfig.storesOut(this.flwls$flwlGlobal));
        flwls$networkedInfo("base_load", this.flwls$flwlConfig.baseLoad(this.flwls$flwlGlobal));

        flwls$networkedInfo("push_strength", this.flwls$flwlConfig.pushStrength(this.flwls$flwlGlobal));
        flwls$networkedInfo("damage_multiplier", this.flwls$flwlConfig.baseDamage(this.flwls$flwlGlobal));
        flwls$networkedInfo("hitbox_size", this.flwls$flwlConfig.hitboxSize(this.flwls$flwlGlobal));
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void flwls$write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo cir) {
        CompoundTag flwls$infoTag = new CompoundTag();
        for (Map.Entry<String, String> flwls$entry : this.flwls$networkedInfo.entrySet()) {
            flwls$infoTag.putString(flwls$entry.getKey(), flwls$entry.getValue());
        }
        compound.put("networked_info", flwls$infoTag);


        CompoundTag flwls$valuesTag = new CompoundTag();
        for (Map.Entry<String, Float> flwls$entry : this.flwls$networkedValues.entrySet()) {
            flwls$valuesTag.putFloat(flwls$entry.getKey(), flwls$entry.getValue());
        }
        compound.put("networked_values", flwls$valuesTag);
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void flwls$read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo cir) {
        if (compound.contains("networked_info")) {
            CompoundTag flwls$mapTag = compound.getCompound("networked_info");
            for (String flwls$key : flwls$mapTag.getAllKeys()) {
                this.flwls$networkedInfo.put(flwls$key, flwls$mapTag.getString(flwls$key));
            }
        }

        if (compound.contains("networked_values")) {
            CompoundTag flwls$mapTag = compound.getCompound("networked_values");
            for (String flwls$key : flwls$mapTag.getAllKeys()) {
                this.flwls$networkedValues.put(flwls$key, flwls$mapTag.getFloat(flwls$key));
            }
        }

        this.flwls$stores = this.flwls$networkedValues.getOrDefault("internal_stores", -0.0f);

        if (clientPacket) {
            float flwls$serverWheelSpeed = this.flwls$networkedValues.getOrDefault("wheel_speed", -0.0f);
			visualSpeed.chase(flwls$serverWheelSpeed, 1 / 8f, Chaser.EXP);
        }
    }

    @Unique 
    public float calcMaxStores() {
        KineticBlockEntity self = (KineticBlockEntity)(Object)this;

        float flwls$shaftSpeed = Math.abs(self.getSpeed());
        float stressPerSpeed = Config.stressPerSpeed();
        float weightMultiplier = this.flwls$flwlConfig.weight();
        float maxInternalStores = flwls$shaftSpeed * (weightMultiplier * stressPerSpeed);

        this.flwls$maxStores = maxInternalStores;
        return maxInternalStores;
    }

    @Unique 
    public float flwls$consumeCurrentStores(float stressDelta) {

        //allow kinetic network to force an idle state
        if (stressDelta == 0) {
            this.flwls$storesPerTick = 0;
            flwls$networkedValue("stores_per_tick", 0f);
            return 0;
        }

        //do not make promises to a stopped network
        if(this.flwls$paused) {
            return 0;
        }

        calcMaxStores();

        KineticBlockEntity self = (KineticBlockEntity)(Object)this;

        float storesOverhead = Math.max(this.flwls$maxStores - this.flwls$stores, 0);
        //storesOverhead += this.flwls$flwlConfig.internalFriction(this.flwls$flwlGlobal) * Math.abs(this.flwls$wheelSpeed); //allow tax collector to collect internal stress losses 

        float consumption = 0;
        if (stressDelta > 0){
            float throughput = Math.min(this.flwls$flwlConfig.storesIn(this.flwls$flwlGlobal), storesOverhead);
            consumption = Math.min(stressDelta, throughput);
        }
        else {
            float throughput = -1 * Math.min(this.flwls$flwlConfig.storesOut(this.flwls$flwlGlobal), this.flwls$stores);
            consumption = Math.max(stressDelta, throughput);
        }

        Config.debugLog("flywheel at " + this.flwls$stores + "/" + this.flwls$maxStores + " was asked for " + stressDelta + " and gave " + consumption + "/tick");

        this.flwls$storesPerTick = consumption;
        flwls$networkedValue("stores_per_tick", this.flwls$storesPerTick);
        return consumption;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;tick()V", shift = At.Shift.AFTER), cancellable = true)
    private void flwls$onAfterSuperTick(CallbackInfo cir) {
        KineticBlockEntity flwls$self = (KineticBlockEntity)(Object)this;
        KineticBlockEntityAccess flwls$selfAccess = (KineticBlockEntityAccess) flwls$self;

        //clientside rendering
        assert flwls$self.getLevel() != null;
        if (flwls$self.getLevel().isClientSide()) {
            float flwls$serverWheelSpeed = this.flwls$networkedValues.getOrDefault("wheel_speed", -0.0f);
            visualSpeed.updateChaseTarget(flwls$serverWheelSpeed);
            visualSpeed.tickChaser();
            angle += visualSpeed.getValue() * 3 / 10f;
            angle %= 360;
        }
        //serverside logic
        else {

            calcMaxStores();

            var flwls$network = flwls$self.getOrCreateNetwork();
            //pause without updating
            if (flwls$network == null) {
                this.flwls$storesPerTick = 0;
                flwls$networkedValue("stores_per_tick", this.flwls$storesPerTick);
            }
            else {
                //pause with deferred updating
                if(flwls$selfAccess.isOverStressed()) {
                    this.flwls$storesPerTick = 0;
                    flwls$networkedValue("stores_per_tick", this.flwls$storesPerTick);
                    this.flwls$paused = true;

                    if (Config.overstressedIsHalt.get().booleanValue()) {
                        this.flwls$wheelSpeed = 0;
                        this.flwls$stores = 0;    
                    }        
                }
                else{
                    //pause with updating
                    boolean flwls$needGoIdle = false;

                    //if charging and full battery
                    if (this.flwls$storesPerTick > 0 && this.flwls$stores >= this.flwls$maxStores)
                        flwls$needGoIdle = true; 

                    //if discharging and out of battery
                    if(this.flwls$storesPerTick < 0 && this.flwls$stores <= 0) 
                        flwls$needGoIdle = true;

                    //when coming back from overstressed nudge the network to reprovision
                    if (this.flwls$paused == true){
                        this.flwls$paused = false;
                        flwls$needGoIdle = true;
                    }
                    
                    if (flwls$needGoIdle) {
                        Config.debugLog("flywheel at " + this.flwls$stores + "/" + this.flwls$maxStores + " that was giving " + this.flwls$storesPerTick + " must go idle");
                        
                        this.flwls$storesPerTick = 0;
                        flwls$networkedValue("stores_per_tick", this.flwls$storesPerTick);
                        flwls$network.updateNetwork();
                    }
                }
            }     

            //flywheels are dangerous
            this.flwls$physicsThrottleTicksCounter++;
            if (this.flwls$physicsThrottleTicksCounter > Config.physicsThrottleTicks()) {
                this.flwls$physicsThrottleTicksCounter = 0;

                if(Config.flywheelsAreDangerous.get().booleanValue() && this.flwls$renderBoundingBoxCached != null) { 

                    Level flwls$level = flwls$self.getLevel();

                    //check bounding box collisions
                    List<LivingEntity> flwls$entitiesInBounds = flwls$level.getEntitiesOfClass(LivingEntity.class, this.flwls$renderBoundingBoxCached);

                    if (flwls$entitiesInBounds.size() > 0 && this.flwls$wheelSpeed > 0 ) {
                        for (LivingEntity flwls$entity : flwls$entitiesInBounds) {
                            Vec3 flwls$entityPos = flwls$entity.position();
                            Vec3 flwls$radius = flwls$entityPos.subtract(Vec3.atCenterOf(flwls$self.getBlockPos()));
                            Vec3 flwls$pushDirection;

                            switch (this.flwls$renderBoundingBoxCachedAxis) {
                                case Y -> flwls$pushDirection = new Vec3(-flwls$radius.z, 0, flwls$radius.x);
                                case X ->  flwls$pushDirection = new Vec3(0, -flwls$radius.z, flwls$radius.y);
                                case Z -> flwls$pushDirection = new Vec3(-flwls$radius.y, flwls$radius.x, 0);
                                default -> flwls$pushDirection = Vec3.ZERO;
                            }

                            float flwls$speedFactor = this.flwls$wheelSpeed / 64.0f;
                            float flwls$pushStrength = this.flwls$flwlConfig.pushStrength(this.flwls$flwlGlobal) * flwls$speedFactor;
                            Config.debugLog("pushing strength " + flwls$pushStrength);

                            flwls$pushDirection = flwls$pushDirection.normalize().scale(flwls$pushStrength);
                            flwls$entity.setDeltaMovement(flwls$entity.getDeltaMovement().add(flwls$pushDirection));

                            float flwls$stressPerSpeed = Config.stressPerSpeed();
                            this.flwls$stores -= flwls$pushDirection.lengthSqr() * flwls$stressPerSpeed;

                            //flywheels are deadly
                            float flwls$damageMultiplier = this.flwls$flwlConfig.baseDamage(this.flwls$flwlGlobal);

                            if (Config.flywheelsAreDeadly.get().booleanValue()) {
                                //do more damage if on the ground
                                if (flwls$entity.onGround() && flwls$pushDirection.y < -0.1) {
                                    float flwls$damage = (float)(flwls$wheelSpeed / 4.0) * flwls$damageMultiplier;
                                    flwls$entity.hurt(flwls$level.damageSources().cramming(), flwls$damage);
                                }
                                else {
                                    double flwls$damage = (this.flwls$wheelSpeed / 16.0) * flwls$damageMultiplier;
                                    flwls$entity.hurt(flwls$level.damageSources().flyIntoWall(), (float) flwls$damage);
                                }
                            }

                            flwls$entity.hurtMarked = true; // sync velocity to client
                        }

                        this.flwls$pushing = true;
                    }
                    else {
                        //if stores were used for pushing and we're not charging, jostle the network
                        if (this.flwls$pushing == true) {
                            this.flwls$pushing = false;
                            this.flwls$paused = true;
                        }
                        
                    }
                }
            }

            //if the shaft stops spinning, continue at the previous rate
            float flwls$newShaftSpeed = flwls$self.getSpeed();
            if (flwls$newShaftSpeed != 0) {
                this.flwls$shaftSpeed = flwls$newShaftSpeed;
            }

            //only charging loss when charging
            if (this.flwls$storesPerTick > 0) {
                this.flwls$stores += this.flwls$storesPerTick / this.flwls$flwlConfig.chargingLoss(this.flwls$flwlGlobal);
            }
            else {
                this.flwls$stores += this.flwls$storesPerTick;
            }
            
            this.flwls$wheelSpeed = this.flwls$stores / (this.flwls$flwlConfig.weight(this.flwls$flwlGlobal) * Config.stressPerSpeed());

            //this.flwls$stores -= this.flwls$flwlConfig.internalFriction(this.flwls$flwlGlobal) * Math.abs(this.flwls$wheelSpeed);

            //dont allow out of bounds values it messes up the consume function (okay do allow over max for spinup funsies?)
            if (this.flwls$stores < 0)
                this.flwls$stores = 0;

            //write networked values to buffer
            flwls$networkedValue("internal_stores", this.flwls$stores);
            flwls$networkedValue("max_stores", this.flwls$maxStores);
            flwls$networkedValue("wheel_speed", this.flwls$wheelSpeed);

            this.flwls$packetThrottleTicksCounter++;
            if (this.flwls$packetThrottleTicksCounter > Config.packetThrottleTicks()) {
                this.flwls$packetThrottleTicksCounter = 0;
                //force network packet to send
                ((BlockEntity)(Object)this).setChanged();
                ((SmartBlockEntity)(Object)this).sendData(); 
            }

        }

        cir.cancel();
        return;
    }

    

}

//Config.debugLog("Hello, World!");
            //edge case network disconnected or stopped
