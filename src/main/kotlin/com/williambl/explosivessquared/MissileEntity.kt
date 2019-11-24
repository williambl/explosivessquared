package com.williambl.explosivessquared

import net.minecraft.entity.*
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.particles.ParticleTypes
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks


open class MissileEntity(type: EntityType<out MissileEntity>, worldIn: World, var target: Vec3d) : ExplosiveEntity(type, worldIn) {

    constructor(type: EntityType<out MissileEntity>, world: World) : this(type, world, Vec3d.ZERO)

    companion object {
        private val FUSE = EntityDataManager.createKey(MissileEntity::class.java, DataSerializers.VARINT)
        private val HORIZONTAL_SPEED = EntityDataManager.createKey(MissileEntity::class.java, DataSerializers.FLOAT)
    }

    private var horizontalSpeed = 20f

    private val horizontalSpeedDataManager: Float
        get() = this.dataManager.get(HORIZONTAL_SPEED)

    constructor(type: EntityType<out MissileEntity>, worldIn: World, x: Double, y: Double, z: Double, igniter: LivingEntity?, target: Vec3d) : this(type, worldIn) {
        this.setPosition(x, y, z)
        this.motion = getMotionToReachTarget(target)
        this.setFuse(80)
        this.prevPosX = x
        this.prevPosY = y
        this.prevPosZ = z
        this.tntPlacedBy = igniter
        this.target = target
    }

    override fun registerData() {
        super.registerData()
        this.dataManager.register(HORIZONTAL_SPEED, 20f)
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

        setFuse(getFuse() - 1)
        if (this.getFuse() <= 0 && positionVec.distanceTo(target) < 5.0) {
            this.remove()
            if (!this.world.isRemote) {
                ExplosivesSquared.entityTypesToBlocks[ExplosivesSquared.missileEntityTypesToEntityTypes[type]]?.entityExplode?.invoke(this)
            }
        } else {
            this.handleWaterMovement()
            this.world.addParticle(ParticleTypes.SMOKE, this.posX, this.posY + 0.5, this.posZ, 0.0, 0.0, 0.0)
        }

    }

    override fun writeAdditional(compound: CompoundNBT) {
        super.writeAdditional(compound)
        compound.putDouble("TargetX", target.x)
        compound.putDouble("TargetY", target.y)
        compound.putDouble("TargetZ", target.z)
    }

    override fun readAdditional(compound: CompoundNBT) {
        super.readAdditional(compound)
        target = Vec3d(compound.getDouble("TargetX"), compound.getDouble("TargetY"), compound.getDouble("TargetZ"))
    }

    override fun getEyeHeight(poseIn: Pose, sizeIn: EntitySize): Float {
        return 0.0f
    }

    override fun notifyDataManagerChange(key: DataParameter<*>) {
        super.notifyDataManagerChange(key)
        if (HORIZONTAL_SPEED == key) {
            this.horizontalSpeed = this.horizontalSpeedDataManager
        }
    }

    fun getHorizontalSpeedAsDouble(): Double {
        return this.horizontalSpeed.toDouble()
    }

    fun getHorizontalMotion(target: Vec3d): Vec3d {
        return target.subtract(positionVec).mul(getHorizontalSpeedAsDouble(), getHorizontalSpeedAsDouble(), getHorizontalSpeedAsDouble())
    }

    fun getTimeToTarget(target: Vec3d): Double {
        val horizontalPosition = positionVec.mul(1.0, 0.0, 1.0)
        val horizontalTarget = target.mul(1.0, 0.0, 1.0)
        return horizontalPosition.distanceTo(horizontalTarget) / getHorizontalSpeedAsDouble()
    }

    fun getVerticalMotion(time: Double, target: Vec3d): Vec3d {
        if (time == 0.0)
            return Vec3d.ZERO
        val verticalDistance = target.y - positionVec.y
        return Vec3d(0.0, (verticalDistance / time) - ((-0.04 * time) / 2), 0.0)
    }

    fun getMotionToReachTarget(target: Vec3d): Vec3d {
        return getHorizontalMotion(target).add(getVerticalMotion(getTimeToTarget(target), target))
    }

    override fun createSpawnPacket(): IPacket<*> {
        return NetworkHooks.getEntitySpawningPacket(this)
    }

}
