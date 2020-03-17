package com.williambl.explosivessquared.util

import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun getLengthOfChord(radius: Int, distanceFromCentre: Int): Int {
    val radD = radius.toDouble()
    val distD = distanceFromCentre.absoluteValue.toDouble() - 0.25
    val result = sqrt(radD * radD - distD * distD)
    return if (result.isNaN()) 0 else result.roundToInt()
}

fun BlockPos.getAllInSphere(radius: Int): Sequence<BlockPos> {
    val mPos = BlockPos.Mutable(this)
    return getAllInLine(radius).map { y -> getAllInCircle(getLengthOfChord(radius, y)).map { xz -> mPos.setPos(this.x + xz.first, this.y + y, this.z + xz.second) } }.flatten()
}

fun BlockPos.getAllInSphereSeq(radius: Int): Sequence<Sequence<Triple<Int, Int, Int>>> {
    return getAllInLine(radius).map { x -> getAllInCircle(getLengthOfChord(radius, x)).map { yz -> Triple(this.x + x, this.y + yz.first, this.z + yz.second) } }
}

fun getAllInCircle(radius: Int): Sequence<Pair<Int, Int>> {
    return getAllInLine(radius).map { x -> getAllInLine(getLengthOfChord(radius, x)).map { y -> Pair(x, y) } }.flatten()
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