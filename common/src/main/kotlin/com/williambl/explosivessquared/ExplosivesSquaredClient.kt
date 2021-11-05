package com.williambl.explosivessquared

import com.williambl.explosivessquared.client.render.AntiGravityBlockRenderer
import com.williambl.explosivessquared.client.render.ExplosiveRenderer
import com.williambl.explosivessquared.client.render.GlassingRayBeamRenderer
import com.williambl.explosivessquared.entity.ExplosiveEntity
import me.shedaniel.architectury.registry.RenderTypes
import me.shedaniel.architectury.registry.entity.EntityRenderers
import net.minecraft.client.renderer.RenderType
import net.minecraft.entity.EntityType

object ExplosivesSquaredClient {
    @Suppress("UNCHECKED_CAST")
    fun initClient() {
        ExplosivesSquared.explosives.forEach {
            if (it.shouldCreateMissile)
                RenderTypes.register(RenderType.getCutout(), it.missileBlock)
            RenderTypes.register(RenderType.getCutout(), it.block)

            EntityRenderers.register(it.entityType as EntityType<ExplosiveEntity>, ::ExplosiveRenderer)
            if (it.shouldCreateMissile) {
                EntityRenderers.register(it.missileEntityType as EntityType<ExplosiveEntity>, ::ExplosiveRenderer)
            }
        }
        EntityRenderers.register(ExplosivesSquared.glassingRayBeam.get(), ::GlassingRayBeamRenderer)
        EntityRenderers.register(ExplosivesSquared.antigravityBlock.get(), ::AntiGravityBlockRenderer)
    }

}