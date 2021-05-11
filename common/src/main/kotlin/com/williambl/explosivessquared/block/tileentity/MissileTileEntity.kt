package com.williambl.explosivessquared.block.tileentity

import com.williambl.explosivessquared.ExplosivesSquared
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.vector.Vector3d

class MissileTileEntity(var target: Vector3d) : TileEntity(ExplosivesSquared.missile.get()) {

    constructor() : this(Vector3d.ZERO)

    override fun write(tag: CompoundNBT): CompoundNBT {
        super.write(tag)
        tag.putDouble("TargetX", target.x)
        tag.putDouble("TargetY", target.y)
        tag.putDouble("TargetZ", target.z)
        return tag
    }

    override fun read(state: BlockState, nbt: CompoundNBT?) {
        super.read(state, nbt)
        if (nbt != null) {
            target = Vector3d(nbt.getDouble("TargetX"), nbt.getDouble("TargetY"), nbt.getDouble("TargetZ"))
        }
    }
}
