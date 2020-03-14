package com.williambl.explosivessquared

import com.williambl.explosivessquared.block.ExplosiveBlock
import com.williambl.explosivessquared.block.MissileBlock
import com.williambl.explosivessquared.entity.ExplosiveEntity
import com.williambl.explosivessquared.entity.MissileEntity
import com.williambl.explosivessquared.item.BoomStickItem
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.ResourceLocation

class ExplosiveType(val name: String) {

    private var blockProperties = Block.Properties.create(Material.TNT)
    private var itemProperties = Item.Properties().group(ItemGroup.REDSTONE)
    private var missileBlockProperties = Block.Properties.create(Material.TNT)
    private var missileItemProperties = Item.Properties().group(ItemGroup.REDSTONE)
    private var boomStickProperties = Item.Properties().group(ItemGroup.TOOLS)

    public var explodeFunction: (ExplosiveEntity) -> Unit = {}
        private set
    public var clientFunction: (ExplosiveEntity) -> Unit = {}
        private set
    public var fuse: Int = 80
        private set
    public lateinit var block: ExplosiveBlock
        private set
    public lateinit var missileBlock: MissileBlock
        private set
    public lateinit var item: BlockItem
        private set
    public lateinit var missileItem: BlockItem
        private set
    public lateinit var boomStickItem: BoomStickItem
        private set
    public lateinit var entityType: EntityType<out ExplosiveEntity>
        private set
    public lateinit var missileEntityType: EntityType<out MissileEntity>
        private set

    public var shouldCreateMissile = true
        private set
    public var shouldCreateBoomStick = true
        private set

    public var texture: ResourceLocation = Blocks.AIR.registryName ?: ResourceLocation("")
        get() =
            if (field == Blocks.AIR.registryName)
                ResourceLocation("explosivessquared:block/explosive/${block.registryName?.path ?: "explosive"}")
            else field
        private set

    fun setExplodeFunction(explodeFunction: (ExplosiveEntity) -> Unit): ExplosiveType {
        this.explodeFunction = explodeFunction
        return this
    }

    fun setClientFunction(clientFunction: (ExplosiveEntity) -> Unit): ExplosiveType {
        this.clientFunction = clientFunction
        return this
    }

    fun setFuseLength(fuseLength: Int): ExplosiveType {
        this.fuse = fuseLength
        return this
    }

    fun setBlockProperties(properties: Block.Properties): ExplosiveType {
        this.blockProperties = properties
        return this
    }

    fun setItemProperties(properties: Item.Properties): ExplosiveType {
        this.itemProperties = properties
        return this
    }

    fun setBoomStickProperties(properties: Item.Properties): ExplosiveType {
        this.boomStickProperties = properties
        return this
    }

    fun noMissile(): ExplosiveType {
        this.shouldCreateMissile = false
        return this
    }

    fun noBoomstick(): ExplosiveType {
        this.shouldCreateBoomStick = false
        return this
    }

    fun setTexture(texture: ResourceLocation): ExplosiveType {
        this.texture = texture
        return this
    }

    fun createBlock(): ExplosiveBlock {
        block = ExplosiveBlock(this, blockProperties)
        block.setRegistryName(name)
        return block
    }

    fun createMissileBlock(): MissileBlock? {
        if (!this.shouldCreateMissile)
            return null
        if (this::block.isInitialized) {
            missileBlock = MissileBlock(this, missileBlockProperties)
            missileBlock.setRegistryName(name + "_missile")
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

    fun createMissileItem(): BlockItem? {
        if (!this.shouldCreateMissile)
            return null
        if (this::missileBlock.isInitialized) {
            val item = BlockItem(missileBlock, missileItemProperties)
            item.setRegistryName(name + "_missile")
            return item
        } else throw UninitializedPropertyAccessException("Tried to create Missile Item before Missile Block!")
    }

    fun createBoomStick(): BoomStickItem? {
        if (!this.shouldCreateBoomStick)
            return null
        if (this::block.isInitialized) {
            boomStickItem = BoomStickItem(this, boomStickProperties)
            boomStickItem.setRegistryName(name + "_boomstick")
            return boomStickItem
        } else throw UninitializedPropertyAccessException("Tried to create Item before Block!")
    }

    fun createEntityType(): EntityType<*>? {
        if (this::block.isInitialized) {
            entityType = EntityType.Builder.create(::ExplosiveEntity, EntityClassification.MISC).build(name).setRegistryName(name) as EntityType<out ExplosiveEntity>
            return entityType
        } else throw UninitializedPropertyAccessException("Tried to create EntityType before Block!")
    }

    fun createMissileEntityType(): EntityType<*>? {
        if (!this.shouldCreateMissile)
            return null
        if (this::missileBlock.isInitialized) {
            if (this::entityType.isInitialized) {
                missileEntityType = EntityType.Builder.create(::MissileEntity, EntityClassification.MISC).setTrackingRange(256).build(name + "_missile").setRegistryName(name + "_missile") as EntityType<out MissileEntity>
                return missileEntityType
            } else throw UninitializedPropertyAccessException("Tried to create Missile EntityType before EntityType!")
        } else throw UninitializedPropertyAccessException("Tried to create Missile EntityType before Missile Block!")
    }
}