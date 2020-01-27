package com.williambl.explosivessquared.objectholders;

import com.williambl.explosivessquared.ExplosivesSquared;
import com.williambl.explosivessquared.GlassingRayBeamEntity;
import com.williambl.explosivessquared.MissileTileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExplosivesSquared.modid)
public class EntityTypeHolder {

    @ObjectHolder("glassing_ray_beam")
    public static EntityType<GlassingRayBeamEntity> glassingRayBeam;

}