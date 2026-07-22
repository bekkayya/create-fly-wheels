package com.mae.create_fly_wheels.mixin;

import static com.mae.create_fly_wheels.CreateFlyWheels.MODID;

import com.mae.create_fly_wheels.mixin_access.FlywheelBlockEntityAccess;
import com.mae.create_fly_wheels.mixin_access.KineticBlockEntityAccess;
import com.mae.create_fly_wheels.mixin_access.KineticNetworkAccess;

import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;

import com.mae.create_fly_wheels.Config;
import com.mae.create_fly_wheels.MaterialMultipliers;

import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.api.stress.BlockStressValues;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.LangBuilder;

import net.minecraft.ChatFormatting;
import static net.minecraft.ChatFormatting.GRAY;
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

import java.util.List;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Map;

import com.llamalad7.mixinextras.sugar.Local;

import com.mae.create_fly_wheels.Config;




@Mixin(KineticBlockEntity.class)
public abstract class KineticBlockEntityMixin implements KineticBlockEntityAccess {

    @Shadow protected float lastStressApplied;

    @Shadow
    public abstract KineticNetwork getOrCreateNetwork();

    @Shadow
    public abstract float getSpeed();

    @Inject(method = "calculateStressApplied", at = @At("RETURN"), cancellable = true)
	public void flwls$calculateStressApplied(CallbackInfoReturnable<Float> cir) {
        KineticBlockEntity flwls$self = (KineticBlockEntity)(Object)this;
        if (flwls$self instanceof FlywheelBlockEntity flwls$flywheel) {
            if (flwls$flywheel instanceof FlywheelBlockEntityAccess flwls$access) {
                float flwls$impact = flwls$access.flwls$getConfigMultipliers().baseLoad();
                this.lastStressApplied = flwls$impact;
                cir.setReturnValue(flwls$impact);
                cir.cancel();
            }
        }
	}

    @Unique
    @Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
    private boolean flwls$addToGoggleTooltipFlywheel(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        KineticBlockEntity self = (KineticBlockEntity) (Object) this;

        if (!(self instanceof FlywheelBlockEntity flwls$flywheel))
            return false;

        // Don't override the vanilla behavior if this BE is no longer valid.
        if (flwls$flywheel.isRemoved())
            return false;

        boolean flwls$added;
        try {
            flwls$added = flwls$addFlywheelInfo(tooltip, flwls$flywheel, isPlayerSneaking);
        } 
        catch (Exception e) {
            return false;
        }

        if (flwls$added) {
            cir.setReturnValue(true);
        }
        else{
            Config.debugLog("failed to add info");
        }

        cir.cancel();
        return true;
    }

    private boolean flwls$addFlywheelInfo(List<Component> tooltip, KineticBlockEntity self, boolean isPlayerSneaking) {

        if (!(self instanceof FlywheelBlockEntityAccess flwls$access))
            return false;

        CreateLang.translate("gui.goggles.kinetic_stats")
                .forGoggles(tooltip);

        Map<String, String> flwls$networkedInfo = flwls$access.flwls$getNetworkedInfo();
        Map<String, Float> flwls$networkedValues = flwls$access.flwls$getNetworkedValues();

        if (flwls$networkedValues == null || flwls$networkedInfo == null) {
            Config.debugLog("failed get values");
            return false;
        }
            

        // flwls$networkedInfo("material", this.flwls$flwlConfig.prettyName());
        // flwls$networkedInfo("weight", this.flwls$flwlConfig.weight(this.flwls$flwlGlobal));
        // flwls$networkedInfo("charging_loss", this.flwls$flwlConfig.chargingLoss(this.flwls$flwlGlobal));
        // flwls$networkedInfo("internal_friction", this.flwls$flwlConfig.internalFriction(this.flwls$flwlGlobal));
        // flwls$networkedInfo("max_in", this.flwls$flwlConfig.storesIn(this.flwls$flwlGlobal));
        // flwls$networkedInfo("max_out", this.flwls$flwlConfig.storesOut(this.flwls$flwlGlobal));
        // flwls$networkedInfo("base_load", this.flwls$flwlConfig.baseLoad(this.flwls$flwlGlobal));

        new LangBuilder(MODID)
                .add(Component.literal(flwls$networkedInfo.get("material")).withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        float displayStores = Math.round(flwls$networkedValues.get("internal_stores"));
        float displayMaxStores = Math.round(flwls$networkedValues.get("max_stores"));
        new LangBuilder(MODID)
                .add(Component.literal("Flywheel Momentum").withStyle(ChatFormatting.DARK_GRAY))
                .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .add(Component.literal(String.valueOf(displayStores) + " / " + String.valueOf(displayMaxStores)).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        float displayWheelSpeed = Math.round(flwls$networkedValues.get("wheel_speed"));
        new LangBuilder(MODID)
                .add(Component.literal("Wheel Speed").withStyle(ChatFormatting.DARK_GRAY))
                .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .add(Component.literal(String.valueOf(displayWheelSpeed)).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        float displayStoresPerTick = Math.round(flwls$networkedValues.get("stores_per_tick"));
        new LangBuilder(MODID)
                .add(Component.literal("Input Output / Tick").withStyle(ChatFormatting.DARK_GRAY))
                .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .add(Component.literal(String.valueOf(displayStoresPerTick)).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        new LangBuilder(MODID)
                .add(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        if (isPlayerSneaking) {
            new LangBuilder(MODID)
                    .add(Component.literal("Weight").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(flwls$networkedInfo.get("weight")).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            new LangBuilder(MODID)
                    .add(Component.literal("Input Loss").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(flwls$networkedInfo.get("charging_loss")).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            new LangBuilder(MODID)
                    .add(Component.literal("Internal Friction").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(flwls$networkedInfo.get("internal_friction")).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            new LangBuilder(MODID)
                    .add(Component.literal("Maximum Input / Output").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(flwls$networkedInfo.get("max_in") + " / " + flwls$networkedInfo.get("max_out")).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);



            new LangBuilder(MODID)
                    .add(Component.literal("Pushing Strength").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(flwls$networkedInfo.get("push_strength")).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            new LangBuilder(MODID)
                    .add(Component.literal("Damage Multiplier").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(flwls$networkedInfo.get("damage_multiplier")).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            new LangBuilder(MODID)
                    .add(Component.literal("Hitbox Size").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(flwls$networkedInfo.get("hitbox_size")).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            float targetSpeed = Math.abs(getSpeed());
            String basedLoad = String.valueOf(Float.valueOf(flwls$networkedInfo.get("base_load")) * targetSpeed);
            new LangBuilder(MODID)
                    .add(Component.literal("Base Load").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(basedLoad).withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
        }

        return tooltip.size() > 0;
    }
}

