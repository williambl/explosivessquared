package com.williambl.explosivessquared.fabric;

import com.williambl.explosivessquared.ExplosivesSquaredClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class ExplosivesSquaredClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExplosivesSquaredClient.INSTANCE.initClient();
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation("explosivessquared:spawn"), (client, handler, buf, responseSender) -> {
            if (client.world != null) {
                client.enqueue(() -> {
                    Entity entity = Registry.ENTITY_TYPE.getByValue(buf.readVarInt()).create(client.world);
                    if (entity != null) {
                        client.world.addEntity(buf.readInt(), entity);
                        entity.setUniqueId(buf.readUniqueId());
                        entity.setPositionAndRotation(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat());
                    }
                });
            }
        });
    }
}
