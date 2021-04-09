package com.williambl.explosivessquared.item

import com.williambl.explosivessquared.block.tileentity.MissileTileEntity
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.World

class TargeterItem(properties: Properties) : Item(properties) {

    override fun onItemUse(context: ItemUseContext): ActionResultType {
        if (context.func_225518_g_()) { //isSneaking
            val tileEntity: TileEntity? = context.world.getTileEntity(context.pos)
            if (tileEntity != null && tileEntity is MissileTileEntity) {
                if (context.item.getOrCreateChildTag("ExplosivesSquared").contains("Target")) {
                    val target = context.item.getOrCreateChildTag("ExplosivesSquared").getCompound("Target")
                    tileEntity.target = Vec3d(target.getDouble("X"), target.getDouble("Y"), target.getDouble("Z"))
                    return ActionResultType.SUCCESS
                }
            }
            return ActionResultType.PASS
        }

        val tag = context.item.getOrCreateChildTag("ExplosivesSquared").getCompound("Target")
        tag.putDouble("X", context.hitVec.x)
        tag.putDouble("Y", context.hitVec.y)
        tag.putDouble("Z", context.hitVec.z)
        context.item.getOrCreateChildTag("ExplosivesSquared").put("Target", tag)

        return ActionResultType.SUCCESS
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<ITextComponent>, flagIn: ITooltipFlag) {
        if (stack.orCreateTag.contains("ExplosivesSquared")) {
            val tag = stack.getOrCreateChildTag("ExplosivesSquared").getCompound("Target")
            tooltip.add(StringTextComponent("Target: ${tag.getDouble("X")}, ${tag.getDouble("Y")}, ${tag.getDouble("Z")}"))
        }
        super.addInformation(stack, worldIn, tooltip, flagIn)
    }

}