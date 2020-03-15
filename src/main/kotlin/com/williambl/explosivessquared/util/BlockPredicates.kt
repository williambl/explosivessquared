package com.williambl.explosivessquared.util

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

typealias BlockPredicate = (World, BlockPos, BlockState) -> Boolean

val isAir: BlockPredicate = { world, pos, state ->
    state.isAir(world, pos)
}

val isNotAir: BlockPredicate = { world, pos, state ->
    !state.isAir(world, pos)
}

val isNotUnbreakable: BlockPredicate = { world, pos, state ->
    state.getBlockHardness(world, pos) >= 0
}

val fiftyFifty: BlockPredicate = { world, _, _ ->
    world.rand.nextBoolean()
}

fun random(chance: Double): BlockPredicate {
    return { world, _, _ -> world.rand.nextDouble() < chance }
}

inline fun <reified T> isOfType(): BlockPredicate {
    return { _, _, state -> state.block is T }
}

fun combine(vararg predicates: BlockPredicate): BlockPredicate {
    return { world, pos, state -> predicates.all { it(world, pos, state) } }
}