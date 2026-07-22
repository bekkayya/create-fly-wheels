package com.mae.create_fly_wheels;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import net.neoforged.neoforge.common.NeoForge;

@Mod("create_fly_wheels")
public final class CreateFlyWheels {

    public static final String MODID = "create_fly_wheels";
    public static final CreateRegistrate REGISTRATE=CreateRegistrate.create(MODID);

    public CreateFlyWheels(IEventBus ModEventBus, ModContainer container) {
        ModEventBus.addListener(this::commonSetup);
        REGISTRATE.registerEventListeners(ModEventBus);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        //NeoForge.EVENT_BUS.register(MaterialsHelper.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //createFlyWheels$LoadConfig();
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        //createFlyWheels$LoadConfig();
    }

    @SubscribeEvent
    public void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == Config.SPEC) {
            //createFlyWheels$CachedCall();
        }
    }
}
