package com.williambl.explosivessquared

import net.minecraft.block.BlockState
import net.minecraft.block.GrassBlock
import net.minecraft.block.IGrowable
import net.minecraft.block.VineBlock
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.IPlantable

fun BlockState.isVegetation(): Boolean {
    if (this.block.tags.contains(ResourceLocation("minecraft:leaves")))
        return true
    if (this.block is IPlantable)
        return true
    if (this.block is IGrowable)
        return true
    if (this.block is VineBlock)
        return true
    return false
}

fun BlockState.isGrass(): Boolean {
    return this.block is GrassBlock
}