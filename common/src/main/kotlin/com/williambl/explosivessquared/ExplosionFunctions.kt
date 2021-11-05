package com.williambl.explosivessquared

import com.williambl.explosivessquared.entity.AntigravityBlockEntity
import com.williambl.explosivessquared.entity.ExplosiveEntity
import com.williambl.explosivessquared.entity.GlassingRayBeamEntity
import com.williambl.explosivessquared.util.*
import com.williambl.explosivessquared.util.actions.*
import net.minecraft.block.Blocks
import net.minecraft.block.IGrowable
import net.minecraft.block.SpreadableSnowyDirtBlock
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.entity.item.TNTEntity
import net.minecraft.entity.monster.MagmaCubeEntity
import net.minecraft.entity.monster.SlimeEntity
import net.minecraft.entity.monster.piglin.PiglinEntity
import net.minecraft.entity.passive.PigEntity
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.Effects
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.DimensionType
import net.minecraft.world.Explosion
import kotlin.math.roundToInt

typealias ExplosionFunction = (ExplosiveEntity) -> Unit

fun combine(vararg funcs: ExplosionFunction): ExplosionFunction {
    return {
        funcs.forEach { func -> func(it) }
    }
}

fun regularExplosion(radius: Float): ExplosionFunction {
    return { it.world.createExplosion(it, it.posX, it.posY, it.posZ, radius, Explosion.Mode.DESTROY) }
}

fun vegetationDestroyerExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.roundToInt()))
                .addFilter(isNotAir)
                .addAction(BlockMappingAction(isOfType<SpreadableSnowyDirtBlock>(), Blocks.DIRT))
                .addAction(BlockRemovalAction(isVegetation))
                .start()
    }
}

fun gravitationalisingExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                .addFilter(isNotAir)
                .addAction(BlockFunctionAction(isNotUnbreakable) { world, pos ->
                    val fallingEntity = FallingBlockEntity(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, world.getBlockState(pos))
                    fallingEntity.setHurtEntities(true)
                    world.addEntity(fallingEntity)
                    Blocks.AIR.defaultState
                })
                .start()
    }
}

fun tntRainingExplosion(amount: Int, spread: Double): ExplosionFunction {
    return {
        for (i in 0 until amount) {
            val pos = it.positionVec.add((it.world.rand.nextDouble() - 0.5) * spread, 20.0, (it.world.rand.nextDouble() - 0.5) * spread)
            val tntEntity = TNTEntity(it.world, pos.x, pos.y, pos.z, null)
            it.world.addEntity(tntEntity)
        }
    }
}

fun repellingExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                .addFilter(isNotAir)
                .addAction(BlockFunctionAction(isNotUnbreakable) { world, pos ->
                    val blockState = world.getBlockState(pos)
                    if (world.canExplosionDestroy(radius.toInt(), it.position, pos, it)) {
                        val fallingEntity = FallingBlockEntity(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, blockState)
                        fallingEntity.setHurtEntities(true)
                        fallingEntity.isInvulnerable = true
                        val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                        val velocityVector = fallingEntity.positionVec.subtract(it.positionVec).normalize().mul(speed, speed, speed)
                        fallingEntity.setMotion(velocityVector.x, velocityVector.y, velocityVector.z)
                        world.addEntity(fallingEntity)
                    }
                    blockState
                })
                .start()
        it.world.getEntitiesInSphere(it.position, radius, it)
                .forEach { entity ->
                    val speed = radius / (entity.positionVec.distanceTo(it.positionVec))
                    val velocityVector = entity.positionVec.subtract(it.positionVec).normalize().mul(speed, speed, speed)
                    entity.addVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                    entity.velocityChanged = true
                }
    }
}

