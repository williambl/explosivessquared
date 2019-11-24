package com.williambl.explosivessquared

import net.minecraft.block.BlockState
import net.minecraft.block.GrassBlock
import net.minecraft.block.IGrowable
import net.minecraft.block.VineBlock
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.IPlantable
import java.util.stream.Stream
import kotlin.math.pow

fun BlockState.isVegetation(): Boolean {
    if (this.block.tags.contains(ResourceLocation("minecraft:leaves")))
        return true
    if (this.block is IPlantable)
        return true
    if (this.block is IGrowable)
        return true
    if (this.block is VineBlock)
        return true
    return false
}

fun BlockState.isGrass(): Boolean {
    return this.block is GrassBlock
}

fun BlockPos.getAllInSphere(radius: Int): Stream<BlockPos> {
    return BlockPos.getAllInBox(this.subtract(BlockPos(radius, radius, radius)), this.add(BlockPos(radius, radius, radius)))
            .filter { pos -> pos.distanceSq(this) < radius.toFloat().pow(2) }
}

fun World.getEntitiesInSphere(pos: BlockPos, radius: Double, excluding: Entity? = null, predicate: (Entity) -> Boolean = { _ -> true }): MutableList<Entity> {
    return this.getEntitiesInAABBexcluding(
            excluding,
            AxisAlignedBB(pos.add(-0.5, -0.5, -0.5)).grow(radius)
    ) { it.getDistanceSq(Vec3d(pos)) < radius.pow(2) && predicate(it) }
}