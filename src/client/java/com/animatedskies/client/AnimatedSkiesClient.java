package com.animatedskies.client;

import com.animatedskies.client.config.SkyMediaConfigManager;
import com.animatedskies.client.enum_and_class.SkyMedia;
import com.animatedskies.client.gui.SkyWidgetsScreen;
import com.animatedskies.client.renderer.SkyMediaRenderer;
import com.animatedskies.client.utils.SkyMediaManager;
import com.animatedskies.client.utils.SkyMediaProcessor;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AnimatedSkiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        //SkyMediaProcessor.processAll();
        
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context ->
                SkyMediaRenderer.render(
                        context.matrixStack(),
                        context.tickCounter().getTickDelta(true)
                )
        );
        
        // Reload on F3+T / resource pack reload
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new IdentifiableResourceReloadListener() {

                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of("animatedskies", "reload_listener");
                    }

                    @Override
                    public CompletableFuture<Void> reload(
                            ResourceReloader.Synchronizer synchronizer,
                            ResourceManager manager,
                            Profiler prepareProfiler,
                            Profiler applyProfiler,
                            Executor prepareExecutor,
                            Executor applyExecutor
                    ) {

                        return CompletableFuture
                                .runAsync(() -> {
                                    System.out.println("[AnimatedSkies] Reloading sky media...");
                                    reloadAnimatedSkies();
                                }, applyExecutor)
                                .thenCompose(synchronizer::whenPrepared);
                    }
                });





                

        ScreenEvents.AFTER_INIT.register(
                (client, screen, scaledWidth, scaledHeight) -> {

                    if (!(screen instanceof OptionsScreen)) {
                        return;
                    }

                    /*
                     * Bottom-right button slot
                     */
                    int x = screen.width / 2 + 4;
                    int y = screen.height / 6 + 155;

                    Screens.getButtons(screen).add(
                            ButtonWidget.builder(
                                    Text.literal("Sky Widgets"),
                                    button -> client.setScreen(
                                            new SkyWidgetsScreen(screen)
                                    )
                            )
                            .dimensions(
                                    x,
                                    y,
                                    150,
                                    20
                            )
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