package com.williambl.explosivessquared

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.block.BlockRenderType
import net.minecraft.block.Blocks
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.Vector3f
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.TNTMinecartRenderer
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import java.util.*

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

                matrixStack.rotate(Vector3f.YP.rotationDegrees(-90.0f))
                matrixStack.translate(-0.5, -0.5, 0.5)
                matrixStack.rotate(Vector3f.YP.rotationDegrees(90.0f))
                TNTMinecartRenderer.renderTntFlash(Blocks.TNT.defaultState, matrixStack, buffer, packedLight, entity.getFuse() / 5 % 2 == 0)
                matrixStack.pop()
                super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight)

                /*
                 * The following translate calls were originally one:
                 *     GlStateManager.translated(
                 *         x - blockpos.x.toDouble() - 0.5,
                 *         y - blockpos.y.toDouble(),
                 *         z - blockpos.z.toDouble() - 0.5
                 *     );
                 * When I added rotation, in order to keep it rendering in the same place, it had to be split:
                 *
                 *     Translate x,y,z
                 *     Rotate yaw
                 *     Translate -0.5,0,-0.5
                 *     Rotate pitch
                 *     Translate -blockpos
                 *
                 *  As you can see, it is still the same translation.
                 */

            }
        }
    }

    override fun getEntityTexture(entity: ExplosiveEntity): ResourceLocation {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE
    }
}