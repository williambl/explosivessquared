package com.williambl.explosivessquared.block.tileentity

import com.williambl.explosivessquared.objectholders.TileEntityTypeHolder
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.Vec3d

class MissileTileEntity(var target: Vec3d) : TileEntity(TileEntityTypeHolder.missile) {

    constructor() : this(Vec3d.ZERO)

    override fun serializeNBT(): CompoundNBT {
        val tag = super.serializeNBT()
        tag.putDouble("TargetX", target.x)
        tag.putDouble("TargetY", target.y)
        tag.putDouble("TargetZ", target.z)
        return tag
    }

    override fun deserializeNBT(nbt: CompoundNBT?) {
        super.deserializeNBT(nbt)
        if (nbt != null) {
            target = Vec3d(nbt.getDouble("TargetX"), nbt.getDouble("TargetY"), nbt.getDouble("TargetZ"))
        }
    }
}
