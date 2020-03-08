package com.williambl.explosivessquared.objectholders;

import com.williambl.explosivessquared.ExplosivesSquared;
import com.williambl.explosivessquared.block.tileentity.MissileTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExplosivesSquared.modid)
public class TileEntityTypeHolder {

    @ObjectHolder("missile")
    public static TileEntityType<MissileTileEntity> missile;

}