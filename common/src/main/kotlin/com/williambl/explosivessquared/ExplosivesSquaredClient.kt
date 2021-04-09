package com.williambl.explosivessquared

import com.williambl.explosivessquared.client.render.ExplosiveRenderer
import com.williambl.explosivessquared.client.render.GlassingRayBeamRenderer
import com.williambl.explosivessquared.objectholders.EntityTypeHolder
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
object ExplosivesSquaredClient {

    @SubscribeEvent
    public fun doClientStuff(event: FMLClientSetupEvent) {
        ExplosivesSquared.explosives.forEach {
            if (it.shouldCreateMissile)
                RenderTypeLookup.setRenderLayer(it.missileBlock, RenderType.getCutout())
            RenderTypeLookup.setRenderLayer(it.block, RenderType.getCutout())

            RenderingRegistry.registerEntityRenderingHandler(it.entityType, ::ExplosiveRenderer)
            if (it.shouldCreateMissile)
                RenderingRegistry.registerEntityRenderingHandler(it.missileEntityType, ::ExplosiveRenderer)
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityTypeHolder.glassingRayBeam, ::GlassingRayBeamRenderer)
    }

}