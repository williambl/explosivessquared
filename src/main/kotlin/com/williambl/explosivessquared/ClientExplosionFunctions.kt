package com.williambl.explosivessquared

import com.williambl.explosivessquared.util.getAllInSphere
import net.minecraft.client.world.ClientWorld
import net.minecraft.particles.ParticleTypes
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.SoundEvents

val explosionSound = playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, volume = 4.0f)

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

fun playSound(soundEvent: SoundEvent, pitch: Float = 1.0f, volume: Float = 1.0f): ExplosionFunction {
    return {
        val world = it.world
        if (world is ClientWorld) {
            world.playSound(it.position, soundEvent, SoundCategory.BLOCKS, volume, pitch, true)
        }
    }
}