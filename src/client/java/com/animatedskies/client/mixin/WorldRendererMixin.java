package com.animatedskies.client.mixin;

import com.animatedskies.client.renderer.SkyMediaRenderer;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.buffers.GpuBufferSlice;


@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(ObjectAllocator allocator,
                          RenderTickCounter tickCounter,
                          boolean renderBlockOutline,
                          Camera camera,
                          Matrix4f positionMatrix,
                          Matrix4f matrix4f,
                          Matrix4f projectionMatrix,
                          GpuBufferSlice fogBuffer,
                          Vector4f fogColor,
                          boolean renderSky,
                          CallbackInfo ci) {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        SkyMediaRenderer.render(camera, tickDelta, immediate);
    }
}