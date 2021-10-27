package com.williambl.explosivessquared.forge;

import com.williambl.explosivessquared.ExplosivesSquared;
import com.williambl.explosivessquared.ExplosivesSquaredClient;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExplosivesSquared.modid)
public class ExplosivesSquaredForge {
    public ExplosivesSquaredForge() {
        EventBuses.registerModEventBus(ExplosivesSquared.modid, FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInitialiseClient);
        ExplosivesSquared.INSTANCE.init();
    }

    public void onInitialiseClient(FMLClientSetupEvent event) {
        ExplosivesSquaredClient.INSTANCE.initClient();
    }
}
