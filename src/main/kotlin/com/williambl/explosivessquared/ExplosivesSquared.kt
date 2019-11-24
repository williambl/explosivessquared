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

    var explosives: List<ExplosiveBuilder> = listOf(
            ExplosiveBuilder("big_tnt").setExplodeFunction(regularExplosion(15f)),
            ExplosiveBuilder("slow_tnt")
                    .setFuseLength(160)
                    .setExplodeFunction(regularExplosion(15f)),
            ExplosiveBuilder("vegetation_destroyer")
                    .setExplodeFunction(vegetationDestroyerExplosion(8.0)),
            ExplosiveBuilder("gravitationaliser")
                    .setExplodeFunction(gravitationalisingExplosion(8.0)),
            ExplosiveBuilder("tnt_rainer")
                    .setExplodeFunction(tntRainingExplosion(16, 16.0)),
            ExplosiveBuilder("repulsor_tnt")
                    .setExplodeFunction(repellingExplosion(8.0)),
            ExplosiveBuilder("attractor_tnt")
                    .setExplodeFunction(attractingExplosion(8.0))
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
                *explosives.map { it.createItem() }.toTypedArray(),
                *explosives.map { it.createBoomStick() }.toTypedArray()
        )
    }

    @SubscribeEvent
    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<out Entity>>) {
        event.registry.registerAll(
                *explosives.map { it.createEntityType() }.toTypedArray()
        )
    }

}
