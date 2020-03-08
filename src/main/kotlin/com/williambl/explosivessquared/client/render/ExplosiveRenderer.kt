package com.williambl.explosivessquared.client.render

import com.mojang.blaze3d.matrix.MatrixStack
import com.williambl.explosivessquared.ExplosivesSquared
import com.williambl.explosivessquared.entity.ExplosiveEntity
import com.williambl.explosivessquared.entity.MissileEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.block.Blocks
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.Vector3f
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraftforge.client.model.data.EmptyModelData

class ExplosiveRenderer(renderManager: EntityRendererManager) : EntityRenderer<ExplosiveEntity>(renderManager) {

    init {
        this.shadowSize = 0.5f
    }

    override fun render(entity: ExplosiveEntity, entityYaw: Float, partialTicks: Float, matrixStack: MatrixStack, buffer: IRenderTypeBuffer, packedLight: Int) {
        val blockstate =
                if (entity is MissileEntity)
                    ExplosivesSquared.explosiveMap[entity.type.registryName!!.path.dropLast(8)]?.missileBlock?.defaultState
                            ?: Blocks.AIR.defaultState
                else
                    ExplosivesSquared.explosiveMap[entity.type.registryName!!.path]?.block?.defaultState
                            ?: Blocks.AIR.defaultState

        if (blockstate.renderType == BlockRenderType.MODEL) {
            val world = entity.world
            if (blockstate !== world.getBlockState(BlockPos(entity)) && blockstate.renderType != BlockRenderType.INVISIBLE) {
                matrixStack.push()
                matrixStack.translate(0.0, 0.5, 0.0)
                if (entity.getFuse().toFloat() - partialTicks + 1.0f < 10.0f) {
                    var f = 1.0f - (entity.getFuse().toFloat() - partialTicks + 1.0f) / 10.0f
                    f = MathHelper.clamp(f, 0.0f, 1.0f)
                    f *= f * f
                    val f1 = 1.0f + f * 0.3f
                    matrixStack.scale(f1, f1, f1)
                }

                matrixStack.rotate(Vector3f.YP.rotation(entityYaw))
                matrixStack.rotate(Vector3f.XP.rotation(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch)))
                matrixStack.translate(-0.5, -0.5, -0.5)
                Minecraft.getInstance().blockRendererDispatcher.renderBlock(blockstate, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE)
                matrixStack.pop()
                super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight)
            }
        }
    }

    override fun getEntityTexture(entity: ExplosiveEntity): ResourceLocation {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE
    }
}