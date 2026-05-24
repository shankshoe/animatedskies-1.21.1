package com.animatedskies.client.gui;

import com.animatedskies.client.config.SkyMediaConfigManager;
import com.animatedskies.client.enum_and_class.SkyMedia;
import com.animatedskies.client.utils.SkyMediaManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SkyWidgetsScreen extends Screen {

    private final Screen parent;

    public SkyWidgetsScreen(Screen parent) {
        super(Text.literal("Sky Widgets"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        this.clearChildren();

        int y = 40;

        for (SkyMedia media : SkyMediaManager.getAllMedia()) {

            int finalY = y;

            addDrawableChild(
                    ButtonWidget.builder(
                            Text.literal(media.getName()),
                            button -> client.setScreen(new MediaConfigScreen(this, media)))
                    .dimensions(width / 2 - 110, y, 150, 20)
                    .build()
            );

            boolean enabled = SkyMediaManager.isActive(media.getName());

            addDrawableChild(
                    ButtonWidget.builder(
                            Text.literal(enabled ? "Disable" : "Enable"),
                            button -> {
                                boolean isEnabled = SkyMediaManager.isActive(media.getName());

                                if (isEnabled) {

                                    SkyMediaManager.deactivate(media.getName());

                                } else {

                                    SkyMediaManager.activate(media.getName());
                                }

                                SkyMediaConfigManager.saveConfig(media, !isEnabled);

                                /*
                                * Refresh screen text
                                */
                                client.setScreen(new SkyWidgetsScreen(parent));
                            }
                    )
                    .dimensions(
                            width / 2 + 50,
                            finalY,
                            70,
                            20
                    )
                    .build()
            );

            y += 25;
        }

        addDrawableChild(
                ButtonWidget.builder(Text.literal("Back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 100, height - 30, 200, 20)
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        context.drawCenteredTextWithShadow(
                textRenderer,
                title,
                width / 2,
                15,
                0xFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }
}