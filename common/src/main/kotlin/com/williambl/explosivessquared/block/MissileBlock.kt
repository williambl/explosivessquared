package com.williambl.explosivessquared.block

import com.williambl.explosivessquared.ExplosiveType
import com.williambl.explosivessquared.block.tileentity.MissileTileEntity
import com.williambl.explosivessquared.entity.MissileEntity
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.AbstractArrowEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.Items
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

open class MissileBlock(val explosiveType: ExplosiveType, properties: Properties) : Block(properties), ITileEntityProvider {

    override fun createNewTileEntity(worldIn: IBlockReader?): TileEntity? {
        return MissileTileEntity()
    }

    override fun getShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
        return makeCuboidShape(4.0, 0.0, 4.0, 12.0, 14.0, 12.0)
    }

    private fun explode(world: World, pos: BlockPos, entity: LivingEntity?) {
        if (!world.isRemote) {
            val missileEntity = MissileEntity(explosiveType.missileEntityType, world, (pos.x.toFloat() + 0.5f).toDouble(), pos.y.toDouble(), (pos.z.toFloat() + 0.5f).toDouble(), entity, (world.getTileEntity(pos) as MissileTileEntity).target)
            world.addEntity(missileEntity)
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

    override fun onBlockActivated(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity, hand: Hand?, hit: BlockRayTraceResult?): ActionResultType {
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

            return ActionResultType.SUCCESS
        }
    }

    override fun onProjectileCollision(world: World, state: BlockState?, hit: BlockRayTraceResult?, projectile: ProjectileEntity?) {
        if (!world.isRemote && projectile is AbstractArrowEntity) {
            val abstractarrowentity = projectile as AbstractArrowEntity?
            val entity = abstractarrowentity!!.func_234616_v_()
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

}
