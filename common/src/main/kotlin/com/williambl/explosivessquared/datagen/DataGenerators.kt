package com.williambl.explosivessquared.datagen
/*
import com.google.gson.GsonBuilder
import com.williambl.explosivessquared.ExplosivesSquared
import com.williambl.explosivessquared.objectholders.ItemHolder
import net.minecraft.block.Block
import net.minecraft.data.DataGenerator
import net.minecraft.data.DirectoryCache
import net.minecraft.data.IDataProvider
import net.minecraft.data.LootTableProvider
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.*
import net.minecraftforge.client.model.generators.*
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.*

class ItemModels(generator: DataGenerator?, existingFileHelper: ExistingFileHelper?) :
        ItemModelProvider(generator, ExplosivesSquared.modid, existingFileHelper) {

    override fun getName(): String {
        return "Explosives Squared Item Models"
    }

    override fun registerModels() {
        ExplosivesSquared.explosives.forEach { makeItemModelFromBlock(it.block) }
        ExplosivesSquared.explosives.forEach { if (it.shouldCreateMissile) makeItemModelFromBlock(it.missileBlock) }
        ExplosivesSquared.explosives.forEach { if (it.shouldCreateBoomStick) makeBasicItemModel(it.boomStickItem, modLoc("item/boomstick")) }
        makeBasicItemModel(ItemHolder.targeter, modLoc("item/targeter"))
    }

    private fun makeItemModelFromBlock(block: Block) {
        val path = block.registryName!!.path
        getBuilder(path)
                .parent(ModelFile.UncheckedModelFile(modLoc("block/$path")))
    }

    private fun makeBasicItemModel(item: Item, texture: ResourceLocation) {
        val path = item.registryName!!.path
        getBuilder(path)
                .parent(ModelFile.UncheckedModelFile(mcLoc("item/generated")))
                .texture("layer0", texture)
    }
}

class BlockStates(gen: DataGenerator?, existingFileHelper: ExistingFileHelper?) : BlockStateProvider(gen, ExplosivesSquared.modid, existingFileHelper) {
    override fun registerStatesAndModels() {
        ExplosivesSquared.explosives.forEach {
            makeExplosiveBlockState(it.block, it.texture)
            if (it.shouldCreateMissile) makeMissileBlockState(it.missileBlock)
        }
    }

    private fun makeCubeBlockState(block: Block, texture: ResourceLocation) {
        val model = models().getBuilder(block.registryName!!.path)
                .parent(models().getExistingFile(mcLoc("block/cube_all")))
                .texture("all", texture)
        getVariantBuilder(block).forAllStates { ConfiguredModel.builder().modelFile(model).build() }
    }

    private fun makeExplosiveBlockState(block: Block,
                                        textureWithin: ResourceLocation,
                                        textureBottom: ResourceLocation = modLoc("block/explosive/explosive_bottom"),
                                        textureTop: ResourceLocation = modLoc("block/explosive/explosive_top"),
                                        textureSide: ResourceLocation = modLoc("block/explosive/explosive_side")) {
        val model = models().getBuilder(block.registryName!!.path)
                .parent(models().getExistingFile(modLoc("block/explosive")))
                .texture("within", textureWithin)
                .texture("bottom", textureBottom)
                .texture("top", textureTop)
                .texture("side", textureSide)
        getVariantBuilder(block).forAllStates { ConfiguredModel.builder().modelFile(model).build() }
    }

    private fun makeMissileBlockState(block: Block,
                                      bodyTexture: ResourceLocation = modLoc("block/missile/body"),
                                      engineTexture: ResourceLocation = modLoc("block/missile/engine"),
                                      topTexture: ResourceLocation = modLoc("block/missile/top")) {
        val model = models().getBuilder(block.registryName!!.path)
                .parent(models().getExistingFile(modLoc("block/missile")))
                .texture("body", bodyTexture)
                .texture("engine", engineTexture)
                .texture("top", topTexture)
        getVariantBuilder(block).forAllStates { ConfiguredModel.builder().modelFile(model).build() }
    }
}

class LootTables(val generator: DataGenerator) : LootTableProvider(generator) {
    private val logger = LogManager.getLogger()
    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    private val lootTables: MutableMap<Block, LootTable.Builder> = HashMap()

    private fun addTables() {
        ExplosivesSquared.explosives.forEach {
            lootTables.put(it.block, createStandardTable(it.name, it.block))
            if (it.shouldCreateMissile) lootTables.put(it.missileBlock, createStandardTable(it.missileBlock.registryName!!.path, it.missileBlock))
        }
    }

    // Subclasses can call this if they want a standard loot table. Modify this for your own needs
    private fun createStandardTable(name: String, block: Block): LootTable.Builder {
        val builder = LootPool.builder()
                .name(name)
                .rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(block))
        return LootTable.builder().addLootPool(builder)
    }

    override fun act(cache: DirectoryCache) {
        addTables()

        val tables = HashMap<ResourceLocation, LootTable>()
        for ((key, value) in lootTables) {
            tables[key.lootTable] = value.setParameterSet(LootParameterSets.BLOCK).build()
        }
        writeTables(cache, tables)
    }

    // Actually write out the tables in the output folder
    private fun writeTables(cache: DirectoryCache, tables: Map<ResourceLocation, LootTable>) {
        val outputFolder = generator.outputFolder
        println(outputFolder)
        tables.forEach { (key, lootTable) ->
            val path = outputFolder.resolve("data/" + key.namespace + "/loot_tables/" + key.path + ".json")
            try {
                IDataProvider.save(gson, cache, LootTableManager.toJson(lootTable), path)
            } catch (e: IOException) {
                logger.error("Couldn't write loot table {}", path, e)
            }
        }
    }

    override fun getName(): String {
        return "Explosives Squared Loot Tables"
    }
}*/