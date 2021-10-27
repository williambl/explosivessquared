package com.williambl.explosivessquared.fabric;

import com.williambl.explosivessquared.ExplosivesSquared;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.ResourceLocation;

public class ExplosivesSquaredFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExplosivesSquared.INSTANCE.init();
    }
}
