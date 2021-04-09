package com.williambl.explosivessquared.util.actions

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.tags.Tag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockFunctionAction(val predicate: ((World, BlockPos, BlockState) -> Boolean)? = null, tagInput: Tag<Block>? = null, blocksInput: Collection<Block>? = null,
                          val function: (World, BlockPos) -> BlockState) : BlockAction {

    val inputs: List<Block> = tagInput?.allElements?.toList() ?: blocksInput?.toList() ?: listOf(Blocks.AIR)

    constructor(input: Tag<Block>, function: (World, BlockPos) -> BlockState) : this(tagInput = input, function = function)
    constructor(input: Collection<Block>, function: (World, BlockPos) -> BlockState) : this(blocksInput = input, function = function)
    constructor(input: Block, function: (World, BlockPos) -> BlockState) : this(blocksInput = listOf(input), function = function)
    constructor(input: ((World, BlockPos, BlockState) -> Boolean), function: (World, BlockPos) -> BlockState) : this(predicate = input, function = function)

    override fun matches(world: World, blockPos: BlockPos, blockState: BlockState): Boolean {
        return predicate?.invoke(world, blockPos, blockState) ?: inputs.contains(blockState.block)
    }

    override fun process(world: World, blockPos: BlockPos): BlockState {
        return function(world, blockPos)
    }
}