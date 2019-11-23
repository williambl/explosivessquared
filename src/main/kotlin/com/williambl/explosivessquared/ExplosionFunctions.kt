package com.williambl.explosivessquared

import net.minecraft.block.Blocks
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.world.Explosion

typealias ExplosionFunction = (ExplosiveEntity) -> Unit

fun regularExplosion(radius: Float): ExplosionFunction {
    return { it.world.createExplosion(it, it.posX, it.posY, it.posZ, radius, Explosion.Mode.DESTROY) }
}

val createCake: ExplosionFunction = { it.world.setBlockState(it.position, Blocks.CAKE.defaultState) }

val destroyVegetation: ExplosionFunction = {
    it.position.getAllInSphere(8)
            .filter { pos -> it.world.getBlockState(pos).isVegetation() }
            .forEach { pos ->
                if (it.world.getBlockState(pos).isGrass())
                    it.world.setBlockState(pos, Blocks.DIRT.defaultState)
                else
                    it.world.destroyBlock(pos, false)
            }
}

val makeBlocksFall: ExplosionFunction = {
    it.position.getAllInSphere(8)
            .forEach { pos ->
                val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                fallingEntity.setHurtEntities(true)
                it.world.addEntity(fallingEntity)
            }
}