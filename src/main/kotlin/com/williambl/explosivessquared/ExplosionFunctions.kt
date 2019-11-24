package com.williambl.explosivessquared

import net.minecraft.block.Blocks
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.entity.item.TNTEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion

typealias ExplosionFunction = (ExplosiveEntity) -> Unit

fun regularExplosion(radius: Float): ExplosionFunction {
    return { it.world.createExplosion(it, it.posX, it.posY, it.posZ, radius, Explosion.Mode.DESTROY) }
}

fun vegetationDestroyerExplosion(radius: Int): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius)
                .filter { pos -> it.world.getBlockState(pos).isVegetation() }
                .forEach { pos ->
                    if (it.world.getBlockState(pos).isGrass())
                        it.world.setBlockState(pos, Blocks.DIRT.defaultState)
                    else
                        it.world.destroyBlock(pos, false)
                }
    }
}

fun gravitationalisingExplosion(radius: Int): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius)
                .forEach { pos ->
                    val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                    fallingEntity.setHurtEntities(true)
                    it.world.addEntity(fallingEntity)
                }
    }
}

fun tntRainingExplosion(amount: Int, spread: Int): ExplosionFunction {
    return {
        for (i in 0 until amount) {
            var pos = Vec3d(it.position)
            pos = pos.add((it.world.rand.nextDouble() - 0.5) * spread, 20.0, (it.world.rand.nextDouble() - 0.5) * spread)
            val tntEntity = TNTEntity(it.world, pos.x, pos.y, pos.z, null)
            it.world.addEntity(tntEntity)
        }
    }
}

fun repellingExplosion(radius: Int): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius)
                .forEach { pos ->
                    val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                    fallingEntity.setHurtEntities(true)
                    val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                    val velocityVector = fallingEntity.positionVec.subtract(it.positionVec).normalize().mul(speed, speed, speed)
                    fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                    it.world.addEntity(fallingEntity)
                }
    }
}

fun attractingExplosion(radius: Int): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius)
                .forEach { pos ->
                    val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                    fallingEntity.setHurtEntities(true)
                    val speed = 8 / (fallingEntity.positionVec.distanceTo(it.positionVec))
                    val velocityVector = it.positionVec.subtract(fallingEntity.positionVec).normalize().mul(speed, speed, speed)
                    fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                    it.world.addEntity(fallingEntity)
                }
    }
}