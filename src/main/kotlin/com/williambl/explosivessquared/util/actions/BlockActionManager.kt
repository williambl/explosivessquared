package com.williambl.explosivessquared.util.actions

import com.williambl.explosivessquared.ExplosivesSquared
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.minecraft.block.BlockState
import net.minecraft.util.concurrent.ThreadTaskExecutor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.LogicalSidedProvider

class BlockActionManager(val world: World, val positions: Sequence<Sequence<Triple<Int, Int, Int>>>) {

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
            positions.map { seq ->
                async {
                    val pos = BlockPos.Mutable(0, 0, 0)
                    seq.forEach { triple ->
                        if (triple.second > 256 || triple.second < 0)
                            return@forEach
                        pos.setPos(triple.first, triple.second, triple.third)

                        executor.deferTask {
                            actions.forEach {
                                val bs = world.getBlockState(pos)
                                if (filters.all { it(world, pos, bs) } && it.matches(world, pos, bs))
                                    it.process(world, pos)
                            }
                        }.await()
                    }
                }
            }.toList().awaitAll()
        }
    }

}