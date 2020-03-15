package com.williambl.explosivessquared

import com.williambl.explosivessquared.block.tileentity.MissileTileEntity
import com.williambl.explosivessquared.client.render.ExplosiveRenderer
import com.williambl.explosivessquared.client.render.GlassingRayBeamRenderer
import com.williambl.explosivessquared.datagen.BlockStates
import com.williambl.explosivessquared.datagen.ItemModels
import com.williambl.explosivessquared.datagen.LootTables
import com.williambl.explosivessquared.entity.GlassingRayBeamEntity
import com.williambl.explosivessquared.item.TargeterItem
import com.williambl.explosivessquared.objectholders.EntityTypeHolder
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.function.Supplier

@Mod(ExplosivesSquared.modid)
@Mod.EventBusSubscriber(modid = ExplosivesSquared.modid, bus = Mod.EventBusSubscriber.Bus.MOD)
object ExplosivesSquared {

    const val modid = "explosivessquared"
    private val LOGGER = LogManager.getLogger()

    val threadPool = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    var explosives: List<ExplosiveType> = listOf(
            ExplosiveType("big_tnt")
                    .setExplodeFunction(regularExplosion(15f))
                    .setTexture(ResourceLocation("minecraft:block/tnt_side")),
            ExplosiveType("slow_tnt")
                    .setFuseLength(160)
                    .setExplodeFunction(regularExplosion(15f))
                    .noBoomstick()
                    .noMissile(),
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
                    .setExplodeFunction(napalmExplosion(8.0))
                    .setTexture(ResourceLocation("minecraft:block/lava_still")),
            ExplosiveType("frost_bomb")
                    .setExplodeFunction(frostExplosion(8.0))
                    .setTexture(ResourceLocation("minecraft:block/packed_ice")),
            ExplosiveType("nether_bomb")
                    .setExplodeFunction(netherExplosion(8.0))
                    .setTexture(ResourceLocation("minecraft:block/nether_portal")),
            ExplosiveType("glassing_ray")
                    .setExplodeFunction(glassingRay(16.0))
                    .setClientFunction(glassingRayClient(16.0))
    )

    lateinit var explosiveMap: Map<String, ExplosiveType>

    @SubscribeEvent
    public fun setup(event: FMLCommonSetupEvent) {
        explosiveMap = explosives.map { it.name to it }.toMap()
        explosives.forEach {
            if (it.shouldCreateMissile)
                RenderTypeLookup.setRenderLayer(it.missileBlock, RenderType.getCutout())
            RenderTypeLookup.setRenderLayer(it.block, RenderType.getCutout())
        }
    }

    @SubscribeEvent
    public fun doClientStuff(event: FMLClientSetupEvent) {
        explosives.forEach {
            RenderingRegistry.registerEntityRenderingHandler(it.entityType, ::ExplosiveRenderer)
            if (it.shouldCreateMissile)
                RenderingRegistry.registerEntityRenderingHandler(it.missileEntityType, ::ExplosiveRenderer)
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityTypeHolder.glassingRayBeam, ::GlassingRayBeamRenderer)
    }

    @SubscribeEvent
    fun onServerStarting(event: FMLServerStartingEvent) {
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.registerAll(
                *explosives.map { it.createBlock() }.toTypedArray(),
                *explosives.mapNotNull { it.createMissileBlock() }.toTypedArray()
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.registerAll(
                *explosives.map { it.createItem() }.toTypedArray(),
                *explosives.mapNotNull { it.createMissileItem() }.toTypedArray(),
                *explosives.mapNotNull { it.createBoomStick() }.toTypedArray(),
                TargeterItem(Item.Properties().group(ItemGroup.TOOLS)).setRegistryName("targeter")
        )
    }

    @SubscribeEvent
    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<out Entity>>) {
        event.registry.registerAll(
                *explosives.map { it.createEntityType() }.toTypedArray(),
                *explosives.mapNotNull { it.createMissileEntityType() }.toTypedArray(),
                EntityType.Builder.create(::GlassingRayBeamEntity, EntityClassification.MISC).build("glassing_ray_beam").setRegistryName("glassing_ray_beam")
        )
    }

    @SubscribeEvent
    fun registerTileEntityTypes(event: RegistryEvent.Register<TileEntityType<out TileEntity>>) {
        event.registry.register(TileEntityType.Builder.create(Supplier { MissileTileEntity() }, *explosives.mapNotNull { if (it.shouldCreateMissile) it.missileBlock else null }.toTypedArray()).build(null).setRegistryName("missile"))
    }

    @SubscribeEvent
    fun setupDataGenerators(event: GatherDataEvent) {
        event.generator.addProvider(ItemModels(event.generator, event.existingFileHelper))
        event.generator.addProvider(BlockStates(event.generator, event.existingFileHelper))
        event.generator.addProvider(LootTables(event.generator))
    }

}
