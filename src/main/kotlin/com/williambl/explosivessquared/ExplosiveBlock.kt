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
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.Explosion
import net.minecraft.world.World

class ExplosiveBlock(properties: Block.Properties, val explode: (World, BlockPos, LivingEntity?) -> Unit, val explodeFromExplosion: (World, BlockPos, Explosion?) -> Unit) : Block(properties) {

    init {
        this.defaultState = this.defaultState.with(BlockStateProperties.UNSTABLE, false)
    }

    override fun onBlockAdded(state: BlockState, worldIn: World?, pos: BlockPos?, oldState: BlockState, isMoving: Boolean) {
        if (oldState.block != state.block) {
            if (worldIn!!.isBlockPowered(pos!!)) {
                explode(worldIn, pos, null)
                worldIn.removeBlock(pos, false)
            }
        }
    }

    override fun neighborChanged(state: BlockState?, worldIn: World, pos: BlockPos?, blockIn: Block?, fromPos: BlockPos?, isMoving: Boolean) {
        if (worldIn.isBlockPowered(pos!!)) {
            explode(worldIn, pos, null)
            worldIn.removeBlock(pos, false)
        }

    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     */
    override fun onBlockHarvested(worldIn: World, pos: BlockPos, state: BlockState?, player: PlayerEntity) {
        if (!worldIn.isRemote() && !player.isCreative && state!!.get(BlockStateProperties.UNSTABLE)) {
            explode(worldIn, pos, null)
        }

        super.onBlockHarvested(worldIn, pos, state, player)
    }

    /**
     * Called when this Block is destroyed by an Explosion
     */
    override fun onExplosionDestroy(worldIn: World, pos: BlockPos?, explosionIn: Explosion?) {
        if (!worldIn.isRemote) {
            explodeFromExplosion(worldIn, pos!!, explosionIn)
        }
    }

    override fun onBlockActivated(state: BlockState?, worldIn: World?, pos: BlockPos?, player: PlayerEntity, handIn: Hand?, hit: BlockRayTraceResult?): Boolean {
        val itemstack = player.getHeldItem(handIn)
        val item = itemstack.item
        if (item !== Items.FLINT_AND_STEEL && item !== Items.FIRE_CHARGE) {
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit)
        } else {
            explode(worldIn!!, pos!!, player)
            worldIn.setBlockState(pos, Blocks.AIR.defaultState, 11)
            if (item === Items.FLINT_AND_STEEL) {
                itemstack.damageItem(1, player, { p_220287_1_ -> p_220287_1_.sendBreakAnimation(handIn) })
            } else {
                itemstack.shrink(1)
            }

            return true
        }
    }

    override fun onProjectileCollision(worldIn: World, state: BlockState?, hit: BlockRayTraceResult?, projectile: Entity?) {
        if (!worldIn.isRemote && projectile is AbstractArrowEntity) {
            val abstractarrowentity = projectile as AbstractArrowEntity?
            val entity = abstractarrowentity!!.shooter
            if (abstractarrowentity.isBurning) {
                val blockpos = hit!!.pos
                explode(worldIn, blockpos, if (entity is LivingEntity) entity else null)
                worldIn.removeBlock(blockpos, false)
            }
        }

    }

    /**
     * Return whether this block can drop from an explosion.
     */
    override fun canDropFromExplosion(explosionIn: Explosion?): Boolean {
        return false
    }

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(BlockStateProperties.UNSTABLE)
    }

}
