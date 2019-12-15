package com.williambl.explosivessquared

import net.minecraft.block.Blocks
import net.minecraft.block.IGrowable
import net.minecraft.entity.EntityType
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.entity.item.TNTEntity
import net.minecraft.entity.monster.MagmaCubeEntity
import net.minecraft.entity.monster.SlimeEntity
import net.minecraft.entity.monster.ZombiePigmanEntity
import net.minecraft.entity.passive.PigEntity
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
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
        it.world.getEntitiesInSphere(it.position, radius, it)
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

fun napalmExplosion(radius: Double): ExplosionFunction {
    return {
        it.world.createExplosion(it, it.posX, it.posY, it.posZ, (radius / 2).toFloat(), Explosion.Mode.DESTROY)
        it.position.getAllInSphere(radius.roundToInt())
                .filter { pos -> it.world.getBlockState(pos).isAir(it.world, pos) }
                .forEach { pos ->
                    it.world.setBlockState(pos, Blocks.FIRE.defaultState)
                }
    }
}

fun frostExplosion(radius: Double): ExplosionFunction {
    return {
        it.position.getAllInSphere(radius.roundToInt())
                .filter { pos -> !it.world.getBlockState(pos).isAir(it.world, pos) }
                .forEach { pos ->
                    when (it.world.getBlockState(pos).block) {
                        Blocks.ICE -> it.world.setBlockState(pos, Blocks.PACKED_ICE.defaultState)
                        Blocks.FIRE -> it.world.removeBlock(pos, false)
                        Blocks.MAGMA_BLOCK -> it.world.setBlockState(pos, Blocks.NETHERRACK.defaultState)
                    }

                    if (it.world.getFluidState(pos).fluid.tags.contains(ResourceLocation("minecraft:water")))
                        it.world.setBlockState(pos, Blocks.ICE.defaultState)
                    if (it.world.getFluidState(pos).fluid.tags.contains(ResourceLocation("minecraft:lava")))
                        it.world.setBlockState(pos, Blocks.STONE.defaultState)

                    if (it.world.getBlockState(pos.up()).isAir(it.world, pos) && Blocks.SNOW.isValidPosition(Blocks.SNOW.defaultState, it.world, pos.up()))
                        it.world.setBlockState(pos.up(), Blocks.SNOW.defaultState)

                    if (it.world.rand.nextDouble() < 0.05)
                        it.world.setBlockState(pos, Blocks.ICE.defaultState)
                }
        it.world.getEntitiesInSphere(it.position, radius, it)
                .forEach { entity ->
                    var damage =
                            (if (entity.position == it.position)
                                100 * radius
                            else
                                radius / entity.getDistance(it)).toFloat()

                    if (entity.type == EntityType.BLAZE || entity.type == EntityType.MAGMA_CUBE)
                        damage *= 3

                    entity.attackEntityFrom(DamageSource("frost"), damage)

                }
    }
}

