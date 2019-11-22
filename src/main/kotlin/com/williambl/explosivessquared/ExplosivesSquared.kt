package com.williambl.explosivessquared

import com.williambl.explosivessquared.objectholders.BlockHolder
import net.alexwells.kottle.KotlinEventBusSubscriber
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.item.TNTEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager

@Mod(ExplosivesSquared.modid)
@KotlinEventBusSubscriber(modid = ExplosivesSquared.modid, bus = KotlinEventBusSubscriber.Bus.MOD)
object ExplosivesSquared {

    const val modid = "explosivessquared"
    private val LOGGER = LogManager.getLogger()

    @SubscribeEvent
    public fun setup(event: FMLCommonSetupEvent) {
    }

    @SubscribeEvent
    public fun doClientStuff(event: FMLClientSetupEvent) {
    }

    @SubscribeEvent
    fun onServerStarting(event: FMLServerStartingEvent) {
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.register(
                ExplosiveBlock(Block.Properties.create(Material.TNT),
                        { worldIn, pos, entityIn ->
                            if (!worldIn.isRemote) {
                                val tntentity = TNTEntity(worldIn, (pos.getX().toFloat() + 0.5f).toDouble(), pos.getY().toDouble(), (pos.getZ().toFloat() + 0.5f).toDouble(), entityIn)
                                worldIn.addEntity(tntentity)
                                worldIn.playSound(null as PlayerEntity?, tntentity.posX, tntentity.posY, tntentity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f)
                            }
                        },
                        { worldIn, pos, explosionIn ->
                            if (!worldIn.isRemote) {
                                val tntentity = TNTEntity(worldIn, (pos.x.toFloat() + 0.5f).toDouble(), pos.y.toDouble(), (pos.z.toFloat() + 0.5f).toDouble(), explosionIn?.explosivePlacedBy)
                                tntentity.fuse = (worldIn.rand.nextInt(tntentity.fuse / 4) + tntentity.fuse / 8).toShort().toInt()
                                worldIn.addEntity(tntentity)
                            }
                        }
                ).setRegistryName("explosive")
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.register(
                BlockItem(BlockHolder.explosiveBlock, Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(BlockHolder.explosiveBlock.registryName)
        )
    }


}
