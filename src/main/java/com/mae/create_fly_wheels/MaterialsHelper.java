package com.mae.create_fly_wheels;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class MaterialsHelper {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();

        var itemRegistry = server.registryAccess()
                .registryOrThrow(Registries.ITEM);

        System.out.println("===== INGOT TAGS START =====");

        itemRegistry.getTagNames()
                .filter(tagKey -> tagKey.location().getPath().contains("ingots"))
                .map(tagKey -> tagKey.location().toString())
                .sorted()
                .forEach(System.out::println);

        System.out.println("===== INGOT TAGS END =====");
    }
}