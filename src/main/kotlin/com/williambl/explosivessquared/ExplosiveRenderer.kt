package com.williambl.explosivessquared

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.block.BlockRenderType
import net.minecraft.block.Blocks
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import java.util.*

class ExplosiveRenderer(renderManagerIn: EntityRendererManager) : EntityRenderer<ExplosiveEntity>(renderManagerIn) {

    init {
        this.shadowSize = 0.5f
    }

    override fun doRender(entity: ExplosiveEntity, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        val blockstate = ExplosivesSquared.entityTypesToBlocks[entity.type]?.defaultState ?: Blocks.AIR.defaultState
        if (blockstate.renderType == BlockRenderType.MODEL) {
            val world = entity.world
            if (blockstate !== world.getBlockState(BlockPos(entity)) && blockstate.renderType != BlockRenderType.INVISIBLE) {
                this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                GlStateManager.pushMatrix()
                GlStateManager.disableLighting()
                val tessellator = Tessellator.getInstance()
                val bufferbuilder = tessellator.buffer
                if (this.renderOutlines) {
                    GlStateManager.enableColorMaterial()
                    GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity))
                }

                bufferbuilder.begin(7, DefaultVertexFormats.BLOCK)
                val blockpos = BlockPos(entity.posX, entity.boundingBox.maxY, entity.posZ)
                GlStateManager.translatef((x - blockpos.x.toDouble() - 0.5).toFloat(), (y - blockpos.y.toDouble()).toFloat(), (z - blockpos.z.toDouble() - 0.5).toFloat())
                val blockrendererdispatcher = Minecraft.getInstance().blockRendererDispatcher
                blockrendererdispatcher.blockModelRenderer.renderModel(world, blockrendererdispatcher.getModelForState(blockstate), blockstate, blockpos, bufferbuilder, false, Random(), blockstate.getPositionRandom(entity.position))
                tessellator.draw()
                if (this.renderOutlines) {
                    GlStateManager.tearDownSolidRenderingTextureCombine()
                    GlStateManager.disableColorMaterial()
                }

                GlStateManager.enableLighting()
                GlStateManager.popMatrix()
                super.doRender(entity, x, y, z, entityYaw, partialTicks)
            }
        }
    }

    override fun getEntityTexture(entity: ExplosiveEntity): ResourceLocation {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE
    }
}