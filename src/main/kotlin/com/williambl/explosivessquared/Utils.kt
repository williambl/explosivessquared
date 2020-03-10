package com.williambl.explosivessquared

import net.minecraft.block.BlockState
import net.minecraft.block.GrassBlock
import net.minecraft.block.IGrowable
import net.minecraft.block.VineBlock
import net.minecraft.entity.Entity
import net.minecraft.tags.BlockTags
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.IPlantable
import kotlin.math.pow
import kotlin.math.sqrt

fun BlockState.isVegetation(): Boolean {
    if (BlockTags.LEAVES.contains(this.block))
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

fun BlockPos.getAllInSphere(radius: Int): Sequence<BlockPos> {
    return Sequence { BlockPos.getAllInBoxMutable(this.subtract(BlockPos(radius, radius, radius)), this.add(BlockPos(radius, radius, radius))).iterator() }
            .filter { pos -> pos.distanceSq(this) < radius.toFloat().pow(2) }
}

fun World.getEntitiesInSphere(pos: BlockPos, radius: Double, excluding: Entity? = null, predicate: (Entity) -> Boolean = { _ -> true }): MutableList<Entity> {
    return this.getEntitiesInAABBexcluding(
            excluding,
            AxisAlignedBB(pos.add(-0.5, -0.5, -0.5)).grow(radius)
    ) { it.getDistanceSq(Vec3d(pos)) < radius.pow(2) && predicate(it) }
}

fun BlockPos.getAllInColumn(radius: Int): Sequence<BlockPos> {
    return Sequence { BlockPos.getAllInBoxMutable(this.subtract(BlockPos(radius, 0, radius)), this.add(BlockPos(radius, 0, radius))).iterator() }
            .filter { pos -> pos.distanceSq(this) < radius.toFloat().pow(2) }
            .map { pos -> List(256) { idx -> BlockPos(pos.x, idx, pos.z) } }
            .flatten()
}

fun World.canExplosionDestroy(explosionRadius: Int, explosionCentre: BlockPos, pos: BlockPos, exploder: Entity): Boolean {
    var f: Double = explosionRadius * (0.7f + this.rand.nextFloat() * 0.6f) - 0.225 * sqrt(explosionCentre.distanceSq(pos))
    val blockstate = this.world.getBlockState(pos)
    val ifluidstate = this.world.getFluidState(pos)
    if (!blockstate.isAir(this.world, pos) || !ifluidstate.isEmpty) {
        val f2 = blockstate.getExplosionResistance(this.world, pos, exploder, null).coerceAtLeast(ifluidstate.getExplosionResistance(this.world, pos, exploder, null))
        f -= (f2 + 0.3f) * 0.3f
    }

    return f > 0.0f
}