package com.williambl.explosivessquared

import net.minecraft.block.Blocks
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.entity.item.TNTEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import kotlin.math.roundToInt

typealias ExplosionFunction = (ExplosiveEntity) -> Unit

fun regularExplosion(radius: Float): ExplosionFunction {
    return { it.world.createExplosion(it, it.posX, it.posY, it.posZ, radius, Explosion.Mode.DESTROY) }
}

fun vegetationDestroyerExplosion(radius: Double): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius.roundToInt())
                .filter { pos -> it.world.getBlockState(pos).isVegetation() }
                .forEach { pos ->
                    if (it.world.getBlockState(pos).isGrass())
                        it.world.setBlockState(pos, Blocks.DIRT.defaultState)
                    else
                        it.world.destroyBlock(pos, false)
                }
    }
}

fun gravitationalisingExplosion(radius: Double): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius.roundToInt())
                .forEach { pos ->
                    val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                    fallingEntity.setHurtEntities(true)
                    it.world.addEntity(fallingEntity)
                }
    }
}

fun tntRainingExplosion(amount: Int, spread: Double): ExplosionFunction {
    return {
        for (i in 0 until amount) {
            var pos = Vec3d(it.position)
            pos = pos.add((it.world.rand.nextDouble() - 0.5) * spread, 20.0, (it.world.rand.nextDouble() - 0.5) * spread)
            val tntEntity = TNTEntity(it.world, pos.x, pos.y, pos.z, null)
            it.world.addEntity(tntEntity)
        }
    }
}

fun repellingExplosion(radius: Double): ExplosionFunction {
    return {
        it.world.getEntitiesInSphere(it.position, radius, it)
                .forEach { entity ->
                    val speed = radius / (entity.positionVec.distanceTo(it.positionVec))
                    val velocityVector = entity.positionVec.subtract(it.positionVec).normalize().mul(speed, speed, speed)
                    entity.addVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                    entity.velocityChanged = true
                }
        it.position.getAllInSphere(radius.roundToInt())
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

fun attractingExplosion(radius: Double): ExplosionFunction {
    return {
        it.world.getEntitiesInSphere(it.position, radius)
                .forEach { entity ->
                    val speed = radius / (entity.positionVec.distanceTo(it.positionVec))
                    val velocityVector = it.positionVec.subtract(entity.positionVec).normalize().mul(speed, speed, speed)
                    entity.addVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                    entity.velocityChanged = true
                }
        it.position.getAllInSphere(radius.roundToInt())
                .forEach { pos ->
                    val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                    fallingEntity.setHurtEntities(true)
                    val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                    val velocityVector = it.positionVec.subtract(fallingEntity.positionVec).normalize().mul(speed, speed, speed)
                    fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                    it.world.addEntity(fallingEntity)
                }
    }
}