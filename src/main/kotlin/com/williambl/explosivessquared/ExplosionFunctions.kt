package com.williambl.explosivessquared

import com.williambl.explosivessquared.entity.ExplosiveEntity
import com.williambl.explosivessquared.entity.GlassingRayBeamEntity
import com.williambl.explosivessquared.objectholders.EntityTypeHolder
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
import net.minecraft.particles.ParticleTypes
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import net.minecraftforge.common.Tags
import kotlin.math.roundToInt

typealias ExplosionFunction = (ExplosiveEntity) -> Unit

fun regularExplosion(radius: Float): ExplosionFunction {
    return { it.world.createExplosion(it, it.posX, it.posY, it.posZ, radius, Explosion.Mode.DESTROY) }
}

fun vegetationDestroyerExplosion(radius: Double): ExplosionFunction {
    return {
        val mappings = BlockMappings(it.world.rand)
                .addMapping(BlockState::isGrass, Blocks.DIRT)
                .addMapping({ state -> !state.isGrass() }, Blocks.AIR)

        it.position.getAllInSphere(radius.roundToInt())
                .filter { pos -> it.world.getBlockState(pos).isVegetation() }
                .forEach { pos ->
                    it.world.setBlockState(pos, mappings.process(it.world.getBlockState(pos)))
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
                    val blockState = it.world.getBlockState(pos)
                    if (it.world.canExplosionDestroy(radius.toInt(), it.position, pos, it)) {
                        val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, blockState)
                        fallingEntity.setHurtEntities(true)
                        fallingEntity.isInvulnerable = true
                        val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                        val velocityVector = fallingEntity.positionVec.subtract(it.positionVec).normalize().mul(speed, speed, speed)
                        fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                        it.world.addEntity(fallingEntity)
                    }
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
                    val blockState = it.world.getBlockState(pos)
                    if (it.world.canExplosionDestroy(radius.toInt(), it.position, pos, it)) {
                        val fallingEntity = FallingBlockEntity(it.world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, blockState)
                        fallingEntity.setHurtEntities(true)
                        fallingEntity.isInvulnerable = true
                        val speed = radius / (fallingEntity.positionVec.distanceTo(it.positionVec))
                        val velocityVector = it.positionVec.subtract(fallingEntity.positionVec).normalize().mul(speed, speed, speed)
                        fallingEntity.setVelocity(velocityVector.x, velocityVector.y, velocityVector.z)
                        it.world.addEntity(fallingEntity)
                    }
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
        val mapping = BlockMappings(it.world.rand)
            .addMapping(Blocks.ICE, Blocks.PACKED_ICE)
            .addMapping(Blocks.FIRE, Blocks.AIR)
            .addMapping(Blocks.MAGMA_BLOCK, Blocks.NETHERRACK)
        it.position.getAllInSphere(radius.roundToInt())
                .filter { pos -> !it.world.getBlockState(pos).isAir(it.world, pos) }
                .forEach { pos ->
                    it.world.setBlockState(pos, mapping.process(it.world.getBlockState(pos)))

                    if (FluidTags.WATER.contains(it.world.getFluidState(pos).fluid))
                        it.world.setBlockState(pos, Blocks.ICE.defaultState)
                    if (FluidTags.LAVA.contains(it.world.getFluidState(pos).fluid))
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
            val mapping = BlockMappings(it.world.rand)
                    .addMapping(Tags.Blocks.SAND, Blocks.SOUL_SAND)
                    .addMapping(Blocks.CLAY, Blocks.MAGMA_BLOCK)
                    .addMapping(Blocks.BRICKS, Blocks.NETHER_BRICKS)
                    .addMapping(Blocks.DIRT, Blocks.NETHERRACK)
                    .addMapping({ state -> state.block is IGrowable }, Blocks.NETHER_WART)
                    .addMapping(BlockTags.LOGS, Blocks.COBBLESTONE)
                    .addMapping(BlockTags.LEAVES, Blocks.COBBLESTONE)
                    .addMapping(BlockTags.ICE, Blocks.STONE)
            it.position.getAllInSphere(radius.roundToInt())
                    .filter { pos -> !it.world.getBlockState(pos).isAir(it.world, pos) }
                    .forEach { pos ->
                        val block = it.world.getBlockState(pos).block
                        it.world.setBlockState(pos, mapping.process(it.world.getBlockState(pos)))

                        if (FluidTags.WATER.contains(it.world.getFluidState(pos).fluid))
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
            val mapping = BlockMappings(it.world.rand)
                    .addMapping(Blocks.SOUL_SAND, Blocks.SAND)
                    .addMapping(Blocks.MAGMA_BLOCK, Blocks.CLAY)
                    .addMapping(Blocks.NETHER_BRICKS, Blocks.BRICKS)
                    .addMapping(Blocks.NETHERRACK, Blocks.DIRT)
            it.position.getAllInSphere(radius.roundToInt())
                    .filter { pos -> !it.world.getBlockState(pos).isAir(it.world, pos) }
                    .forEach { pos ->
                        it.world.setBlockState(pos, mapping.process(it.world.getBlockState(pos)))

                        if (FluidTags.LAVA.contains(it.world.getFluidState(pos).fluid))
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

fun glassingRay(radius: Double): ExplosionFunction {
    return {
        val mapping = BlockMappings(it.world.rand)
                .addMapping(BlockTags.SAND, Tags.Blocks.GLASS)
                .addMapping(Tags.Blocks.GRAVEL, Tags.Blocks.STONE)
                .addMapping(Blocks.CLAY, Blocks.TERRACOTTA)
                .addMapping({ state -> state.block is SpreadableSnowyDirtBlock }, Blocks.DIRT)
                .addMapping(Blocks.GRASS_PATH, Blocks.COARSE_DIRT)
                .addMapping(Blocks.COBBLESTONE, Blocks.STONE)

        it.position.getAllInSphere(radius.toInt())
                .forEach { pos ->
                    val blockstate = it.world.getBlockState(pos)
                    val block = blockstate.block

                    if (blockstate.isAir(it.world, pos)) {
                        if (it.world.rand.nextBoolean())
                            it.world.setBlockState(pos, Blocks.FIRE.defaultState)
                        return@forEach
                    }

                    if (BlockTags.ICE.contains(block)) {
                        it.world.removeBlock(pos, false)
                        for (i in 0..20)
                            it.world.addParticle(ParticleTypes.EXPLOSION, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 0.0, 0.0, 0.0)
                    } else if (Tags.Blocks.STONE.contains(block)) {
                        if (it.world.rand.nextBoolean())
                            it.world.setBlockState(pos, Blocks.LAVA.defaultState)
                        else
                            it.world.setBlockState(pos, Blocks.OBSIDIAN.defaultState)
                    } else if (block == Blocks.DIRT) {
                        if (it.world.rand.nextBoolean())
                            it.world.setBlockState(pos, Blocks.MAGMA_BLOCK.defaultState)
                        else
                            it.world.setBlockState(pos, Blocks.COARSE_DIRT.defaultState)
                    }

                    it.world.setBlockState(pos, mapping.process(blockstate))

                    if (FluidTags.WATER.contains(it.world.getFluidState(pos).fluid)) {
                        it.world.setBlockState(pos, Blocks.AIR.defaultState)
                        for (i in 0..20)
                            it.world.addParticle(ParticleTypes.EXPLOSION, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 0.0, 0.0, 0.0)
                    }
                }
        it.world.addEntity(GlassingRayBeamEntity(EntityTypeHolder.glassingRayBeam, it.world, it.posX, it.posY, it.posZ))
    }
}