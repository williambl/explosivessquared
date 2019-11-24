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
    private var missileBlockProperties = Block.Properties.create(Material.TNT)
    private var missileItemProperties = Item.Properties().group(ItemGroup.REDSTONE)
    private var boomStickProperties = Item.Properties().group(ItemGroup.TOOLS)

    private lateinit var block: ExplosiveBlock
    private lateinit var missileBlock: MissileBlock
    private lateinit var entityType: EntityType<out ExplosiveEntity>
    private lateinit var missileEntityType: EntityType<out MissileEntity>

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

    fun setBoomStickProperties(properties: Item.Properties): ExplosiveBuilder {
        this.boomStickProperties = properties
        return this
    }

    fun createBlock(): ExplosiveBlock {
        block = ExplosiveBlock(blockProperties, entityExplosion)
        block.setRegistryName(name)
        return block
    }

    fun createMissileBlock(): MissileBlock {
        if (this::block.isInitialized) {
            missileBlock = MissileBlock(block, missileBlockProperties)
            missileBlock.setRegistryName(name + "_missile")
            ExplosivesSquared.validMissileBlocks.add(missileBlock)
            return missileBlock
        } else throw UninitializedPropertyAccessException("Tried to create Missile Block before Block!")
    }

    fun createItem(): BlockItem {
        if (this::block.isInitialized) {
            val item = BlockItem(block, itemProperties)
            item.setRegistryName(name)
            return item
        } else throw UninitializedPropertyAccessException("Tried to create Item before Block!")
    }

    fun createMissileItem(): BlockItem {
        if (this::missileBlock.isInitialized) {
            val item = BlockItem(missileBlock, missileItemProperties)
            item.setRegistryName(name + "_missile")
            return item
        } else throw UninitializedPropertyAccessException("Tried to create Missile Item before Missile Block!")
    }

    fun createBoomStick(): BoomStickItem {
        if (this::block.isInitialized) {
            val item = BoomStickItem(block, boomStickProperties)
            item.setRegistryName(name + "_boomstick")
            return item
        } else throw UninitializedPropertyAccessException("Tried to create Item before Block!")
    }

    fun createEntityType(): EntityType<*>? {
        if (this::block.isInitialized) {
            entityType = EntityType.Builder.create(::ExplosiveEntity, EntityClassification.MISC).build(name).setRegistryName(name) as EntityType<out ExplosiveEntity>
            ExplosivesSquared.entityTypesToBlocks[entityType] = block
            ExplosivesSquared.blocksToEntityTypes[block] = entityType
            return entityType
        } else throw UninitializedPropertyAccessException("Tried to create EntityType before Block!")
    }

    fun createMissileEntityType(): EntityType<*>? {
        if (this::missileBlock.isInitialized) {
            if (this::entityType.isInitialized) {
                missileEntityType = EntityType.Builder.create(::MissileEntity, EntityClassification.MISC).build(name + "_missile").setRegistryName(name + "_missile") as EntityType<out MissileEntity>
                ExplosivesSquared.entityTypesToMissileEntityTypes[entityType] = missileEntityType
                ExplosivesSquared.missileEntityTypesToEntityTypes[missileEntityType] = entityType
                return missileEntityType
            } else throw UninitializedPropertyAccessException("Tried to create Missile EntityType before EntityType!")
        } else throw UninitializedPropertyAccessException("Tried to create Missile EntityType before Missile Block!")
    }
}