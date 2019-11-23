package com.williambl.explosivessquared

import net.alexwells.kottle.KotlinEventBusSubscriber
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
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

    lateinit var explosive: ExplosiveBuilder

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
        explosive = ExplosiveBuilder("explosive")
        event.registry.register(
                explosive.createBlock()
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.register(
                explosive.createItem()
        )
    }

    @SubscribeEvent
    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<out Entity>>) {
        event.registry.register(
                explosive.createEntityType()
        )
    }

}
