package com.williambl.explosivessquared

import com.williambl.explosivessquared.block.tileentity.MissileTileEntity
import com.williambl.explosivessquared.entity.ExplosiveEntity
import com.williambl.explosivessquared.entity.GlassingRayBeamEntity
import com.williambl.explosivessquared.item.TargeterItem
import kotlinx.coroutines.asCoroutineDispatcher
import me.shedaniel.architectury.registry.DeferredRegister
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvents
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.function.Supplier

object ExplosivesSquared {

    const val modid = "explosivessquared"
    private val LOGGER = LogManager.getLogger()

    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(modid, Registry.ITEM_KEY)
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(modid, Registry.BLOCK_KEY)
    val ENTITY_TYPES: DeferredRegister<EntityType<*>> = DeferredRegister.create(modid, Registry.ENTITY_TYPE_KEY)
    val TILE_ENTITY_TYPES: DeferredRegister<TileEntityType<*>> = DeferredRegister.create(modid, Registry.BLOCK_ENTITY_TYPE_KEY)

    val targeter = ITEMS.register(ResourceLocation("explosivessquared:targeter")) {
        TargeterItem(Item.Properties().group(ItemGroup.TOOLS))
    }
    val glassingRayBeam = ENTITY_TYPES.register(ResourceLocation("explosivessquared:glassing_ray_beam")) {
        EntityType.Builder.create(::GlassingRayBeamEntity, EntityClassification.MISC).build("glassing_ray_beam")
    }
    val missile = TILE_ENTITY_TYPES.register(ResourceLocation("explosivessquared:missile")) {
        TileEntityType.Builder.create({ MissileTileEntity() }, *explosives.mapNotNull { if (it.shouldCreateMissile) it.missileBlock else null }.toTypedArray()).build(null)
    }

    val threadPool = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    var explosives: List<ExplosiveType> = listOf(
            ExplosiveType(ResourceLocation(modid, "big_tnt"))
                    .setExplodeFunction(regularExplosion(15f))
                    .setClientFunction(explosionSound)
                    .setTexture(ResourceLocation("minecraft:block/tnt_side")),
            ExplosiveType(ResourceLocation(modid, "slow_tnt"))
                    .setFuseLength(160)
                    .setExplodeFunction(regularExplosion(15f))
                    .setClientFunction(explosionSound)
                    .noBoomstick()
                    .noMissile(),
            ExplosiveType(ResourceLocation(modid, "vegetation_destroyer"))
                    .setExplodeFunction(vegetationDestroyerExplosion(8.0))
                    .setClientFunction(explosionSound),
            ExplosiveType(ResourceLocation(modid, "gravitationaliser"))
                    .setExplodeFunction(gravitationalisingExplosion(8.0))
                    .setClientFunction(playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, volume = 4.0f)),
            ExplosiveType(ResourceLocation(modid, "tnt_rainer"))
                    .setExplodeFunction(tntRainingExplosion(16, 16.0)),
            ExplosiveType(ResourceLocation(modid, "repulsor_tnt"))
                    .setExplodeFunction(repellingExplosion(8.0))
                    .setClientFunction(explosionSound),
            ExplosiveType(ResourceLocation(modid, "attractor_tnt"))
                    .setExplodeFunction(attractingExplosion(8.0))
                    .setClientFunction(explosionSound),
            ExplosiveType(ResourceLocation(modid, "napalm"))
                    .setExplodeFunction(napalmExplosion(8.0))
                    .setTexture(ResourceLocation("minecraft:block/lava_still")),
            ExplosiveType(ResourceLocation(modid, "frost_bomb"))
                    .setExplodeFunction(frostExplosion(8.0))
                    .setClientFunction(playSound(SoundEvents.ENTITY_SNOW_GOLEM_AMBIENT, volume = 4.0f))
                    .setTexture(ResourceLocation("minecraft:block/packed_ice")),
            ExplosiveType(ResourceLocation(modid, "nether_bomb"))
                    .setExplodeFunction(netherExplosion(8.0))
                    .setClientFunction(playSound(SoundEvents.BLOCK_PORTAL_TRIGGER, volume = 4.0f))
                    .setTexture(ResourceLocation("minecraft:block/nether_portal")),
            ExplosiveType(ResourceLocation(modid, "glassing_ray"))
                    .setExplodeFunction(glassingRay(16.0))
                    .setClientFunction(glassingRayClient(16.0))
                    .setTexture(ResourceLocation("explosivessquared:entity/glassing_ray_beam")),
            ExplosiveType(ResourceLocation(modid, "nuke"))
                    .setExplodeFunction(removeAllBlocks(128.0))
    )

    lateinit var explosiveMap: Map<ResourceLocation, ExplosiveType>

    fun getTypeFor(block: Block): ExplosiveType = explosives.find {
        it.block == block || (it.shouldCreateMissile && it.missileBlock == block)
    } ?: throw IllegalArgumentException("must be an explosive")

    fun getTypeFor(entityType: EntityType<*>): ExplosiveType = explosives.find {
        it.entityType == entityType || (it.shouldCreateMissile && it.missileEntityType == entityType)
    } ?: throw IllegalArgumentException("must be an explosive")

    fun init() {
        explosiveMap = explosives.map { it.name to it }.toMap()

        explosives.forEach {
            BLOCKS.register(it.name, it::createBlock)
            ITEMS.register(it.name, it::createItem)
            ENTITY_TYPES.register(it.name, it::createEntityType)
        }
        explosives.filter { it.shouldCreateMissile }.forEach {
            val name = ResourceLocation(it.name.namespace, it.name.path+"_missile")
            BLOCKS.register(name, it::createMissileBlock)
            ITEMS.register(name, it::createMissileItem)
            ENTITY_TYPES.register(name, it::createMissileEntityType)
        }
        explosives.filter { it.shouldCreateBoomStick }.forEach {
            ITEMS.register(ResourceLocation(it.name.namespace, it.name.path+"_boomstick"), it::createBoomStick)
        }


        BLOCKS.register()
        ITEMS.register()
        ENTITY_TYPES.register()
        TILE_ENTITY_TYPES.register()
    }
}
