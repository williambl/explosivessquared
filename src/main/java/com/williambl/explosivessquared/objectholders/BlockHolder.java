package com.williambl.explosivessquared.objectholders;

import com.williambl.explosivessquared.ExplosivesSquared;
import com.williambl.explosivessquared.block.ExplosiveBlock;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExplosivesSquared.modid)
public class BlockHolder {

    @ObjectHolder("explosive")
    public static ExplosiveBlock explosiveBlock;

}