fun attractingExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                .addFilter(isNotAir)
                .addAction(BlockFunctionAction(isNotUnbreakable) { world, pos ->
                    val blockState = world.getBlockState(pos)
                    if (world.canExplosionDestroy(radius.toInt(), it.position, pos, it)) {
                        val fallingEntity = FallingBlockEntity(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, blockState)
                        fallingEntity.setHurtEntities(true)
                        fallingEntity.isInvulnerable = true
                        val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                        val velocityVector = it.positionVec.subtract(fallingEntity.positionVec).normalize().mul(speed, speed, speed)
                        fallingEntity.setMotion(velocityVector.x, velocityVector.y, velocityVector.z)
                        world.addEntity(fallingEntity)
                    }
                    blockState
                })
                .start()
        it.world.getEntitiesInSphere(it.position, radius, it)
                .forEach { entity ->
                    val speed = radius / (entity.positionVec.distanceTo(it.positionVec))
                    val velocityVector = it.positionVec.subtract(entity.positionVec).normalize().mul(speed, speed, speed)
                    entity.addVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                    entity.velocityChanged = true
                }
    }
}

fun antiGravityExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
            .addFilter(isNotAir)
            .addAction(BlockFunctionAction(isNotUnbreakable) { world, pos ->
                val blockState = world.getBlockState(pos)
                val fallingEntity = AntigravityBlockEntity(world, pos, blockState)
                world.addEntity(fallingEntity)
                Blocks.AIR.defaultState
            })
            .start()
        it.world.getEntitiesInSphere(it.position, radius, it)
            .filterIsInstance(LivingEntity::class.java)
            .forEach { entity ->
                entity.addPotionEffect(EffectInstance(Effects.LEVITATION, 100))
            }
    }
}

fun napalmExplosion(radius: Double): ExplosionFunction {
    return {
        it.world.createExplosion(it, it.posX, it.posY, it.posZ, (radius / 2).toFloat(), Explosion.Mode.DESTROY)
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                .addAction(BlockFunctionAction(isAir) { _, _ -> Blocks.FIRE.defaultState })
                .start()
    }
}

fun frostExplosion(radius: Double): ExplosionFunction {
    return {
        val mapping = BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.roundToInt()))
                .addFilter(isNotAir)
                .addAction(BlockMappingAction(Blocks.ICE, Blocks.PACKED_ICE))
                .addAction(BlockMappingAction(Blocks.FIRE, Blocks.AIR))
                .addAction(BlockMappingAction(Blocks.MAGMA_BLOCK, Blocks.NETHERRACK))
                .addAction(BlockMappingAction(random(0.05), Blocks.ICE))

                .addAction(BlockMappingAction(Blocks.WATER, Blocks.ICE))
                .addAction(BlockMappingAction(Blocks.LAVA, Blocks.STONE))
                .start()
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.roundToInt()))
            .addFilter(isAir)
            .addAction(BlockFunctionAction({ world, pos, state ->
                Blocks.SNOW.isValidPosition(Blocks.SNOW.defaultState, world, pos)
            }, { world, pos -> Blocks.SNOW.defaultState  }))
            .start()

        it.world.getEntitiesInSphere(it.position, radius, it)
                .forEach { entity ->
                    var damage =
                            (if (entity.position == it.position)
                                100 * radius
                            else
                                radius / entity.getDistance(it)).toFloat()

                    if (entity.type == EntityType.BLAZE || entity.type == EntityType.MAGMA_CUBE)
                        damage *= 3

                    entity.attackEntityFrom(object : DamageSource("frost") {}, damage)

                }
    }
}

