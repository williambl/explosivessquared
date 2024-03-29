package com.williambl.explosivessquared.util.actions

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.tags.ITag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockMappingAction(val predicate: ((World, BlockPos, BlockState) -> Boolean)? = null, tagInput: ITag<Block>? = null, blocksInput: Collection<Block>? = null,
                         tagOutput: ITag<Block>? = null, blocksOutput: Collection<Block>? = null) : BlockAction {

    val inputs: List<Block> = tagInput?.allElements?.toList() ?: blocksInput?.toList() ?: listOf(Blocks.AIR)
    val outputs: List<Block> = tagOutput?.allElements?.toList() ?: blocksOutput?.toList() ?: listOf(Blocks.AIR)

    constructor(input: ITag<Block>, output: ITag<Block>) : this(tagInput = input, tagOutput = output)
    constructor(input: Collection<Block>, output: ITag<Block>) : this(blocksInput = input, tagOutput = output)
    constructor(input: Block, output: ITag<Block>) : this(blocksInput = listOf(input), tagOutput = output)
    constructor(input: ((World, BlockPos, BlockState) -> Boolean), output: ITag<Block>) : this(predicate = input, tagOutput = output)

    constructor(input: ITag<Block>, output: Collection<Block>) : this(tagInput = input, blocksOutput = output)
    constructor(input: Collection<Block>, output: Collection<Block>) : this(blocksInput = input, blocksOutput = output)
    constructor(input: Block, output: Collection<Block>) : this(blocksInput = listOf(input), blocksOutput = output)
    constructor(input: ((World, BlockPos, BlockState) -> Boolean), output: Collection<Block>) : this(predicate = input, blocksOutput = output)

    constructor(input: ITag<Block>, output: Block) : this(tagInput = input, blocksOutput = listOf(output))
    constructor(input: Collection<Block>, output: Block) : this(blocksInput = input, blocksOutput = listOf(output))
    constructor(input: Block, output: Block) : this(blocksInput = listOf(input), blocksOutput = listOf(output))
    constructor(input: ((World, BlockPos, BlockState) -> Boolean), output: Block) : this(predicate = input, blocksOutput = listOf(output))

    override fun matches(world: World, blockPos: BlockPos, blockState: BlockState): Boolean {
        return predicate?.invoke(world, blockPos, blockState) ?: inputs.contains(blockState.block)
    }

    override fun process(world: World, blockPos: BlockPos): BlockState {
        return outputs[world.rand.nextInt(outputs.size)].defaultState
    }
}