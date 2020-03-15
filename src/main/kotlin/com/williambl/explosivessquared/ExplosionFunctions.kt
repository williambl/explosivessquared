package com.williambl.explosivessquared

import com.williambl.explosivessquared.entity.ExplosiveEntity
import com.williambl.explosivessquared.entity.GlassingRayBeamEntity
import com.williambl.explosivessquared.objectholders.EntityTypeHolder
import com.williambl.explosivessquared.util.actions.BlockActionManager
import com.williambl.explosivessquared.util.actions.BlockFunctionAction
import com.williambl.explosivessquared.util.actions.BlockMappingAction
import com.williambl.explosivessquared.util.actions.BlockRemovalAction
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.IGrowable
import net.minecraft.block.SpreadableSnowyDirtBlock
import net.minecraft.entity.EntityType
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.entity.item.TNTEntity
import net.minecraft.entity.monster.MagmaCubeEntity
import net.minecraft.entity.monster.SlimeEntity
import net.minecraft.entity.monster.ZombiePigmanEntity
import net.minecraft.entity.passive.PigEntity
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import net.minecraft.world.World
import net.minecraftforge.common.Tags
import kotlin.math.roundToInt

typealias ExplosionFunction = (ExplosiveEntity) -> Unit

fun regularExplosion(radius: Float): ExplosionFunction {
    return { it.world.createExplosion(it, it.posX, it.posY, it.posZ, radius, Explosion.Mode.DESTROY) }
}

fun vegetationDestroyerExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphere(radius.roundToInt()))
                .addAction(BlockMappingAction(Blocks.GRASS, Blocks.DIRT))
                .addAction(BlockRemovalAction { _, _, state -> !state.isGrass() })
                .start()
    }
}

fun gravitationalisingExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphere(radius.toInt()))
                .addAction(BlockFunctionAction({ _, _, _ -> true }, { world, pos ->
                    val fallingEntity = FallingBlockEntity(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, world.getBlockState(pos))
                    fallingEntity.setHurtEntities(true)
                    world.addEntity(fallingEntity)
                }))
                .start()
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
        BlockActionManager(it.world, it.position.getAllInSphere(radius.toInt()))
                .addAction(BlockFunctionAction({ _, _, _ -> true }, { world, pos ->
                    val blockState = world.getBlockState(pos)
                    if (world.canExplosionDestroy(radius.toInt(), it.position, pos, it)) {
                        val fallingEntity = FallingBlockEntity(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, blockState)
                        fallingEntity.setHurtEntities(true)
                        fallingEntity.isInvulnerable = true
                        val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                        val velocityVector = fallingEntity.positionVec.subtract(it.positionVec).normalize().mul(speed, speed, speed)
                        fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                        world.addEntity(fallingEntity)
                    }
                }))
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
        BlockActionManager(it.world, it.position.getAllInSphere(radius.toInt()))
                .addAction(BlockFunctionAction({ _, _, _ -> true }, { world, pos ->
                    val blockState = world.getBlockState(pos)
                    if (world.canExplosionDestroy(radius.toInt(), it.position, pos, it)) {
                        val fallingEntity = FallingBlockEntity(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, blockState)
                        fallingEntity.setHurtEntities(true)
                        fallingEntity.isInvulnerable = true
                        val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                        val velocityVector = it.positionVec.subtract(fallingEntity.positionVec).normalize().mul(speed, speed, speed)
                        fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                        world.addEntity(fallingEntity)
                    }
                }))
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

fun napalmExplosion(radius: Double): ExplosionFunction {
    return {
        BlockActionManager(it.world, it.position.getAllInSphere(radius.toInt()))
                .addAction(BlockFunctionAction({ world, pos, state -> state.isAir(world, pos) }, { world, pos ->
                    world.setBlockState(pos, Blocks.FIRE.defaultState)
                }))
                .start()
        it.world.createExplosion(it, it.posX, it.posY, it.posZ, (radius / 2).toFloat(), Explosion.Mode.DESTROY)
    }
}

fun frostExplosion(radius: Double): ExplosionFunction {
    return {
        val mapping = BlockActionManager(it.world, it.position.getAllInSphere(radius.roundToInt()))
                .addAction(BlockMappingAction(Blocks.ICE, Blocks.PACKED_ICE))
                .addAction(BlockMappingAction(Blocks.FIRE, Blocks.AIR))
                .addAction(BlockMappingAction(Blocks.MAGMA_BLOCK, Blocks.NETHERRACK))
                .addAction(BlockMappingAction({ world, _, _ -> world.rand.nextDouble() < 0.05 }, Blocks.ICE))
                .addAction(BlockMappingAction({ world, pos, _ ->
                    world.getBlockState(pos.up()).isAir(world, pos) && Blocks.SNOW.isValidPosition(Blocks.SNOW.defaultState, world, pos.up())
                }, Blocks.SNOW))
                .addAction(BlockMappingAction(Blocks.WATER, Blocks.ICE))
                .addAction(BlockMappingAction(Blocks.LAVA, Blocks.STONE))
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

                    entity.attackEntityFrom(DamageSource("frost"), damage)

                }
    }
}

