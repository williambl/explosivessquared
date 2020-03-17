package com.williambl.explosivessquared.util.actions

import com.williambl.explosivessquared.ExplosivesSquared
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.minecraft.block.BlockState
import net.minecraft.util.concurrent.ThreadTaskExecutor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.LogicalSidedProvider

class BlockActionManager(val world: World, val positions: Sequence<BlockPos>) {

    private val actions: MutableList<BlockAction> = mutableListOf()
    private val filters: MutableList<(World, BlockPos, BlockState) -> Boolean> = mutableListOf()

    public fun addAction(action: BlockAction): BlockActionManager {
        actions.add(action)
        return this
    }

    public fun addFilter(filter: (World, BlockPos, BlockState) -> Boolean): BlockActionManager {
        filters.add(filter)
        return this
    }

    public fun start() {
        GlobalScope.launch(ExplosivesSquared.threadPool) {
            val executor = LogicalSidedProvider.WORKQUEUE.get<ThreadTaskExecutor<in Runnable>>(LogicalSide.SERVER)
            positions.map { pos ->
                async {
                    val imPos = pos.toImmutable()
                    executor.execute {
                    actions.forEach {
                        val bs = world.getBlockState(imPos)
                        if (filters.all { it(world, imPos, bs) } && it.matches(world, imPos, bs))
                            it.process(world, imPos)
                    }
                    }
                }
            }.forEach { it.await() }
        }
    }

}