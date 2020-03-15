package com.williambl.explosivessquared.util.actions

import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.util.concurrent.ThreadTaskExecutor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.LogicalSidedProvider

class BlockActionManager(val world: World, val positions: Sequence<BlockPos>) {

    private val actions: MutableList<BlockAction> = mutableListOf()

    public fun addAction(action: BlockAction): BlockActionManager {
        actions.add(action)
        return this
    }

    public fun start() = runBlocking {
        launch {
            val executor = LogicalSidedProvider.WORKQUEUE.get<ThreadTaskExecutor<in Runnable>>(LogicalSide.SERVER)
            val runnables = mutableListOf<Runnable>()
            positions.forEach { pos ->
                actions.forEach {
                    runnables.add(Runnable { if (it.matches(world, pos, world.getBlockState(pos))) it.process(world, pos) })
                    executor.runAsync {
                        runnables.forEach {
                            it.run()
                        }
                    }.await()
                    runnables.clear()
                }
            }
        }
    }

}