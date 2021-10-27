package com.williambl.explosivessquared.forge;

import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.registry.entity.EntityRenderers;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.function.Function;

public final class PlatformUtilsImpl {
    public static IPacket<?> createSpawnPacket(Entity entity) {
        return NetworkHooks.getEntitySpawningPacket(entity);
    }
}
