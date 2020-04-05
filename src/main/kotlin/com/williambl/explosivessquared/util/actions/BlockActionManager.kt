package com.williambl.explosivessquared.util.actions

import com.williambl.explosivessquared.ExplosivesSquared
import com.williambl.explosivessquared.util.BlockPosSeq3D
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

class BlockActionManager(val world: World, val positions: BlockPosSeq3D) {

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
            positions.second.map { xseq ->
                async {
                    val x = xseq.first
                    xseq.second.forEach { zseq ->
                        val z = zseq.first
                        val seq = zseq.second

                        val pos = BlockPos.Mutable(x, 0, z)

                        executor.deferTask {
                            seq.forEach { y ->
                                pos.setPos(positions.first.x + x, positions.first.y + y, positions.first.z + z)
                                actions.forEach {
                                    val bs = world.getBlockState(pos)
                                    if (filters.all { it(world, pos, bs) } && it.matches(world, pos, bs))
                                        it.process(world, pos)
                                }
                            }
                        }.await()
                    }
                }
            }.toList().awaitAll()
        }
    }

}