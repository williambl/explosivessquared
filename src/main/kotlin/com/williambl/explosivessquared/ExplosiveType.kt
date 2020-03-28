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
import net.minecraftforge.registries.IForgeRegistryEntry

class ExplosiveType : IForgeRegistryEntry<ExplosiveType> {

    private lateinit var registryName: ResourceLocation

    private var blockProperties = Block.Properties.create(Material.TNT)
    private var itemProperties = Item.Properties().group(ItemGroup.REDSTONE)
    private var missileBlockProperties = Block.Properties.create(Material.TNT)
    private var missileItemProperties = Item.Properties().group(ItemGroup.REDSTONE)
    private var boomStickProperties = Item.Properties().group(ItemGroup.TOOLS)

    var explodeFunction: (ExplosiveEntity) -> Unit = {}
        private set
    var clientFunction: (ExplosiveEntity) -> Unit = {}
        private set
    var fuse: Int = 80
        private set
    lateinit var block: ExplosiveBlock
        private set
    lateinit var missileBlock: MissileBlock
        private set
    lateinit var item: BlockItem
        private set
    lateinit var missileItem: BlockItem
        private set
    lateinit var boomStickItem: BoomStickItem
        private set
    lateinit var entityType: EntityType<out ExplosiveEntity>
        private set
    lateinit var missileEntityType: EntityType<out MissileEntity>
        private set

    var shouldCreateMissile = true
        private set
    var shouldCreateBoomStick = true
        private set

    var texture: ResourceLocation = Blocks.AIR.registryName ?: ResourceLocation("")
        get() =
            if (field == Blocks.AIR.registryName)
                ResourceLocation("explosivessquared:block/explosive/${registryName.path ?: "explosive"}")
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
        block.registryName = registryName
        return block
    }

    fun createMissileBlock(): MissileBlock? {
        if (!this.shouldCreateMissile)
            return null
        if (this::block.isInitialized) {
            missileBlock = MissileBlock(this, missileBlockProperties)
            missileBlock.setRegistryName("${registryName.namespace}:${registryName.path}_missile")
            return missileBlock
        } else throw UninitializedPropertyAccessException("Tried to create Missile Block before Block!")
    }

    fun createItem(): BlockItem {
        if (this::block.isInitialized) {
            val item = BlockItem(block, itemProperties)
            item.registryName = registryName
            return item
        } else throw UninitializedPropertyAccessException("Tried to create Item before Block!")
    }

    fun createMissileItem(): BlockItem? {
        if (!this.shouldCreateMissile)
            return null
        if (this::missileBlock.isInitialized) {
            val item = BlockItem(missileBlock, missileItemProperties)
            item.setRegistryName("${registryName.namespace}:${registryName.path}_missile")
            return item
        } else throw UninitializedPropertyAccessException("Tried to create Missile Item before Missile Block!")
    }

    fun createBoomStick(): BoomStickItem? {
        if (!this.shouldCreateBoomStick)
            return null
        if (this::block.isInitialized) {
            boomStickItem = BoomStickItem(this, boomStickProperties)
            boomStickItem.setRegistryName("${registryName.namespace}:${registryName.path}_boomstick")
            return boomStickItem
        } else throw UninitializedPropertyAccessException("Tried to create Item before Block!")
    }

    fun createEntityType(): EntityType<*>? {
        if (this::block.isInitialized) {
            entityType = EntityType.Builder.create(::ExplosiveEntity, EntityClassification.MISC).build(registryName.path).setRegistryName(registryName) as EntityType<out ExplosiveEntity>
            return entityType
        } else throw UninitializedPropertyAccessException("Tried to create EntityType before Block!")
    }

    fun createMissileEntityType(): EntityType<*>? {
        if (!this.shouldCreateMissile)
            return null
        if (this::missileBlock.isInitialized) {
            if (this::entityType.isInitialized) {
                missileEntityType = EntityType.Builder.create(::MissileEntity, EntityClassification.MISC).setTrackingRange(256).build("${registryName.namespace}:${registryName.path}_missile").setRegistryName("${registryName.namespace}:${registryName.path}_missile") as EntityType<out MissileEntity>
                return missileEntityType
            } else throw UninitializedPropertyAccessException("Tried to create Missile EntityType before EntityType!")
        } else throw UninitializedPropertyAccessException("Tried to create Missile EntityType before Missile Block!")
    }

    override fun getRegistryName(): ResourceLocation? {
        return registryName
    }

    override fun getRegistryType(): Class<ExplosiveType> {
        return this.javaClass
    }

    override fun setRegistryName(registryName: ResourceLocation?): ExplosiveType {
        this.registryName = registryName!!
        return this
    }
}