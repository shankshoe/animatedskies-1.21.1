package com.animatedskies.client.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogDisableMixin {

    @Inject(
        method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disableFog(
            Camera camera,
            int viewDistance,
            RenderTickCounter tickCounter,
            float tickDelta,
            ClientWorld world,
            CallbackInfoReturnable<Vector4f> cir
    ) {
        cir.setReturnValue(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
    }
}