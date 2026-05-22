package com.animatedskies.client.gui;

import com.animatedskies.client.config.SkyMediaConfigManager;
import com.animatedskies.client.enum_and_class.*;
import com.animatedskies.client.utils.SkyMediaManager;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MediaConfigScreen extends Screen {

    private final Screen parent;
    private final SkyMedia media;

    private boolean enabled;
    private ButtonWidget enableButton;
    
    public MediaConfigScreen(Screen parent, SkyMedia media) {
        super(Text.literal(media.getName()));
        this.parent = parent;
        this.media = media;
        this.enabled = SkyMediaManager.isActive(media.getName());
    }

    @Override
    protected void init() {

        int y = 40;

        enableButton = ButtonWidget.builder(
            Text.literal(
                    enabled ? "Disable" : "Enable"
            ),
            b -> {

                enabled = !enabled;

                SkyMediaConfigManager.saveConfig(media, enabled);

                // update button text immediately
                enableButton.setMessage(Text.literal(enabled ? "Disable" : "Enable"));
            }
    )
    .dimensions(
            width / 2 - 100,
            y,
            200,
            20
    )
    .build();

    addDrawableChild(enableButton);

        y += 24;

        addDrawableChild(button(
                "Time: " + media.timeVisibility,
                y,
                b -> {

                    TimeVisibility[] vals =
                            TimeVisibility.values();

                    int next =
                            (media.timeVisibility.ordinal() + 1)
                                    % vals.length;

                    media.timeVisibility =
                            vals[next];

                    SkyMediaConfigManager.saveConfig(
                            media,
                            enabled
                    );

                    client.setScreen(
                            new MediaConfigScreen(
                                    parent,
                                    media
                            )
                    );
                }
        ));

        y += 24;

        addDrawableChild(button(
                "Dimension: " +
                        media.designatedDimension,
                y,
                b -> {

                    DesignatedDimension[] vals = DesignatedDimension.values();

                    int next = (media.designatedDimension.ordinal() + 1) % vals.length;

                    media.designatedDimension = vals[next];

                    SkyMediaConfigManager.saveConfig(media, enabled);

                    client.setScreen(new MediaConfigScreen(parent, media)
                    );
                }
        ));

        
        addSliderButton("X Pos", y += 24,
        () -> media.QuadXpos,
        v -> media.QuadXpos = v,
        -360f, 360f, -135f);

        addSliderButton("Y Pos", y += 24,
                () -> media.QuadYpos,
                v -> media.QuadYpos = v,
                -360f, 360f,
                45f);

        addSliderButton("X Rot", y += 24,
                () -> media.QuadXrot,
                v -> media.QuadXrot = v,
                -360f, 360f,
                0f);

        addSliderButton("Y Rot", y += 24,
                () -> media.QuadYrot,
                v -> media.QuadYrot = v,
                -360f, 360f,
                0f);

        addSliderButton("Z Rot", y += 24,
                () -> media.QuadZrot,
                v -> media.QuadZrot = v,
                -360f, 360f,
                0f);

        addSliderButton("X Scale", y += 24,
                () -> media.QuadXscale,
                v -> media.QuadXscale = v,
                0.01f, 24f,
                1f);

        addSliderButton("Y Scale", y += 24,
                () -> media.QuadYscale,
                v -> media.QuadYscale = v,
                0.01f, 24f,
                1f);

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Back"),
                        b -> {

                            SkyMediaConfigManager.saveConfig(media, enabled);

                            client.setScreen(parent);
                        }
                )
                .dimensions(
                        width / 2 - 100,
                        height - 28,
                        200,
                        20
                )
                .build()
        );
    }

    private ButtonWidget button(String text, int y, ButtonWidget.PressAction action) {
        return ButtonWidget.builder(Text.literal(text), action)
                .dimensions(width / 2 - 100, y, 200, 20)
                .build();
    }

    private void addSliderButton(
            String label,
            int y,
            java.util.function.Supplier<Float> getter,
            java.util.function.Consumer<Float> setter,
            float min,
            float max,
            float defaultValue
    ) {

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal(label),
                        b -> client.setScreen(
                                new SliderConfigScreen(
                                        this,
                                        label,
                                        getter,
                                        setter,
                                        min,
                                        max,
                                        defaultValue,
                                        () -> SkyMediaConfigManager.saveConfig(
                                                media,
                                                enabled
                                        )
                                )
                        )
                )
                .dimensions(
                        width / 2 - 100,
                        y,
                        200,
                        20
                )
                .build()
        );
    }
}