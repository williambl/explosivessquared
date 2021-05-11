package com.williambl.explosivessquared.util.actions

import com.williambl.explosivessquared.util.BlockPosSeq3D
import net.minecraft.block.Blocks
import net.minecraft.network.play.server.SChunkDataPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.SectionPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkSection
import net.minecraft.world.server.ServerWorld

class MassBlockActionManager(world: World, positions: BlockPosSeq3D): BlockActionManager(world, positions) {

    private val chunkJobs: MutableMap<Long, MutableList<Pair<Long, (ChunkSection?, Chunk, World, BlockPos, Int, Int, Int) -> Unit>>> = mutableMapOf()

    private fun addChunkJob(x: Int, y: Int, z: Int, job: (ChunkSection?, Chunk, World, BlockPos, Int, Int, Int) -> Unit) {
        chunkJobs.computeIfAbsent(((x shr 4).toLong() shl 32) or (z shr 4).toLong()) { mutableListOf() }.add(
            Pair(BlockPos.pack(x, y, z), job)
        )
    }

    public override fun start() {
        positions.second.forEach { xseq ->
            val x = xseq.first
            xseq.second.forEach { zseq ->
                val z = zseq.first
                val seq = zseq.second
                seq.filter { y -> positions.first.y + y >= 0 && positions.first.y + y <= world.height }.forEach { y ->
                    addChunkJob(positions.first.x + x, positions.first.y + y, positions.first.z + z) { section, chunk, world, pos, sectionX, sectionY, sectionZ ->
                        actions.forEach {
                            val empty = ChunkSection.isEmpty(section)
                            val bs = if (empty) Blocks.AIR.defaultState else section!!.getBlockState(sectionX, sectionY, sectionZ)
                            if (filters.all { it(world, pos, bs) } && it.matches(world, pos, bs)) {
                                val result = it.process(world, pos)
                                if (empty) {
                                    val newSection = ChunkSection(pos.y shr 4 shl 4)
                                    chunk.sections[pos.y shr 4] = newSection
                                    newSection.setBlockState(sectionX, sectionY, sectionZ, result)
                                } else {
                                    section!!.setBlockState(sectionX, sectionY, sectionZ, result)
                                }
                            }
                        }
                    }
                }
            }
        }

        chunkJobs.forEach { chunkJobCollection ->
            val chunk =
                world.getChunk((chunkJobCollection.key shr 32).toInt(), chunkJobCollection.key.toInt())
            val sections = chunk.sections
            val mutablePos = BlockPos.Mutable()

            chunkJobCollection.value.forEach { chunkJobData ->
                mutablePos.setPos(
                    BlockPos.unpackX(chunkJobData.first),
                    BlockPos.unpackY(chunkJobData.first),
                    BlockPos.unpackZ(chunkJobData.first)
                )
                chunkJobData.second(
                    sections[mutablePos.y shr 4],
                    chunk,
                    world,
                    mutablePos,
                    mutablePos.x and 15,
                    mutablePos.y and 15,
                    mutablePos.z and 15
                )
            }

            chunk.markDirty()
            sections.filterNotNull().forEach { section ->
                chunk.worldLightManager?.updateSectionStatus(
                    SectionPos.from(
                        chunk.pos,
                        section.yLocation shr 4
                    ), ChunkSection.isEmpty(section)
                )
            }
            if (world is ServerWorld) {
                world.chunkProvider.chunkManager.getTrackingPlayers(chunk.pos, false).forEach { player ->
                    player.connection.sendPacket(SChunkDataPacket(chunk, 65535))
                }
            }
        }
    }
}