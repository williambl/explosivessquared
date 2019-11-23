package com.williambl.explosivessquared

import net.alexwells.kottle.KotlinEventBusSubscriber
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.world.Explosion
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.registry.RenderingRegistry
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

    var explosives: List<ExplosiveBuilder> = listOf(
            ExplosiveBuilder("big_tnt").setExplodeFunction { it.world.createExplosion(it, it.posX, it.posY, it.posZ, 15f, Explosion.Mode.DESTROY) },
            ExplosiveBuilder("slow_tnt")
                    .setFuseLength(160)
                    .setExplodeFunction { it.world.createExplosion(it, it.posX, it.posY, it.posZ, 4f, Explosion.Mode.DESTROY) },
            ExplosiveBuilder("cake_tnt")
                    .setBlockProperties(Block.Properties.create(Material.CAKE))
                    .setItemProperties(Item.Properties().group(ItemGroup.FOOD))
                    .setExplodeFunction { it.world.setBlockState(it.position, Blocks.CAKE.defaultState) }
    )

    @SubscribeEvent
    public fun setup(event: FMLCommonSetupEvent) {
    }

    @SubscribeEvent
    public fun doClientStuff(event: FMLClientSetupEvent) {
        RenderingRegistry.registerEntityRenderingHandler(ExplosiveEntity::class.java, ::ExplosiveRenderer)
    }

    @SubscribeEvent
    fun onServerStarting(event: FMLServerStartingEvent) {
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.registerAll(
                *explosives.map { it.createBlock() }.toTypedArray()
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.registerAll(
                *explosives.map { it.createItem() }.toTypedArray()
        )
    }

    @SubscribeEvent
    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<out Entity>>) {
        event.registry.registerAll(
                *explosives.map { it.createEntityType() }.toTypedArray()
        )
    }

}
