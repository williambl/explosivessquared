package com.williambl.explosivessquared.client.render

import com.mojang.blaze3d.matrix.MatrixStack
import com.williambl.explosivessquared.entity.AntigravityBlockEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos

class AntiGravityBlockRenderer(entityRendererManager: EntityRendererManager?) :
    EntityRenderer<AntigravityBlockEntity>(entityRendererManager) {
    override fun render(
        entityIn: AntigravityBlockEntity,
        entityYaw: Float,
        partialTicks: Float,
        matrixStackIn: MatrixStack,
        bufferIn: IRenderTypeBuffer,
        packedLightIn: Int
    ) {
        val blockState = entityIn.blockState
        if (blockState.renderType == BlockRenderType.MODEL) {
            val world = entityIn.world
            if (blockState !== world.getBlockState(entityIn.position) && blockState.renderType != BlockRenderType.INVISIBLE) {
                matrixStackIn.push()
                val blockPos = BlockPos(entityIn.posX, entityIn.boundingBox.maxY, entityIn.posZ)
                matrixStackIn.translate(-0.5, 0.0, -0.5)
                val blockRendererDispatcher = Minecraft.getInstance().blockRendererDispatcher
                blockRendererDispatcher.blockModelRenderer.renderModel(
                    world,
                    blockRendererDispatcher.getModelForState(blockState),
                    blockState,
                    blockPos,
                    matrixStackIn,
                    bufferIn.getBuffer(
                        RenderTypeLookup.func_239221_b_(blockState)
                    ),
                    false,
                    world.random,
                    blockState.getPositionRandom(entityIn.origin),
                    OverlayTexture.NO_OVERLAY
                )
                matrixStackIn.pop()
                super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn)
            }
        }
    }

    /**
     * Returns the location of an entity's texture.
     */
    override fun getEntityTexture(entity: AntigravityBlockEntity): ResourceLocation {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE
    }

    init {
        shadowSize = 0.5f
    }
}