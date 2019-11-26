package com.williambl.explosivessquared

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.AbstractArrowEntity
import net.minecraft.item.Items
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.Explosion
import net.minecraft.world.World

open class ExplosiveBlock(val explosiveType: ExplosiveType, properties: Block.Properties) : Block(properties) {

    init {
        this.defaultState = this.defaultState.with(BlockStateProperties.UNSTABLE, false)
    }

    private fun explode(world: World, pos: BlockPos, entity: LivingEntity?) {
        if (!world.isRemote) {
            val explosiveEntity = ExplosiveEntity(explosiveType.entityType, world, (pos.x.toFloat() + 0.5f).toDouble(), pos.y.toDouble(), (pos.z.toFloat() + 0.5f).toDouble(), entity)
            explosiveEntity.setFuse(explosiveType.fuse)
            world.addEntity(explosiveEntity)
            world.playSound(null as PlayerEntity?, explosiveEntity.posX, explosiveEntity.posY, explosiveEntity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f)
        }
    }

    override fun onBlockAdded(state: BlockState, world: World?, pos: BlockPos?, oldState: BlockState, isMoving: Boolean) {
        if (oldState.block != state.block) {
            if (world!!.isBlockPowered(pos!!)) {
                explode(world, pos, null)
                world.removeBlock(pos, false)
            }
        }
    }

    override fun neighborChanged(state: BlockState?, world: World, pos: BlockPos?, block: Block?, fromPos: BlockPos?, isMoving: Boolean) {
        if (world.isBlockPowered(pos!!)) {
            explode(world, pos, null)
            world.removeBlock(pos, false)
        }

    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     */
    override fun onBlockHarvested(world: World, pos: BlockPos, state: BlockState?, player: PlayerEntity) {
        if (!world.isRemote() && !player.isCreative && state!!.get(BlockStateProperties.UNSTABLE)) {
            explode(world, pos, null)
        }

        super.onBlockHarvested(world, pos, state, player)
    }

    /**
     * Called when this Block is destroyed by an Explosion
     */
    override fun onExplosionDestroy(world: World, pos: BlockPos, explosion: Explosion?) {
        if (!world.isRemote) {
            val explosiveEntity = ExplosiveEntity(explosiveType.entityType, world, (pos.x.toFloat() + 0.5f).toDouble(), pos.y.toDouble(), (pos.z.toFloat() + 0.5f).toDouble(), null)
            explosiveEntity.setFuse(world.rand.nextInt(explosiveType.fuse / 4) + explosiveType.fuse / 8)
            world.addEntity(explosiveEntity)
        }
    }

    override fun onBlockActivated(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity, hand: Hand?, hit: BlockRayTraceResult?): Boolean {
        val itemstack = player.getHeldItem(hand)
        val item = itemstack.item
        if (item !== Items.FLINT_AND_STEEL && item !== Items.FIRE_CHARGE) {
            return super.onBlockActivated(state, world, pos, player, hand, hit)
        } else {
            explode(world!!, pos!!, player)
            world.setBlockState(pos, Blocks.AIR.defaultState, 11)
            if (item === Items.FLINT_AND_STEEL) {
                itemstack.damageItem(1, player, { p_220287_1_ -> p_220287_1_.sendBreakAnimation(hand) })
            } else {
                itemstack.shrink(1)
            }

            return true
        }
    }

    override fun onProjectileCollision(world: World, state: BlockState?, hit: BlockRayTraceResult?, projectile: Entity?) {
        if (!world.isRemote && projectile is AbstractArrowEntity) {
            val abstractarrowentity = projectile as AbstractArrowEntity?
            val entity = abstractarrowentity!!.shooter
            if (abstractarrowentity.isBurning) {
                val blockpos = hit!!.pos
                explode(world, blockpos, if (entity is LivingEntity) entity else null)
                world.removeBlock(blockpos, false)
            }
        }

    }

    /**
     * Return whether this block can drop from an explosion.
     */
    override fun canDropFromExplosion(explosion: Explosion?): Boolean {
        return false
    }

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(BlockStateProperties.UNSTABLE)
    }

}
