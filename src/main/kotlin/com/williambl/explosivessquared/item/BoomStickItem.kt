package com.williambl.explosivessquared.item

import com.williambl.explosivessquared.ExplosiveType
import com.williambl.explosivessquared.entity.ExplosiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.*
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World


class BoomStickItem(val explosiveType: ExplosiveType, properties: Item.Properties) : Item(properties) {

    override fun onItemRightClick(world: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {
        val result = rayTrace(playerIn, 64.0, world)
        if (result.type == RayTraceResult.Type.BLOCK) {
            val explosiveEntity = ExplosiveEntity(explosiveType.entityType, world, result.hitVec.x, result.hitVec.y, result.hitVec.z, playerIn)
            explosiveEntity.setFuse(0)
            world.addEntity(explosiveEntity)
            world.playSound(null as PlayerEntity?, explosiveEntity.posX, explosiveEntity.posY, explosiveEntity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f)
            return ActionResult(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn))
        }
        return ActionResult(ActionResultType.PASS, playerIn.getHeldItem(handIn))
    }

    fun rayTrace(playerIn: PlayerEntity, blockReachDistance: Double, world: World): BlockRayTraceResult {
        val eyeVector = Vec3d(playerIn.posX, playerIn.posY + playerIn.eyeHeight.toDouble(), playerIn.posZ)
        val lookVector = playerIn.getLook(1f)
        val endVector = eyeVector.add(lookVector.x * blockReachDistance, lookVector.y * blockReachDistance, lookVector.z * blockReachDistance)
        return world.rayTraceBlocks(RayTraceContext(eyeVector, endVector, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, playerIn))
    }

}
