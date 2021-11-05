package com.williambl.explosivessquared.entity

import com.williambl.explosivessquared.ExplosivesSquared
import com.williambl.explosivessquared.PlatformUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.*
import net.minecraft.crash.CrashReportCategory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MoverType
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.fluid.Fluids
import net.minecraft.item.DirectionalPlaceContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.NBTUtil
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.GameRules
import net.minecraft.world.World
import java.util.*
import kotlin.math.min

class AntigravityBlockEntity(entityType: EntityType<out AntigravityBlockEntity?>?, world: World?) :
    Entity(entityType, world) {

    var origin: BlockPos
        get() = dataManager.get(ORIGIN)
        set(origin) = dataManager.set(ORIGIN, origin)

    var blockState: BlockState
        get() = dataManager.get(BLOCKSTATE).orElse(Blocks.SAND.defaultState)
        private set(value) = dataManager.set(BLOCKSTATE, Optional.of(value))

    var fallTime = 0
    private var shouldDropItem: Boolean = true
    private var dontSetBlock = false
    private var hurtEntities = false
    private var fallHurtMax: Int = 40
    private var fallHurtAmount: Float = 2.0f
    var tileEntityData: CompoundNBT? = null

    constructor(world: World, pos: BlockPos, blockState: BlockState) : this(
        ExplosivesSquared.antigravityBlock.get(),
        world
    ) {
        this.blockState = blockState
        preventEntitySpawning = true
        val posVec = Vector3d.copyCenteredWithVerticalOffset(pos, ((1.0f - this.height) / 2.0f).toDouble())
        setPosition(posVec.x, posVec.y, posVec.z)
        motion = Vector3d(0.0, 1.0, 0.0)
        prevPosX = posVec.x
        prevPosY = posVec.y
        prevPosZ = posVec.z
        origin = pos
        noClip = true
    }

    override fun canBeAttackedWithItem(): Boolean {
        return false
    }

    override fun canTriggerWalking(): Boolean {
        return false
    }

    override fun registerData() {
        dataManager.register(ORIGIN, BlockPos.ZERO)
        dataManager.register(BLOCKSTATE, Optional.empty())
    }

    override fun canBeCollidedWith(): Boolean {
        return !removed
    }

    override fun canCollide(entity: Entity?): Boolean {
        return super.canCollide(entity) && entity !is AntigravityBlockEntity
    }

    override fun hasNoGravity(): Boolean {
        return true
    }

    override fun tick() {
        if (blockState.isAir) {
            this.remove()
        } else {
            val block = blockState.block
            move(MoverType.SELF, motion)
            if (!world.isRemote) {
                val pos = position
                val sqrSpeed = motion.lengthSquared()
                if (sqrSpeed <= 0.01) {
                    val blockState = world.getBlockState(pos)
                    motion = motion.mul(0.7, 0.5, 0.7)
                    if (!blockState.isIn(Blocks.MOVING_PISTON)) {
                        this.remove()
                        if (!dontSetBlock) {
                            val canReplace = blockState.isReplaceable(
                                DirectionalPlaceContext(world, pos, Direction.UP, ItemStack.EMPTY, Direction.DOWN)
                            )
                            if (canReplace) {
                                if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && world.getFluidState(pos).fluid === Fluids.WATER) {
                                    this.blockState = this.blockState.with(BlockStateProperties.WATERLOGGED, true)
                                }
                                if (world.setBlockState(pos, this.blockState, 3)) {
                                    if (tileEntityData != null && block is ITileEntityProvider) {
                                        val tileEntity = world.getTileEntity(pos)
                                        if (tileEntity != null) {
                                            val compoundNBT = tileEntity.write(CompoundNBT())
                                            tileEntityData!!.keySet().forEach { string ->
                                                val iNBT = tileEntityData!![string]
                                                if ("x" != string && "y" != string && "z" != string) {
                                                    compoundNBT.put(string, iNBT!!.copy())
                                                }
                                            }
                                            tileEntity.read(this.blockState, compoundNBT)
                                            tileEntity.markDirty()
                                        }
                                    }
                                } else if (shouldDropItem && world.gameRules.getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                    this.entityDropItem(block)
                                }
                            } else if (shouldDropItem && world.gameRules.getBoolean(GameRules.DO_ENTITY_DROPS)) {
                                this.entityDropItem(block)
                            }
                        }
                    }
                } else if (!world.isRemote) {
                    if (fallTime > 100 && (pos.y < 1 || pos.y > 256) || fallTime > 600) {
                        if (shouldDropItem && world.gameRules.getBoolean(GameRules.DO_ENTITY_DROPS)) {
                            this.entityDropItem(block)
                        }
                        this.remove()
                    }
                }
            }
            motion = motion.scale(0.98)
        }
    }

    override fun onLivingFall(distance: Float, damageMultiplier: Float): Boolean {
        if (hurtEntities) {
            val i = MathHelper.ceil(distance - 1.0f)
            if (i > 0) {
                val list: List<Entity> = world.getEntitiesWithinAABBExcludingEntity(this, boundingBox)
                val bl = blockState.isIn(BlockTags.ANVIL)
                val damageSource = if (bl) DamageSource.ANVIL else DamageSource.FALLING_BLOCK
                list.forEach { entity ->
                    entity.attackEntityFrom(
                        damageSource, min(
                            MathHelper.floor(i.toFloat() * fallHurtAmount),
                            fallHurtMax
                        ).toFloat()
                    )
                }
                if (bl && rand.nextFloat().toDouble() < 0.05 + i.toDouble() * 0.05) {
                    val blockState = AnvilBlock.damage(blockState)
                    if (blockState == null) {
                        dontSetBlock = true
                    } else {
                        this.blockState = blockState
                    }
                }
            }
        }
        return false
    }

    override fun writeAdditional(compound: CompoundNBT) {
        compound.put("BlockState", NBTUtil.writeBlockState(blockState))
        compound.putInt("Time", fallTime)
        compound.putBoolean("DropItem", shouldDropItem)
        compound.putBoolean("HurtEntities", hurtEntities)
        compound.putFloat("FallHurtAmount", fallHurtAmount)
        compound.putInt("FallHurtMax", fallHurtMax)
        if (tileEntityData != null) {
            compound.put("TileEntityData", tileEntityData)
        }
    }

    override fun readAdditional(compound: CompoundNBT) {
        blockState = NBTUtil.readBlockState(compound.getCompound("BlockState"))
        fallTime = compound.getInt("Time")
        if (compound.contains("HurtEntities", 99)) {
            hurtEntities = compound.getBoolean("HurtEntities")
            fallHurtAmount = compound.getFloat("FallHurtAmount")
            fallHurtMax = compound.getInt("FallHurtMax")
        } else if (blockState.isIn(BlockTags.ANVIL)) {
            hurtEntities = true
        }
        if (compound.contains("DropItem", 99)) {
            shouldDropItem = compound.getBoolean("DropItem")
        }
        if (compound.contains("TileEntityData", 10)) {
            tileEntityData = compound.getCompound("TileEntityData")
        }
        if (blockState.isAir) {
            blockState = Blocks.SAND.defaultState
        }
    }

    fun setHurtEntities(hurtEntitiesIn: Boolean) {
        hurtEntities = hurtEntitiesIn
    }

    @Environment(EnvType.CLIENT)
    override fun canRenderOnFire(): Boolean {
        return false
    }

    override fun fillCrashReport(category: CrashReportCategory) {
        super.fillCrashReport(category)
        category.addDetail("Imitating BlockState", blockState.toString() as Any)
    }

    override fun ignoreItemEntityData(): Boolean {
        return true
    }

    override fun createSpawnPacket(): IPacket<*> {
        return PlatformUtils.createSpawnPacket(this)
    }

    companion object {
        private val ORIGIN: DataParameter<BlockPos> = EntityDataManager.createKey(
                FallingBlockEntity::class.java, DataSerializers.BLOCK_POS
            )

        private val BLOCKSTATE: DataParameter<Optional<BlockState>> = EntityDataManager.createKey(
            FallingBlockEntity::class.java, DataSerializers.OPTIONAL_BLOCK_STATE
        )
    }

}
