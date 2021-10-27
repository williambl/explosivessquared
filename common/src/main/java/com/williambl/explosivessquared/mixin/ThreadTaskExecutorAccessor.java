package com.williambl.explosivessquared.mixin;

import net.minecraft.util.concurrent.ThreadTaskExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadTaskExecutor.class)
public interface ThreadTaskExecutorAccessor {
    @Invoker("deferTask")
    CompletableFuture<Void> callDeferTask(Runnable taskIn);
}
