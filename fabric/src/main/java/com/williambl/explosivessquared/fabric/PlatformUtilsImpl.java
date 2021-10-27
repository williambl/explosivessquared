package com.williambl.explosivessquared.fabric;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.mixin.blockrenderlayer.MixinBlockRenderLayer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;

public final class PlatformUtilsImpl {
    public static IPacket<?> createSpawnPacket(Entity entity) {
        PacketBuffer buf = PacketByteBufs.create();

        buf.writeVarInt(Registry.ENTITY_TYPE.getId(entity.getType()));
        buf.writeInt(entity.getEntityId());
        buf.writeUniqueId(entity.getUniqueID());
        buf.writeDouble(entity.getPosX());
        buf.writeDouble(entity.getPosY());
        buf.writeDouble(entity.getPosZ());
        buf.writeFloat(entity.getPitchYaw().y);
        buf.writeFloat(entity.getPitchYaw().x);
        buf.writeDouble(entity.getMotion().x);
        buf.writeDouble(entity.getMotion().y);
        buf.writeDouble(entity.getMotion().z);
        return ServerPlayNetworking.createS2CPacket(new ResourceLocation("explosivessquared:spawn"), buf);
    }
}
