package com.williambl.explosivessquared.util.actions

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.tags.Tag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockRemovalAction(val predicate: ((World, BlockPos, BlockState) -> Boolean)? = null, tagInput: Tag<Block>? = null, blocksInput: Collection<Block>? = null)
    : BlockAction {

    val inputs: List<Block> = tagInput?.allElements?.toList() ?: blocksInput?.toList() ?: listOf(Blocks.AIR)

    constructor(input: Tag<Block>) : this(tagInput = input)
    constructor(input: Collection<Block>) : this(blocksInput = input)
    constructor(input: Block) : this(blocksInput = listOf(input))
    constructor(input: ((World, BlockPos, BlockState) -> Boolean)) : this(predicate = input)

    override fun matches(world: World, blockPos: BlockPos, blockState: BlockState): Boolean {
        return predicate?.invoke(world, blockPos, blockState) ?: inputs.contains(blockState.block)
    }

    override fun process(world: World, blockPos: BlockPos) {
        world.setBlockState(blockPos, Blocks.AIR.defaultState, 2)
    }
}