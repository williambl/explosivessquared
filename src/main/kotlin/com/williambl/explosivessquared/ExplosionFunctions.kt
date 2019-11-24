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

val rainTNT: ExplosionFunction = {
    for (i in 0 until 10) {
        var pos = Vec3d(it.position)
        pos = pos.add((it.world.rand.nextDouble() - 0.5) * 5, 20.0, (it.world.rand.nextDouble() - 0.5) * 5)
        val tntEntity = TNTEntity(it.world, pos.x, pos.y, pos.z, null)
        it.world.addEntity(tntEntity)
    }
}

val repelBlocks: ExplosionFunction = {
    it.position.getAllInSphere(8)
            .forEach { pos ->
                val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                fallingEntity.setHurtEntities(true)
                val speed = 8 / (fallingEntity.positionVec.distanceTo(it.positionVec))
                val velocityVector = fallingEntity.positionVec.subtract(it.positionVec).normalize().mul(speed, speed, speed)
                fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                it.world.addEntity(fallingEntity)
            }
}

val attractBlocks: ExplosionFunction = {
    it.position.getAllInSphere(8)
            .forEach { pos ->
                val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, it.world.getBlockState(pos))
                fallingEntity.setHurtEntities(true)
                val speed = 8 / (fallingEntity.positionVec.distanceTo(it.positionVec))
                val velocityVector = it.positionVec.subtract(fallingEntity.positionVec).normalize().mul(speed, speed, speed)
                fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                it.world.addEntity(fallingEntity)
            }
}