package com.williambl.explosivessquared.util.actions

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface BlockAction {
    fun matches(world: World, blockPos: BlockPos, blockState: BlockState): Boolean
    fun process(world: World, blockPos: BlockPos)
}