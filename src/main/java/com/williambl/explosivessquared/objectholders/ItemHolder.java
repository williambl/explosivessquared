package com.williambl.explosivessquared.objectholders;

import com.williambl.explosivessquared.ExplosivesSquared;
import com.williambl.explosivessquared.item.TargeterItem;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExplosivesSquared.modid)
public class ItemHolder {

    @ObjectHolder("targeter")
    public static TargeterItem targeter;

}