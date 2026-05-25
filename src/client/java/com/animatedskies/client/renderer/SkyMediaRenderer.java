package com.animatedskies.client.renderer;

import org.joml.Matrix4f;

import com.animatedskies.client.enum_and_class.MediaType;
import com.animatedskies.client.enum_and_class.SkyMedia;
import com.animatedskies.client.utils.SkyMediaManager;
import com.animatedskies.client.utils.SkyMediaTextureLoader;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class SkyMediaRenderer {

private static RenderLayer SKY_MEDIA_LAYER(Identifier texture) {
    return RenderLayer.of(
        "sky_media",
        256,
        RenderPipelines.POSITION_TEX_COLOR_END_SKY,
        MultiPhaseParameters.builder()

            .texture(
                new RenderPhase.Texture(
                    texture,
                    false
                )
            )


            .build(false)
    );
}

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static void render(Camera camera, float tickDelta, VertexConsumerProvider.Immediate immediate) {
        if (CLIENT.world == null || CLIENT.player == null) return;
        if (SkyMediaManager.getActiveMedia().isEmpty()) return;

        MatrixStack matrices = new MatrixStack();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        for (SkyMedia media : SkyMediaManager.getActiveMedia()) {
            if (!shouldRenderInCurrentDimension(media)) continue;
            if (!shouldRenderAtCurrentTime(media)) continue;
            renderMedia(matrices, media, tickDelta, immediate);
        }
    }

    private static void renderMedia(MatrixStack matrices, SkyMedia media, float tickDelta, VertexConsumerProvider.Immediate immediate) {

        Identifier texture = SkyMediaTextureLoader.getTexture(media);

        if (texture == null) {return;}
        
        var textureObject = CLIENT.getTextureManager().getTexture(texture);

        var gpuTexture =
        CLIENT.getTextureManager()
                .getTexture(texture)
                .getGlTexture();

                
                matrices.push();


        
        int renderDistanceChunks = CLIENT.options.getViewDistance().getValue();
        
        float renderDistanceBlocks = renderDistanceChunks * 16.0f;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(media.QuadXpos));

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(media.QuadYpos));
                
        matrices.translate(0, 0, -renderDistanceBlocks);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(media.QuadXrot));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(media.QuadYrot));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(media.QuadZrot));
        

        /*
         * Quad size
         */
        float width = media.getFrameWidth() * media.QuadXscale * 0.05f;

        float height = media.getFrameHeight() * media.QuadYscale * 0.05f;

        matrices.scale(width, height, 1.0f);

        /*
        * Animation UVs
        */
        float u0 = 0f;
        float u1 = 1f;

        float v0 = 0f;
        float v1 = 1f;

        if (media.getMediaType() == MediaType.GIF) {

                long worldTime = CLIENT.world.getTime();

                int frame = (int) ((worldTime / media.getTicksPerFrame()) % media.getFrameCount());

                /*
                * Vertical spritesheet:
                * each frame is stacked top-to-bottom
                */
               float frameHeightUV = 1.0f / media.getFrameCount();
               
                v0 = frame * frameHeightUV;
                v1 = v0 + frameHeightUV;
            }

        if (media.getMediaType() == MediaType.VIDEO) {

                double time = Util.getMeasuringTimeMs() / 50.0;

                int frame = Math.floorMod((int)(time / media.getTicksPerFrame()), media.getFrameCount());
                
                final int columns = 10;
                final int rows = 10;

                final float tileSize = 256f;

                int col = frame % columns;
                int row = frame / columns;

                row = (rows - 1) - row;

                float cellWidthUV = 1.0f / columns;

                float cellHeightUV = 1.0f / rows;

                
                float visibleWidthRatio = media.getFrameWidth() / tileSize;

                float visibleHeightRatio = media.getFrameHeight() / tileSize;

                float visibleWidthUV = cellWidthUV * visibleWidthRatio;

                float visibleHeightUV = cellHeightUV * visibleHeightRatio;

                float padX = (cellWidthUV - visibleWidthUV) / 2f;

                float padY = (cellHeightUV - visibleHeightUV) / 2f;

                u0 = (col * cellWidthUV) + padX;

                u1 = u0 + visibleWidthUV;

                
                float rowTop = 1.0f - (row * cellHeightUV);

                v1 = rowTop - padY;
                
                v0 = v1 - visibleHeightUV;
                }

        //RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    VertexConsumer buffer =
        immediate.getBuffer(
                SKY_MEDIA_LAYER(texture)
        );

MatrixStack.Entry entry = matrices.peek();

int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;

buffer.vertex(entry, -0.5f, -0.5f, 0f)
        .color(255,255,255,255)
        .texture(u0, v1);
        //.overlay(OverlayTexture.DEFAULT_UV)       
        //.light(light) 
        //.normal(entry, 0f, 0f, 1f);              
        

buffer.vertex(entry, 0.5f, -0.5f, 0f)
        .color(255,255,255,255)
        .texture(u1, v1);
        //.overlay(OverlayTexture.DEFAULT_UV)       
        //.light(light) 
        //.normal(entry, 0f, 0f, 1f);              
        

buffer.vertex(entry, 0.5f, 0.5f, 0f)
        .color(255,255,255,255)
        .texture(u1, v0);
        //.overlay(OverlayTexture.DEFAULT_UV)       
        //.light(light) 
        //.normal(entry, 0f, 0f, 1f);              
        

buffer.vertex(entry, -0.5f, 0.5f, 0f)
        .color(255,255,255,255)
        .texture(u0, v0);
        //.overlay(OverlayTexture.DEFAULT_UV)       
        //.light(light) 
        //.normal(entry, 0f, 0f, 1f);              
        

immediate.draw();
        
        matrices.pop();
    }

    private static boolean shouldRenderInCurrentDimension(SkyMedia media) {

        RegistryKey<World> current = CLIENT.world.getRegistryKey();

        return switch (media.designatedDimension) {

            case OVERWORLD -> current == World.OVERWORLD;

            case END -> current == World.END;

            default -> true;
        };
    }

    private static boolean shouldRenderAtCurrentTime(SkyMedia media) {

        if (CLIENT.world == null) {
            return false;
        }

        long time = CLIENT.world.getTime() % 24000;

        return switch (media.timeVisibility) {

            case DAY -> time >= 0 && time < 13000;
            
            case NIGHT -> time >= 13000 && time < 24000;

            case BOTH -> true;
        };
    }
}