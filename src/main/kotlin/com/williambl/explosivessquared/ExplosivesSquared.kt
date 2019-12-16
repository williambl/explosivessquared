package com.williambl.explosivessquared

import net.alexwells.kottle.KotlinEventBusSubscriber
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager
import java.util.function.Supplier

@Mod(ExplosivesSquared.modid)
@KotlinEventBusSubscriber(modid = ExplosivesSquared.modid, bus = KotlinEventBusSubscriber.Bus.MOD)
object ExplosivesSquared {

    const val modid = "explosivessquared"
    private val LOGGER = LogManager.getLogger()

    var explosives: List<ExplosiveType> = listOf(
            ExplosiveType("big_tnt").setExplodeFunction(regularExplosion(15f)),
            ExplosiveType("slow_tnt")
                    .setFuseLength(160)
                    .setExplodeFunction(regularExplosion(15f)),
            ExplosiveType("vegetation_destroyer")
                    .setExplodeFunction(vegetationDestroyerExplosion(8.0)),
            ExplosiveType("gravitationaliser")
                    .setExplodeFunction(gravitationalisingExplosion(8.0)),
            ExplosiveType("tnt_rainer")
                    .setExplodeFunction(tntRainingExplosion(16, 16.0)),
            ExplosiveType("repulsor_tnt")
                    .setExplodeFunction(repellingExplosion(8.0)),
            ExplosiveType("attractor_tnt")
                    .setExplodeFunction(attractingExplosion(8.0)),
            ExplosiveType("napalm")
                    .setExplodeFunction(napalmExplosion(8.0)),
            ExplosiveType("frost_bomb")
                    .setExplodeFunction(frostExplosion(8.0)),
            ExplosiveType("nether_bomb")
                    .setExplodeFunction(netherExplosion(8.0))
    )

    lateinit var explosiveMap: Map<String, ExplosiveType>

    @SubscribeEvent
    public fun setup(event: FMLCommonSetupEvent) {
        explosiveMap = explosives.map { it.name to it }.toMap()
    }

    @SubscribeEvent
    public fun doClientStuff(event: FMLClientSetupEvent) {
        RenderingRegistry.registerEntityRenderingHandler(ExplosiveEntity::class.java, ::ExplosiveRenderer)
        RenderingRegistry.registerEntityRenderingHandler(MissileEntity::class.java, ::ExplosiveRenderer)
    }

    @SubscribeEvent
    fun onServerStarting(event: FMLServerStartingEvent) {
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.registerAll(
                *explosives.map { it.createBlock() }.toTypedArray(),
                *explosives.map { it.createMissileBlock() }.toTypedArray()
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.registerAll(
                *explosives.map { it.createItem() }.toTypedArray(),
                *explosives.map { it.createMissileItem() }.toTypedArray(),
                *explosives.map { it.createBoomStick() }.toTypedArray(),
                TargeterItem(Item.Properties().group(ItemGroup.TOOLS)).setRegistryName("targeter")
        )
    }

    @SubscribeEvent
    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<out Entity>>) {
        event.registry.registerAll(
                *explosives.map { it.createEntityType() }.toTypedArray(),
                *explosives.map { it.createMissileEntityType() }.toTypedArray()
        )
    }

    @SubscribeEvent
    fun registerTileEntityTypes(event: RegistryEvent.Register<TileEntityType<out TileEntity>>) {
        event.registry.register(TileEntityType.Builder.create(Supplier { MissileTileEntity() }, *explosives.map { it.missileBlock }.toTypedArray()).build(null).setRegistryName("missile"))
    }

    @SubscribeEvent
    fun setupDataGenerators(event: GatherDataEvent) {
        event.generator.addProvider(ItemModels(event.generator, event.existingFileHelper))
        event.generator.addProvider(BlockStates(event.generator, event.existingFileHelper))
        event.generator.addProvider(LootTables(event.generator))
    }

}
