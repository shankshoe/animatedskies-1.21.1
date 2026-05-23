package com.animatedskies.client.utils;

import com.animatedskies.client.enum_and_class.SkyMedia;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;

public class SkyMediaTextureLoader {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static Identifier getTexture(SkyMedia media) {

        if (media.getTextureId() != null) {
            return media.getTextureId();
        }

        try {

            Path path =
                    Path.of(
                            media.getProcessedPath()
                    );

            NativeImage image =
                NativeImage.read(
                        Files.newInputStream(path)
                );

        NativeImageBackedTexture texture =
                 new NativeImageBackedTexture(
                () -> "animatedskies_" + media.getName(),
                image);

        Identifier id = Identifier.of(
                "animatedskies",
                "dynamic/" + media.getName().toLowerCase()
        );

        CLIENT.getTextureManager().registerTexture(id, texture);

        media.setTextureId(id);

        System.out.println(
                "[AnimatedSkies] Loaded texture: "
                        + media.getName()
        );

        return id;

        } catch (Exception e) {

            System.err.println(
                    "[AnimatedSkies] Failed loading "
                            + media.getProcessedPath()
            );

            e.printStackTrace();

            return null;
        }
    }
}