package com.williambl.explosivessquared

import com.williambl.explosivessquared.objectholders.BlockHolder
import net.alexwells.kottle.KotlinEventBusSubscriber
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.world.Explosion
import net.minecraft.world.World
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

    val entityTypesToBlocks: MutableMap<EntityType<out ExplosiveEntity>, ExplosiveBlock> = mutableMapOf()
    val blocksToEntityTypes: MutableMap<ExplosiveBlock, EntityType<out ExplosiveEntity>> = mutableMapOf()

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
                        { entity ->
                            entity.world.createExplosion(entity, entity.posX, entity.posY + (entity.height / 16.0f).toDouble(), entity.posZ, 4.0f, Explosion.Mode.BREAK)
                        }
                ).setRegistryName("explosive")
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.register(
                createItemForBlock(BlockHolder.explosiveBlock)
        )
    }

    @SubscribeEvent
    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<out Entity>>) {
        event.registry.register(
                createEntityTypeForExplosive(BlockHolder.explosiveBlock, ::ExplosiveEntity)
        )
    }

    private fun createItemForBlock(block: Block, properties: Item.Properties = Item.Properties().group(ItemGroup.REDSTONE)): Item {
        return BlockItem(BlockHolder.explosiveBlock, properties).setRegistryName(block.registryName)
    }

    private fun <T : ExplosiveEntity> createEntityTypeForExplosive(block: ExplosiveBlock, factory: (EntityType<T>, World) -> T): EntityType<*>? {
        val type = EntityType.Builder.create(factory, EntityClassification.MISC).build(block.registryName!!.path).setRegistryName(block.registryName)
        entityTypesToBlocks[type as EntityType<out ExplosiveEntity>] = block
        blocksToEntityTypes[block] = type as EntityType<out ExplosiveEntity>
        return type
    }

}
