package com.williambl.explosivessquared.entity

import com.williambl.explosivessquared.ExplosivesSquared
import net.minecraft.entity.*
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.particles.ParticleTypes
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.cos
import kotlin.math.sin


open class ExplosiveEntity(type: EntityType<out ExplosiveEntity>, worldIn: World) : Entity(type, worldIn) {

    companion object {
        private val FUSE = EntityDataManager.createKey(ExplosiveEntity::class.java, DataSerializers.VARINT)
    }

    private var fuse = 80
    var tntPlacedBy: LivingEntity? = null

    val fuseDataManager: Int
        get() = this.dataManager.get(FUSE)

    init {
        this.preventEntitySpawning = true
        fuse = ExplosivesSquared.explosiveMap[type.registryName!!.path]?.fuse ?: 80
    }

    constructor(type: EntityType<out ExplosiveEntity>, worldIn: World, x: Double, y: Double, z: Double, igniter: LivingEntity?) : this(type, worldIn) {
        this.setPosition(x, y, z)
        val d0 = worldIn.rand.nextDouble() * (Math.PI.toFloat() * 2f).toDouble()
        this.setMotion(-sin(d0) * 0.02, 0.2f.toDouble(), -cos(d0) * 0.02)
        this.setFuse(80)
        this.prevPosX = x
        this.prevPosY = y
        this.prevPosZ = z
        this.tntPlacedBy = igniter
    }

    override fun registerData() {
        this.dataManager.register(FUSE, 80)
    }

    override fun canTriggerWalking(): Boolean {
        return false
    }

    override fun canBeCollidedWith(): Boolean {
        return !this.removed
    }

    override fun tick() {
        this.prevPosX = this.posX
        this.prevPosY = this.posY
        this.prevPosZ = this.posZ
        if (!this.hasNoGravity()) {
            this.motion = this.motion.add(0.0, -0.04, 0.0)
        }

        this.move(MoverType.SELF, this.motion)
        this.motion = this.motion.scale(0.98)
        if (this.onGround) {
            this.motion = this.motion.mul(0.7, -0.5, 0.7)
        }

        --this.fuse
        if (this.fuse <= 0) {
            this.remove()
            if (!this.world.isRemote) {
                ExplosivesSquared.explosiveMap[type.registryName!!.path]?.explodeFunction?.invoke(this)
            }
        } else {
            this.handleWaterMovement()
            this.world.addParticle(ParticleTypes.SMOKE, this.posX, this.posY + 0.5, this.posZ, 0.0, 0.0, 0.0)
        }

    }

    override fun writeAdditional(compound: CompoundNBT) {
        compound.putShort("Fuse", this.getFuse().toShort())
    }

    override fun readAdditional(compound: CompoundNBT) {
        this.setFuse(compound.getShort("Fuse").toInt())
    }

    override fun getEyeHeight(poseIn: Pose, sizeIn: EntitySize): Float {
        return 0.0f
    }

    fun setFuse(fuseIn: Int) {
        this.dataManager.set(FUSE, fuseIn)
        this.fuse = fuseIn
    }

    override fun notifyDataManagerChange(key: DataParameter<*>) {
        if (FUSE == key) {
            this.fuse = this.fuseDataManager
        }

    }

    fun getFuse(): Int {
        return this.fuse
    }

    override fun createSpawnPacket(): IPacket<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }

}
