package com.williambl.explosivessquared

import net.minecraft.entity.*
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.cos
import kotlin.math.sin


open class GlassingRayBeamEntity(type: EntityType<out GlassingRayBeamEntity>, worldIn: World) : Entity(type, worldIn) {

    companion object {
        private val TIME_LEFT = EntityDataManager.createKey(GlassingRayBeamEntity::class.java, DataSerializers.VARINT)
    }

    private var timeLeft = 80

    val timeLeftDataManager: Int
        get() = this.dataManager.get(TIME_LEFT)

    init {
        this.preventEntitySpawning = true
        this.ignoreFrustumCheck = true
    }

    constructor(type: EntityType<out GlassingRayBeamEntity>, worldIn: World, x: Double, y: Double, z: Double) : this(type, worldIn) {
        this.setPosition(x, y, z)
        val d0 = worldIn.rand.nextDouble() * (Math.PI.toFloat() * 2f).toDouble()
        this.setMotion(-sin(d0) * 0.02, 0.2f.toDouble(), -cos(d0) * 0.02)
        this.setTimeLeft(80)
        this.prevPosX = x
        this.prevPosY = y
        this.prevPosZ = z
    }

    override fun registerData() {
        this.dataManager.register(TIME_LEFT, 80)
    }

    override fun canTriggerWalking(): Boolean {
        return false
    }

    override fun tick() {
        this.prevPosX = this.posX
        this.prevPosY = this.posY
        this.prevPosZ = this.posZ

        --this.timeLeft
        if (this.timeLeft <= 0) {
            this.remove()
        }
    }

    override fun writeAdditional(compound: CompoundNBT) {
        compound.putShort("Fuse", this.getTimeLeft().toShort())
    }

    override fun readAdditional(compound: CompoundNBT) {
        this.setTimeLeft(compound.getShort("Fuse").toInt())
    }

    override fun getEyeHeight(poseIn: Pose, sizeIn: EntitySize): Float {
        return 0.0f
    }

    fun setTimeLeft(timeLeftIn: Int) {
        this.dataManager.set(TIME_LEFT, timeLeftIn)
        this.timeLeft = timeLeftIn
    }

    override fun notifyDataManagerChange(key: DataParameter<*>) {
        if (TIME_LEFT == key) {
            this.timeLeft = this.timeLeftDataManager
        }

    }

    fun getTimeLeft(): Int {
        return this.timeLeft
    }

    override fun createSpawnPacket(): IPacket<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }

}
