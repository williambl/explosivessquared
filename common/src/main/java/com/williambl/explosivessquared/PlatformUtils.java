package com.williambl.explosivessquared;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;

import java.util.function.Function;

public final class PlatformUtils {
    @ExpectPlatform
    public static IPacket<?> createSpawnPacket(Entity entity) {
        throw new AssertionError();
    }
}