fun netherExplosion(radius: Double): ExplosionFunction {
    return {
        if (!it.world.dimension.isNether) {
            val mapping = BlockActionManager(it.world, it.position.getAllInSphere(radius.toInt()))
                    .addAction(BlockMappingAction(Tags.Blocks.SAND, Blocks.SOUL_SAND))
                    .addAction(BlockMappingAction(Blocks.CLAY, Blocks.MAGMA_BLOCK))
                    .addAction(BlockMappingAction(Blocks.BRICKS, Blocks.NETHER_BRICKS))
                    .addAction(BlockMappingAction(Blocks.DIRT, Blocks.NETHERRACK))
                    .addAction(BlockMappingAction({ _, _, state -> state.block is SpreadableSnowyDirtBlock }, Blocks.NETHER_WART_BLOCK))
                    .addAction(BlockMappingAction({ _, _, state -> state.block is IGrowable }, Blocks.NETHER_WART))
                    .addAction(BlockMappingAction(BlockTags.LOGS, Blocks.COBBLESTONE))
                    .addAction(BlockMappingAction(BlockTags.LEAVES, Blocks.COBBLESTONE))
                    .addAction(BlockMappingAction(BlockTags.ICE, Blocks.STONE))
                    .addAction(BlockMappingAction(Blocks.FARMLAND, Blocks.SOUL_SAND))
                    .addAction(BlockMappingAction(Blocks.GRASS_PATH, Blocks.COBBLESTONE))
                    .addAction(BlockMappingAction(Blocks.WATER, Blocks.LAVA))
                    .addAction(BlockMappingAction({ world: World, pos: BlockPos, state: BlockState -> world.rand.nextDouble() < 0.05 }, Blocks.NETHERRACK))
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
                            val newEntity = ZombiePigmanEntity(EntityType.ZOMBIE_PIGMAN, entity.world)
                            newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                            newEntity.customName = entity.customName
                            newEntity.isCustomNameVisible = entity.isCustomNameVisible
                            entity.world.addEntity(newEntity)
                            entity.removed = true
                        }

                    }
        } else {
            val mapping = BlockActionManager(it.world, it.position.getAllInSphere(radius.toInt()))
                    .addAction(BlockMappingAction(Blocks.SOUL_SAND, Blocks.SAND))
                    .addAction(BlockMappingAction(Blocks.MAGMA_BLOCK, Blocks.CLAY))
                    .addAction(BlockMappingAction(Blocks.NETHER_BRICKS, Blocks.BRICKS))
                    .addAction(BlockMappingAction(Blocks.NETHERRACK, Blocks.DIRT))
                    .addAction(BlockMappingAction(Blocks.NETHER_WART, Blocks.DEAD_BUSH))
                    .addAction(BlockMappingAction(Blocks.NETHER_WART_BLOCK, Blocks.GRASS_BLOCK))
                    .addAction(BlockMappingAction(Blocks.LAVA, Blocks.WATER))
                    .addAction(BlockMappingAction({ world: World, pos: BlockPos, state: BlockState -> world.rand.nextDouble() < 0.05 }, Blocks.GRASS_BLOCK))
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

        if (it.world.rand.nextDouble() < 0.1) {
            val blockPlacingPos = BlockPos.Mutable(it.position)
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
        BlockActionManager(it.world, it.position.getAllInSphere(radius.toInt()))
                .addAction(BlockMappingAction(BlockTags.SAND, Tags.Blocks.GLASS))
                .addAction(BlockMappingAction(Tags.Blocks.GRAVEL, Tags.Blocks.STONE))
                .addAction(BlockMappingAction(Blocks.CLAY, Blocks.TERRACOTTA))
                .addAction(BlockMappingAction({ _, _, state -> state.block is SpreadableSnowyDirtBlock }, Blocks.DIRT))
                .addAction(BlockMappingAction(Blocks.GRASS_PATH, Blocks.COARSE_DIRT))
                .addAction(BlockMappingAction(Blocks.COBBLESTONE, Blocks.STONE))
                .addAction(BlockMappingAction({ world, pos, state -> state.isAir(world, pos) && world.rand.nextBoolean() }, Blocks.FIRE))
                .addAction(BlockRemovalAction(BlockTags.ICE))
                .addAction(BlockMappingAction(Tags.Blocks.STONE, listOf(Blocks.LAVA, Blocks.OBSIDIAN)))
                .addAction(BlockMappingAction(Blocks.DIRT, listOf(Blocks.MAGMA_BLOCK, Blocks.COARSE_DIRT)))
                .addAction(BlockRemovalAction { world, pos, _ -> FluidTags.WATER.contains(world.getFluidState(pos).fluid) })
                .start()

        it.world.addEntity(GlassingRayBeamEntity(EntityTypeHolder.glassingRayBeam, it.world, it.posX, it.posY, it.posZ))
    }
}