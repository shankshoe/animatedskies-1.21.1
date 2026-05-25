package com.animatedskies.client;

import com.animatedskies.client.config.SkyMediaConfigManager;
import com.animatedskies.client.enum_and_class.SkyMedia;
import com.animatedskies.client.gui.SkyWidgetsScreen;
import com.animatedskies.client.utils.SkyMediaManager;
import com.animatedskies.client.utils.SkyMediaProcessor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AnimatedSkiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Reload on F3+T / resource pack reload
        ResourceLoader.get(ResourceType.CLIENT_RESOURCES)
                .registerReloader(
                        Identifier.of("animatedskies", "reload_listener"),
                        new ResourceReloader() {

                            @Override
                            public CompletableFuture<Void> reload(
                                    Store store,
                                    Executor prepareExecutor,
                                    Synchronizer synchronizer,
                                    Executor applyExecutor
                            ) {

                                return CompletableFuture
                                        .runAsync(() -> {
                                            System.out.println("[AnimatedSkies] Reloading sky media...");
                                            reloadAnimatedSkies();
                                        }, applyExecutor)
                                        .thenCompose(synchronizer::whenPrepared);
                            }
                        }
                );

        ScreenEvents.AFTER_INIT.register(
                (client, screen, scaledWidth, scaledHeight) -> {

                    if (!(screen instanceof OptionsScreen)) {
                        return;
                    }

                    int x = screen.width / 2 + 4;
                    int y = screen.height / 6 + 155;

                    Screens.getButtons(screen).add(
                            ButtonWidget.builder(
                                    Text.literal("Sky Widgets"),
                                    button -> client.setScreen(
                                            new SkyWidgetsScreen(screen)
                                    )
                            )
                            .dimensions(x, y, 150, 20)
                            .build()
                    );
                }
        );
    }

    private void reloadAnimatedSkies() {

        SkyMediaManager.clear();
        SkyMediaProcessor.processAll();
        SkyMediaConfigManager.loadAllConfigs();

        for (SkyMedia media : SkyMediaManager.getAllMedia()) {
            System.out.println("Registered: " + media.getName());
        }
    }
}