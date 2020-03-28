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
import net.minecraftforge.registries.ForgeRegistry
import net.minecraftforge.registries.RegistryBuilder
import net.minecraftforge.registries.RegistryManager
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.function.Supplier

@Mod(ExplosivesSquared.modid)
@Mod.EventBusSubscriber(modid = ExplosivesSquared.modid, bus = Mod.EventBusSubscriber.Bus.MOD)
object ExplosivesSquared {

    const val modid = "explosivessquared"
    private val LOGGER = LogManager.getLogger()

    val threadPool = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    val explosive_types: ForgeRegistry<ExplosiveType> by lazy {
        RegistryManager.ACTIVE.getRegistry<ExplosiveType>(ResourceLocation(modid, "explosive_types"))
    }

    @SubscribeEvent
    public fun setup(event: FMLCommonSetupEvent) {
        explosive_types.forEach {
            if (it.shouldCreateMissile)
                RenderTypeLookup.setRenderLayer(it.missileBlock, RenderType.getCutout())
            RenderTypeLookup.setRenderLayer(it.block, RenderType.getCutout())
        }
    }

    @SubscribeEvent
    public fun doClientStuff(event: FMLClientSetupEvent) {
        explosive_types.forEach {
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
    fun registerRegistries(event: RegistryEvent.NewRegistry) {
        RegistryBuilder<ExplosiveType>()
                .setName(ResourceLocation(modid, "explosive_types"))
                .setType(ExplosiveType::class.java)
                .setMaxID(Integer.MAX_VALUE - 1)
                .create()
    }

    @SubscribeEvent
    fun registerExplosives(event: RegistryEvent.Register<ExplosiveType>) {
        event.registry.registerAll(
                ExplosiveType()
                        .setExplodeFunction(regularExplosion(15f))
                        .setTexture(ResourceLocation("minecraft:block/tnt_side"))
                        .setRegistryName(ResourceLocation(modid, "big_tnt")),
                ExplosiveType()
                        .setFuseLength(160)
                        .setExplodeFunction(regularExplosion(15f))
                        .noBoomstick()
                        .noMissile()
                        .setRegistryName(ResourceLocation(modid, "slow_tnt")),
                ExplosiveType()
                        .setExplodeFunction(vegetationDestroyerExplosion(8.0))
                        .setRegistryName(ResourceLocation(modid, "vegetation_destroyer")),
                ExplosiveType()
                        .setExplodeFunction(gravitationalisingExplosion(8.0))
                        .setRegistryName(ResourceLocation(modid, "gravitationaliser")),
                ExplosiveType()
                        .setExplodeFunction(tntRainingExplosion(16, 16.0))
                        .setRegistryName(ResourceLocation(modid, "tnt_rainer")),
                ExplosiveType()
                        .setExplodeFunction(repellingExplosion(8.0))
                        .setRegistryName(ResourceLocation(modid, "repulsor_tnt")),
                ExplosiveType()
                        .setExplodeFunction(attractingExplosion(8.0))
                        .setRegistryName(ResourceLocation(modid, "attractor_tnt")),
                ExplosiveType()
                        .setExplodeFunction(napalmExplosion(8.0))
                        .setTexture(ResourceLocation("minecraft:block/lava_still"))
                        .setRegistryName(ResourceLocation(modid, "napalm")),
                ExplosiveType()
                        .setExplodeFunction(frostExplosion(8.0))
                        .setTexture(ResourceLocation("minecraft:block/packed_ice"))
                        .setRegistryName(ResourceLocation(modid, "frost_bomb")),
                ExplosiveType()
                        .setExplodeFunction(netherExplosion(8.0))
                        .setTexture(ResourceLocation("minecraft:block/nether_portal"))
                        .setRegistryName(ResourceLocation(modid, "nether_bomb")),
                ExplosiveType()
                        .setExplodeFunction(glassingRay(16.0))
                        .setClientFunction(glassingRayClient(16.0))
                        .setRegistryName(ResourceLocation(modid, "glassing_ray")),
                ExplosiveType()
                        .setExplodeFunction(removeAllBlocks(128.0))
                        .setRegistryName(ResourceLocation(modid, "nuke"))
        )
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.registerAll(
                *explosive_types.map { it.createBlock() }.toTypedArray(),
                *explosive_types.mapNotNull { it.createMissileBlock() }.toTypedArray()
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.registerAll(
                *explosive_types.map { it.createItem() }.toTypedArray(),
                *explosive_types.mapNotNull { it.createMissileItem() }.toTypedArray(),
                *explosive_types.mapNotNull { it.createBoomStick() }.toTypedArray(),
                TargeterItem(Item.Properties().group(ItemGroup.TOOLS)).setRegistryName("targeter")
        )
    }

    @SubscribeEvent
    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<out Entity>>) {
        event.registry.registerAll(
                *explosive_types.map { it.createEntityType() }.toTypedArray(),
                *explosive_types.mapNotNull { it.createMissileEntityType() }.toTypedArray(),
                EntityType.Builder.create(::GlassingRayBeamEntity, EntityClassification.MISC).build("glassing_ray_beam").setRegistryName("glassing_ray_beam")
        )
    }

    @SubscribeEvent
    fun registerTileEntityTypes(event: RegistryEvent.Register<TileEntityType<out TileEntity>>) {
        event.registry.register(TileEntityType.Builder.create(Supplier { MissileTileEntity() }, *explosive_types.mapNotNull { if (it.shouldCreateMissile) it.missileBlock else null }.toTypedArray()).build(null).setRegistryName("missile"))
    }

    @SubscribeEvent
    fun setupDataGenerators(event: GatherDataEvent) {
        event.generator.addProvider(ItemModels(event.generator, event.existingFileHelper))
        event.generator.addProvider(BlockStates(event.generator, event.existingFileHelper))
        event.generator.addProvider(LootTables(event.generator))
    }

}
