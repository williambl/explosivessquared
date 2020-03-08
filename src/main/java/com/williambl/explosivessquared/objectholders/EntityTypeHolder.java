package com.williambl.explosivessquared.objectholders;

import com.williambl.explosivessquared.ExplosivesSquared;
import com.williambl.explosivessquared.entity.GlassingRayBeamEntity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExplosivesSquared.modid)
public class EntityTypeHolder {

    @ObjectHolder("glassing_ray_beam")
    public static EntityType<GlassingRayBeamEntity> glassingRayBeam;

}