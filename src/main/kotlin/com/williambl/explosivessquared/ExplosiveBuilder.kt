package com.williambl.explosivessquared

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup

class ExplosiveBuilder(val name: String) {

    private var entityExplosion: (ExplosiveEntity) -> Unit = {}
    private var fuse: Int = 80
    private var blockProperties = Block.Properties.create(Material.TNT)
    private var itemProperties = Item.Properties().group(ItemGroup.REDSTONE)

    private lateinit var block: ExplosiveBlock

    fun setExplodeFunction(explodeFunction: (ExplosiveEntity) -> Unit): ExplosiveBuilder {
        this.entityExplosion = explodeFunction
        return this
    }

    fun setFuseLength(fuseLength: Int): ExplosiveBuilder {
        this.fuse = fuseLength
        return this
    }

    fun setBlockProperties(properties: Block.Properties): ExplosiveBuilder {
        this.blockProperties = properties
        return this
    }

    fun setItemProperties(properties: Item.Properties): ExplosiveBuilder {
        this.itemProperties = properties
        return this
    }

    fun createBlock(): ExplosiveBlock {
        block = ExplosiveBlock(blockProperties, entityExplosion)
        block.setRegistryName(name)
        return block
    }

    fun createItem(): BlockItem {
        if (this::block.isInitialized) {
            val item = BlockItem(block, itemProperties)
            item.setRegistryName(name)
            return item
        } else throw IllegalStateException("Tried to create Item before Block!")
    }

    fun createEntityType(): EntityType<*>? {
        if (this::block.isInitialized) {
            val type = EntityType.Builder.create(::ExplosiveEntity, EntityClassification.MISC).build(name).setRegistryName(name)
            ExplosivesSquared.entityTypesToBlocks[type as EntityType<out ExplosiveEntity>] = block
            ExplosivesSquared.blocksToEntityTypes[block] = type
            return type
        } else throw IllegalStateException("Tried to create EntityType before Block!")
    }
}