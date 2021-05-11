package com.williambl.explosivessquared.util

import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

typealias BlockPosSeq3D = Pair<BlockPos, Sequence<Pair<Int, Sequence<Pair<Int, Sequence<Int>>>>>>
typealias BlockPosSeq2D = Sequence<Pair<Int, Sequence<Int>>>

fun getLengthOfChord(radius: Int, distanceFromCentre: Int): Int {
    val radD = radius.toDouble()
    val distD = distanceFromCentre.absoluteValue.toDouble() - 0.25
    val result = sqrt(radD * radD - distD * distD)
    return if (result.isNaN()) 0 else result.roundToInt()
}

fun BlockPos.getAllInSphere(radius: Int): Sequence<BlockPos> {
    val mPos = BlockPos.Mutable().setPos(this)
    return getAllInLine(radius).map { y -> getAllInCircle(getLengthOfChord(radius, y)).map { xz -> mPos.setPos(this.x + xz.first, this.y + y, this.z + xz.second) } }.flatten()
}

fun BlockPos.getAllInSphereSeq(radius: Int): BlockPosSeq3D {
    return Pair(this, getAllInLine(radius).map { x -> Pair(x, getAllInCircleSeq(getLengthOfChord(radius, x))) })
}

fun getAllInCircle(radius: Int): Sequence<Pair<Int, Int>> {
    return getAllInLine(radius).map { x -> getAllInLine(getLengthOfChord(radius, x)).map { y -> Pair(x, y) } }.flatten()
}

fun getAllInCircleSeq(radius: Int): BlockPosSeq2D {
    return getAllInLine(radius).map { x -> Pair(x, getAllInLine(getLengthOfChord(radius, x))) }
}

fun getAllInLine(radius: Int): Sequence<Int> {
    return object : Iterator<Int> {
        var x = 0

        override fun next(): Int {
            val result = x
            x = if (x > 0) -x else 1 - x
            return result
        }

        override fun hasNext(): Boolean {
            return x != radius + 1
        }
    }.asSequence()
}

fun World.getEntitiesInSphere(pos: BlockPos, radius: Double, excluding: Entity? = null, predicate: (Entity) -> Boolean = { _ -> true }): MutableList<Entity> {
    return this.getEntitiesInAABBexcluding(
            excluding,
            AxisAlignedBB(pos.add(-0.5, -0.5, -0.5)).grow(radius)
    ) { it.getDistanceSq(Vector3d.copyCentered(pos)) < radius.pow(2) && predicate(it) }
}

fun BlockPos.getAllInColumn(radius: Int): Sequence<BlockPos> {
    return Sequence { BlockPos.getAllInBoxMutable(this.subtract(BlockPos(radius, 0, radius)), this.add(BlockPos(radius, 0, radius))).iterator() }
            .filter { pos -> pos.distanceSq(this) < radius.toFloat().pow(2) }
            .map { pos -> List(256) { idx -> BlockPos(pos.x, idx, pos.z) } }
            .flatten()
}

fun World.canExplosionDestroy(explosionRadius: Int, explosionCentre: BlockPos, pos: BlockPos, exploder: Entity): Boolean {
    var f: Double = explosionRadius * (0.7f + this.rand.nextFloat() * 0.6f) - 0.225 * sqrt(explosionCentre.distanceSq(pos))
    val blockstate = this.getBlockState(pos)
    val ifluidstate = this.getFluidState(pos)
    if (!blockstate.isAir || !ifluidstate.isEmpty) {
        val f2 = blockstate.block.explosionResistance.coerceAtLeast(ifluidstate.explosionResistance)
        f -= (f2 + 0.3f) * 0.3f
    }

    return f > 0.0f
}