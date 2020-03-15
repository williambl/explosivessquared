package com.williambl.explosivessquared.util.actions

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.tags.Tag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockFunctionAction(val predicate: ((World, BlockPos, BlockState) -> Boolean)? = null, tagInput: Tag<Block>? = null, blockCollectionInput: Collection<Block>? = null,
                          val function: (World, BlockPos) -> Unit) : BlockAction {

    val inputs: List<Block> = tagInput?.allElements?.toList() ?: blockCollectionInput?.toList() ?: listOf(Blocks.AIR)

    override fun matches(world: World, blockPos: BlockPos, blockState: BlockState): Boolean {
        return predicate?.invoke(world, blockPos, blockState) ?: inputs.contains(blockState.block)
    }

    override fun process(world: World, blockPos: BlockPos): Runnable {
        return Runnable { function(world, blockPos) }
    }
}