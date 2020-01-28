package com.williambl.explosivessquared

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper

class GlassingRayBeamRenderer(rendererManager: EntityRendererManager): EntityRenderer<GlassingRayBeamEntity>(rendererManager) {
    private val TEXTURE_BEACON_BEAM = ResourceLocation("textures/entity/beacon_beam.png")
    private val COLOR = floatArrayOf(1.0f, 1.0f, 1.0f)

    override fun doRender(entity: GlassingRayBeamEntity, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        bindEntityTexture(entity)
        renderBeam(x, y, z, partialTicks.toDouble(), entity.world.gameTime, entity.getTimeLeft())
    }

    override fun getEntityTexture(entity: GlassingRayBeamEntity): ResourceLocation? {
        return TEXTURE_BEACON_BEAM
    }

    private fun renderBeam(x: Double, y: Double, z: Double, partialTicks: Double, time: Long, timeLeft: Int) {
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.disableFog()
        GlStateManager.disableLighting()
        renderBeamSegement(x, y, z, partialTicks, time, 0, 1024, COLOR, timeLeft)
        GlStateManager.enableLighting()
        GlStateManager.enableFog()
    }

    private fun renderBeamSegement(x: Double, y: Double, z: Double, partialTicks: Double, time: Long, yOffset: Int, height: Int, colors: FloatArray, timeLeft: Int) {
        renderBeamSegment(x, y, z, partialTicks, 1.0, time, yOffset, height, colors, timeLeft * 0.2, timeLeft * 0.225)
    }

    fun renderBeamSegment(x: Double, y: Double, z: Double, partialTicks: Double, textureScale: Double, totalWorldTime: Long, yOffset: Int, height: Int, colors: FloatArray, beamRadius: Double, glowRadius: Double) {
        val i = yOffset + height
        GlStateManager.texParameter(3553, 10242, 10497)
        GlStateManager.texParameter(3553, 10243, 10497)
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.pushMatrix()
        GlStateManager.translated(x + 0.5, y, z + 0.5)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        val d0 = Math.floorMod(totalWorldTime, 40L).toDouble() + partialTicks
        val d1 = if (height < 0) d0 else -d0
        val d2 = MathHelper.frac(d1 * 0.2 - MathHelper.floor(d1 * 0.1).toDouble())
        val f = colors[0]
        val f1 = colors[1]
        val f2 = colors[2]
        GlStateManager.pushMatrix()
        GlStateManager.rotated(d0 * 2.25 - 45.0, 0.0, 1.0, 0.0)
        var d3 = 0.0
        var d5 = 0.0
        var d6 = -beamRadius
        val d7 = 0.0
        val d8 = 0.0
        val d9 = -beamRadius
        var d10 = 0.0
        var d11 = 1.0
        var d12 = -1.0 + d2
        var d13 = height.toDouble() * textureScale * (0.5 / beamRadius) + d12
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        bufferbuilder.pos(0.0, i.toDouble(), beamRadius).tex(1.0, d13).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(0.0, yOffset.toDouble(), beamRadius).tex(1.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(beamRadius, yOffset.toDouble(), 0.0).tex(0.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(beamRadius, i.toDouble(), 0.0).tex(0.0, d13).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(0.0, i.toDouble(), d9).tex(1.0, d13).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(0.0, yOffset.toDouble(), d9).tex(1.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(d6, yOffset.toDouble(), 0.0).tex(0.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(d6, i.toDouble(), 0.0).tex(0.0, d13).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(beamRadius, i.toDouble(), 0.0).tex(1.0, d13).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(beamRadius, yOffset.toDouble(), 0.0).tex(1.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(0.0, yOffset.toDouble(), d9).tex(0.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(0.0, i.toDouble(), d9).tex(0.0, d13).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(d6, i.toDouble(), 0.0).tex(1.0, d13).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(d6, yOffset.toDouble(), 0.0).tex(1.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(0.0, yOffset.toDouble(), beamRadius).tex(0.0, d12).color(f, f1, f2, 1.0f).endVertex()
        bufferbuilder.pos(0.0, i.toDouble(), beamRadius).tex(0.0, d13).color(f, f1, f2, 1.0f).endVertex()
        tessellator.draw()
        GlStateManager.popMatrix()
        GlStateManager.enableBlend()
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.depthMask(false)
        d3 = -glowRadius
        val d4 = -glowRadius
        d5 = -glowRadius
        d6 = -glowRadius
        d10 = 0.0
        d11 = 1.0
        d12 = -1.0 + d2
        d13 = height.toDouble() * textureScale + d12
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        bufferbuilder.pos(d3, i.toDouble(), d4).tex(1.0, d13).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(d3, yOffset.toDouble(), d4).tex(1.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, yOffset.toDouble(), d5).tex(0.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, i.toDouble(), d5).tex(0.0, d13).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, i.toDouble(), glowRadius).tex(1.0, d13).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, yOffset.toDouble(), glowRadius).tex(1.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(d6, yOffset.toDouble(), glowRadius).tex(0.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(d6, i.toDouble(), glowRadius).tex(0.0, d13).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, i.toDouble(), d5).tex(1.0, d13).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, yOffset.toDouble(), d5).tex(1.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, yOffset.toDouble(), glowRadius).tex(0.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(glowRadius, i.toDouble(), glowRadius).tex(0.0, d13).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(d6, i.toDouble(), glowRadius).tex(1.0, d13).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(d6, yOffset.toDouble(), glowRadius).tex(1.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(d3, yOffset.toDouble(), d4).tex(0.0, d12).color(f, f1, f2, 0.125f).endVertex()
        bufferbuilder.pos(d3, i.toDouble(), d4).tex(0.0, d13).color(f, f1, f2, 0.125f).endVertex()
        tessellator.draw()
        GlStateManager.popMatrix()
        GlStateManager.enableLighting()
        GlStateManager.enableTexture()
        GlStateManager.depthMask(true)
    }

}