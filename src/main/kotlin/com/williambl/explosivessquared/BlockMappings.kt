package com.williambl.explosivessquared

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.tags.Tag
import java.util.*

class BlockMappings(val random: Random) {

    private val mappings: MutableList<Mapping> = mutableListOf()

    public fun addMapping(input: Block, output: Block): BlockMappings {
        mappings.add(Mapping(blockInput = input, blockOutput = output))
        return this
    }

    public fun addMapping(input: Tag<Block>, output: Block): BlockMappings {
        mappings.add(Mapping(tagInput = input, blockOutput = output))
        return this
    }

    public fun addMapping(input: Block, output: Tag<Block>): BlockMappings {
        mappings.add(Mapping(blockInput = input, tagOutput = output))
        return this
    }

    public fun addMapping(input: Tag<Block>, output: Tag<Block>): BlockMappings {
        mappings.add(Mapping(tagInput = input, tagOutput = output))
        return this
    }

    public fun addMapping(predicate: (BlockState) -> Boolean, output: Block): BlockMappings {
        mappings.add(Mapping(predicate = predicate, blockOutput = output))
        return this
    }

    public fun addMapping(predicate: (BlockState) -> Boolean, output: Tag<Block>): BlockMappings {
        mappings.add(Mapping(predicate = predicate, tagOutput = output))
        return this
    }

    public fun process(input: BlockState): BlockState {
        return mappings.firstOrNull { it.matches(input) }?.process(random) ?: input
    }

    private class Mapping(val predicate: ((BlockState) -> Boolean)? = null, tagInput: Tag<Block>? = null, blockInput: Block? = null,
                          tagOutput: Tag<Block>? = null, blockOutput: Block? = null) {

        val inputs: List<Block> = tagInput?.allElements?.toList() ?: listOf(blockInput ?: Blocks.AIR)
        val outputs: List<Block> = tagOutput?.allElements?.toList() ?: listOf(blockOutput ?: Blocks.AIR)

        fun matches(input: BlockState): Boolean {
            return predicate?.invoke(input) ?: inputs.contains(input.block)
        }

        fun process(random: Random): BlockState {
            return outputs[random.nextInt(outputs.size)].defaultState
        }
    }

}