fun netherExplosion(radius: Double): ExplosionFunction {
    return {
        if (it.world.dimensionKey != DimensionType.THE_NETHER) {
            val mapping = BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                    .addFilter(isNotAir)
                    .addAction(BlockMappingAction(BlockTags.SAND, Blocks.SOUL_SAND))
                    .addAction(BlockMappingAction(Blocks.CLAY, Blocks.MAGMA_BLOCK))
                    .addAction(BlockMappingAction(Blocks.BRICKS, Blocks.NETHER_BRICKS))
                    .addAction(BlockMappingAction(Blocks.DIRT, Blocks.NETHERRACK))
                    .addAction(BlockMappingAction(isOfType<SpreadableSnowyDirtBlock>(), Blocks.NETHER_WART_BLOCK))
                    .addAction(BlockMappingAction(isOfType<IGrowable>(), Blocks.NETHER_WART))
                    .addAction(BlockMappingAction(BlockTags.LOGS, Blocks.COBBLESTONE))
                    .addAction(BlockMappingAction(BlockTags.LEAVES, Blocks.COBBLESTONE))
                    .addAction(BlockMappingAction(BlockTags.ICE, Blocks.STONE))
                    .addAction(BlockMappingAction(Blocks.FARMLAND, Blocks.SOUL_SAND))
                    .addAction(BlockMappingAction(Blocks.GRASS_PATH, Blocks.COBBLESTONE))
                    .addAction(BlockMappingAction(Blocks.WATER, Blocks.LAVA))
                    .addAction(BlockMappingAction(random(0.05), Blocks.NETHERRACK))
                    .start()
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
                            val newEntity = PiglinEntity(EntityType.PIGLIN, entity.world)
                            newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                            newEntity.customName = entity.customName
                            newEntity.isCustomNameVisible = entity.isCustomNameVisible
                            entity.world.addEntity(newEntity)
                            entity.removed = true
                        }

                    }
        } else {
            val mapping = BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                    .addFilter(isNotAir)
                    .addAction(BlockMappingAction(Blocks.SOUL_SAND, Blocks.SAND))
                    .addAction(BlockMappingAction(Blocks.MAGMA_BLOCK, Blocks.CLAY))
                    .addAction(BlockMappingAction(Blocks.NETHER_BRICKS, Blocks.BRICKS))
                    .addAction(BlockMappingAction(Blocks.NETHERRACK, Blocks.DIRT))
                    .addAction(BlockMappingAction(Blocks.NETHER_WART, Blocks.DEAD_BUSH))
                    .addAction(BlockMappingAction(Blocks.NETHER_WART_BLOCK, Blocks.GRASS_BLOCK))
                    .addAction(BlockMappingAction(Blocks.LAVA, Blocks.WATER))
                    .addAction(BlockMappingAction(random(0.05), Blocks.GRASS_BLOCK))
                    .start()
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

                        if (entity.type == EntityType.PIGLIN) {
                            val newEntity = PigEntity(EntityType.PIG, entity.world)
                            newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                            newEntity.customName = entity.customName
                            newEntity.isCustomNameVisible = entity.isCustomNameVisible
                            entity.world.addEntity(newEntity)
                            entity.removed = true
                        }

                    }
        }

        if (it.world.rand.nextDouble() < 0.1) {
            val blockPlacingPos = BlockPos.Mutable().setPos(it.position)
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
}

fun glassingRay(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                .addAction(BlockMappingAction(BlockTags.SAND, glassTag))
                .addAction(BlockMappingAction(gravelTag, BlockTags.BASE_STONE_OVERWORLD))
                .addAction(BlockMappingAction(Blocks.CLAY, Blocks.TERRACOTTA))
                .addAction(BlockMappingAction(isOfType<SpreadableSnowyDirtBlock>(), Blocks.DIRT))
                .addAction(BlockMappingAction(Blocks.GRASS_PATH, Blocks.COARSE_DIRT))
                .addAction(BlockMappingAction(Blocks.COBBLESTONE, Blocks.STONE))
                .addAction(BlockMappingAction(combine(isAir, fiftyFifty), Blocks.FIRE))
                .addAction(BlockRemovalAction(BlockTags.ICE))
                .addAction(BlockMappingAction(BlockTags.BASE_STONE_OVERWORLD, listOf(Blocks.LAVA, Blocks.OBSIDIAN)))
                .addAction(BlockMappingAction(Blocks.DIRT, listOf(Blocks.MAGMA_BLOCK, Blocks.COARSE_DIRT)))
                .addAction(BlockRemovalAction { world, pos, _ -> FluidTags.WATER.contains(world.getFluidState(pos).fluid) })
                .start()

        it.world.addEntity(GlassingRayBeamEntity(ExplosivesSquared.glassingRayBeam.get(), it.world, it.posX, it.posY, it.posZ))
    }
}

fun removeAllBlocks(radius: Double): ExplosionFunction {
    return {
        MassBlockActionManager(it.world, it.position.getAllInSphereSeq(radius.toInt()))
                .addFilter(isNotAir)
                .addAction(BlockRemovalAction { _, _, _ -> true })
                .start()
    }
}
