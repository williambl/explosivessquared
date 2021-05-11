package com.williambl.explosivessquared.forge;

import com.williambl.explosivessquared.ExplosivesSquared;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExplosivesSquared.modid)
public class ExplosivesSquaredForge {
    public ExplosivesSquaredForge() {
        EventBuses.registerModEventBus(ExplosivesSquared.modid, FMLJavaModLoadingContext.get().getModEventBus());
        ExplosivesSquared.INSTANCE.init();
    }
}
