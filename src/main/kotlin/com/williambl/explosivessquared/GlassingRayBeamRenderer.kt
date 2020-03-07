package com.williambl.explosivessquared

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper

class GlassingRayBeamRenderer(rendererManager: EntityRendererManager): EntityRenderer<GlassingRayBeamEntity>(rendererManager) {
    private val texture = ResourceLocation(ExplosivesSquared.modid, "textures/entity/glassing_ray_beam.png")
    private val color = floatArrayOf(1.0f, 1.0f, 1.0f)

    override fun render(entity: GlassingRayBeamEntity, entityYaw: Float, partialTicks: Float, matrixStackIn: MatrixStack, bufferIn: IRenderTypeBuffer, packedLightIn: Int) {
        renderBeamSegment(matrixStackIn, bufferIn, partialTicks, entity.world!!.gameTime, color, entity.getTimeLeft())
    }

    override fun getEntityTexture(entity: GlassingRayBeamEntity): ResourceLocation? {
        return texture
    }

    private fun renderBeamSegment(matrixStackIn: MatrixStack, bufferIn: IRenderTypeBuffer, partialTicks: Float, totalWorldTime: Long, colors: FloatArray, timeLeft: Int) {
        renderBeamSegment(matrixStackIn, bufferIn, BeaconTileEntityRenderer.TEXTURE_BEACON_BEAM, partialTicks, 1.0f, totalWorldTime, 0, 1024, colors, timeLeft * 0.2f, timeLeft * 0.225f)
    }

    private fun renderBeamSegment(matrixStackIn: MatrixStack, bufferIn: IRenderTypeBuffer, textureLocation: ResourceLocation?, partialTicks: Float, textureScale: Float, totalWorldTime: Long, yOffset: Int, height: Int, colors: FloatArray, beamRadius: Float, glowRadius: Float) {
        val i = yOffset + height
        matrixStackIn.push()
        matrixStackIn.translate(0.5, 0.0, 0.5)
        val f = Math.floorMod(totalWorldTime, 40L).toFloat() + partialTicks
        val f1 = if (height < 0) f else -f
        val f2 = MathHelper.frac(f1 * 0.2f - MathHelper.floor(f1 * 0.1f).toFloat())
        val f3 = colors[0]
        val f4 = colors[1]
        val f5 = colors[2]
        matrixStackIn.push()
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(f * 2.25f - 45.0f))
        var f6 = 0.0f
        var f8 = 0.0f
        var f9 = -beamRadius
        val f10 = 0.0f
        val f11 = 0.0f
        val f12 = -beamRadius
        var f13 = 0.0f
        var f14 = 1.0f
        var f15 = -1.0f + f2
        var f16 = height.toFloat() * textureScale * (0.5f / beamRadius) + f15
        renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.getBeaconBeam(textureLocation, false)), f3, f4, f5, 1.0f, yOffset, i, 0.0f, beamRadius, beamRadius, 0.0f, f9, 0.0f, 0.0f, f12, 0.0f, 1.0f, f16, f15)
        matrixStackIn.pop()
        f6 = -glowRadius
        val f7 = -glowRadius
        f8 = -glowRadius
        f9 = -glowRadius
        f13 = 0.0f
        f14 = 1.0f
        f15 = -1.0f + f2
        f16 = height.toFloat() * textureScale + f15
        renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.getBeaconBeam(textureLocation, true)), f3, f4, f5, 0.125f, yOffset, i, f6, f7, glowRadius, f8, f9, glowRadius, glowRadius, glowRadius, 0.0f, 1.0f, f16, f15)
        matrixStackIn.pop()
    }

    private fun renderPart(matrixStackIn: MatrixStack, bufferIn: IVertexBuilder, red: Float, green: Float, blue: Float, alpha: Float, yMin: Int, yMax: Int, p_228840_8_: Float, p_228840_9_: Float, p_228840_10_: Float, p_228840_11_: Float, p_228840_12_: Float, p_228840_13_: Float, p_228840_14_: Float, p_228840_15_: Float, u1: Float, u2: Float, v1: Float, v2: Float) {
        val entry = matrixStackIn.last
        val matrix4f = entry.matrix
        val matrix3f = entry.normal
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_8_, p_228840_9_, p_228840_10_, p_228840_11_, u1, u2, v1, v2)
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_14_, p_228840_15_, p_228840_12_, p_228840_13_, u1, u2, v1, v2)
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_10_, p_228840_11_, p_228840_14_, p_228840_15_, u1, u2, v1, v2)
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_12_, p_228840_13_, p_228840_8_, p_228840_9_, u1, u2, v1, v2)
    }

    private fun addQuad(matrixPos: Matrix4f, matrixNormal: Matrix3f, bufferIn: IVertexBuilder, red: Float, green: Float, blue: Float, alpha: Float, yMin: Int, yMax: Int, x1: Float, z1: Float, x2: Float, z2: Float, u1: Float, u2: Float, v1: Float, v2: Float) {
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x1, z1, u2, v1)
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x1, z1, u2, v2)
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x2, z2, u1, v2)
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x2, z2, u1, v1)
    }

    private fun addVertex(matrixPos: Matrix4f, matrixNormal: Matrix3f, bufferIn: IVertexBuilder, red: Float, green: Float, blue: Float, alpha: Float, y: Int, x: Float, z: Float, texU: Float, texV: Float) {
        bufferIn.pos(matrixPos, x, y.toFloat(), z).color(red, green, blue, alpha).tex(texU, texV).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(matrixNormal, 0.0f, 1.0f, 0.0f).endVertex()
    }

}