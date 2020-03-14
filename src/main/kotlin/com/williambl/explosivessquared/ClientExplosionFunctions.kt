package com.williambl.explosivessquared

import net.minecraft.particles.ParticleTypes
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags

fun glassingRayClient(radius: Double): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius.toInt())
                .forEach { pos ->
                    val blockstate = it.world.getBlockState(pos)
                    val block = blockstate.block

                    if (FluidTags.WATER.contains(it.world.getFluidState(pos).fluid) || BlockTags.ICE.contains(block)) {
                        for (i in 0..20)
                            it.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 0.0, 0.0, 0.0)
                    }
                }
    }
}