fun netherExplosion(radius: Double): ExplosionFunction {
    return {
        if (!it.world.dimension.isNether) {
            it.position.getAllInSphere(radius.roundToInt())
                    .filter { pos -> !it.world.getBlockState(pos).isAir(it.world, pos) }
                    .forEach { pos ->
                        val block = it.world.getBlockState(pos).block
                        when {
                            block.tags.contains(ResourceLocation("forge:sand")) -> it.world.setBlockState(pos, Blocks.SOUL_SAND.defaultState)
                            block == Blocks.CLAY -> it.world.setBlockState(pos, Blocks.MAGMA_BLOCK.defaultState)
                            block == Blocks.BRICKS -> it.world.setBlockState(pos, Blocks.NETHER_BRICKS.defaultState)
                            block.tags.contains(ResourceLocation("minecraft:dirt_like")) -> it.world.setBlockState(pos, Blocks.NETHERRACK.defaultState)
                            block is IGrowable -> it.world.setBlockState(pos, Blocks.NETHER_WART.defaultState)
                            block.tags.contains(ResourceLocation("minecraft:logs")) -> it.world.setBlockState(pos, Blocks.COBBLESTONE.defaultState)
                            block.tags.contains(ResourceLocation("minecraft:leaves")) -> it.world.setBlockState(pos, Blocks.COBBLESTONE.defaultState)
                            block.tags.contains(ResourceLocation("minecraft:ice")) -> it.world.setBlockState(pos, Blocks.STONE.defaultState)
                        }

                        if (it.world.getFluidState(pos).fluid.tags.contains(ResourceLocation("minecraft:water")))
                            it.world.setBlockState(pos, Blocks.LAVA.defaultState)


                        if (it.world.rand.nextDouble() < 0.05)
                            it.world.setBlockState(pos, Blocks.NETHERRACK.defaultState)

                    }
            it.world.getEntitiesInSphere(it.position, radius, it)
                    .forEach { entity ->

                        if (entity.type == EntityType.SLIME) {
                            val newEntity = object : MagmaCubeEntity(EntityType.MAGMA_CUBE, entity.world) {
                                init {
                                    setSlimeSize((entity as SlimeEntity).slimeSize, true)
                                }
                            }
                            newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                            newEntity.customName = entity.customName
                            newEntity.isCustomNameVisible = entity.isCustomNameVisible
                            entity.world.addEntity(newEntity)
                            entity.remove()
                        }

                        if (entity.type == EntityType.PIG) {
                            val newEntity = ZombiePigmanEntity(EntityType.ZOMBIE_PIGMAN, entity.world)
                            newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                            newEntity.customName = entity.customName
                            newEntity.isCustomNameVisible = entity.isCustomNameVisible
                            entity.world.addEntity(newEntity)
                            entity.removed = true
                        }

                    }
        } else {
            it.position.getAllInSphere(radius.roundToInt())
                    .filter { pos -> !it.world.getBlockState(pos).isAir(it.world, pos) }
                    .forEach { pos ->
                        val block = it.world.getBlockState(pos).block
                        when {
                            block == Blocks.SOUL_SAND -> it.world.setBlockState(pos, Blocks.SAND.defaultState)
                            block == Blocks.MAGMA_BLOCK -> it.world.setBlockState(pos, Blocks.CLAY.defaultState)
                            block == Blocks.NETHER_BRICKS -> it.world.setBlockState(pos, Blocks.BRICKS.defaultState)
                            block == Blocks.NETHERRACK -> it.world.setBlockState(pos, Blocks.DIRT.defaultState)
                        }

                        if (it.world.getFluidState(pos).fluid.tags.contains(ResourceLocation("minecraft:lava")))
                            it.world.setBlockState(pos, Blocks.WATER.defaultState)


                        if (it.world.rand.nextDouble() < 0.05)
                            it.world.setBlockState(pos, Blocks.GRASS_BLOCK.defaultState)

                    }
            it.world.getEntitiesInSphere(it.position, radius, it)
                    .forEach { entity ->

                        if (entity.type == EntityType.MAGMA_CUBE) {
                            val newEntity = object : SlimeEntity(EntityType.SLIME, entity.world) {
                                init {
                                    setSlimeSize((entity as MagmaCubeEntity).slimeSize, true)
                                }
                            }
                            newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                            newEntity.customName = entity.customName
                            newEntity.isCustomNameVisible = entity.isCustomNameVisible
                            entity.world.addEntity(newEntity)
                            entity.remove()
                        }

                        if (entity.type == EntityType.ZOMBIE_PIGMAN) {
                            val newEntity = PigEntity(EntityType.PIG, entity.world)
                            newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                            newEntity.customName = entity.customName
                            newEntity.isCustomNameVisible = entity.isCustomNameVisible
                            entity.world.addEntity(newEntity)
                            entity.removed = true
                        }

                    }
        }

        val blockPlacingPos = BlockPos.MutableBlockPos(it.position)
        blockPlacingPos.move(Direction.UP).move(Direction.UP).move(Direction.UP)
        it.world.setBlockState(blockPlacingPos.move(Direction.UP), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.WEST), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.WEST), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.DOWN), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.DOWN), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.DOWN), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.DOWN), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.EAST), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.EAST), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.EAST), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.UP), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.UP), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.UP), Blocks.OBSIDIAN.defaultState)
        it.world.setBlockState(blockPlacingPos.move(Direction.UP), Blocks.OBSIDIAN.defaultState)
    }
}