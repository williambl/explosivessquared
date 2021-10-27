package com.williambl.explosivessquared.fabric;

import com.williambl.explosivessquared.ExplosivesSquaredClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class ExplosivesSquaredClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExplosivesSquaredClient.INSTANCE.initClient();
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation("explosivessquared:spawn"), (client, handler, buf, responseSender) -> {
            if (client.world != null) {
                buf.retain();
                client.enqueue(() -> {
                    Entity entity = Registry.ENTITY_TYPE.getByValue(buf.readVarInt()).create(client.world);
                    if (entity != null) {
                        int id = buf.readInt();
                        entity.setEntityId(id);
                        entity.setUniqueId(buf.readUniqueId());
                        Vector3d pos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
                        entity.setPacketCoordinates(pos.x, pos.y, pos.z);
                        entity.setPositionAndRotation(pos.x, pos.y, pos.z, buf.readFloat(), buf.readFloat());
                        entity.setMotion(buf.readDouble(), buf.readDouble(), buf.readDouble());
                        client.world.addEntity(id, entity);
                    }
                    buf.release();
                });
            }
        });
    }
}
