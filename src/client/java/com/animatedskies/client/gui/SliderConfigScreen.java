package com.animatedskies.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderConfigScreen extends Screen {

    private static final float STEP = 0.05f;

    private final Screen parent;
    private final String label;

    private final Supplier<Float> getter;
    private final Consumer<Float> setter;

    private final float min;
    private final float max;
    private final float defaultValue;

    private final Runnable onCloseSave;

    private ConfigSlider slider;

    public SliderConfigScreen(
            Screen parent,
            String label,
            Supplier<Float> getter,
            Consumer<Float> setter,
            float min,
            float max,
            float defaultValue,
            Runnable onCloseSave
    ) {
        super(Text.literal(label));

        this.parent = parent;
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.onCloseSave = onCloseSave;
    }

    @Override
    protected void init() {

        float current = getter.get();

        double normalized =
                (current - min) / (max - min);

        slider = new ConfigSlider(
                width / 2 - 155,
                height - 52,
                310,
                20,
                normalized
        );

        addDrawableChild(slider);

        // Reset button
        addDrawableChild(
                ButtonWidget.builder(
                                Text.literal("Reset"),
                                b -> resetValue()
                        )
                        .dimensions(
                                width / 2 - 155,
                                height - 26,
                                75,
                                20
                        )
                        .build()
        );

        // Back button
        addDrawableChild(
                ButtonWidget.builder(
                                Text.literal("Back"),
                                b -> client.setScreen(parent)
                        )
                        .dimensions(
                                width / 2 - 75,
                                height - 26,
                                150,
                                20
                        )
                        .build()
        );
    }

    private void resetValue() {
        setter.accept(defaultValue);
        slider.syncToValue(defaultValue);
    }

    private String formatValue(float value) {
        return String.format("%.2f", value);
    }

    @Override
    public void renderBackground(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // transparent background
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        onCloseSave.run();
        client.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {

        float current = getter.get();

        current += verticalAmount > 0
                ? STEP
                : -STEP;

        // snap to 0.05
        current =
                Math.round(current / STEP) * STEP;

        // clamp
        current =
                Math.max(min,
                Math.min(max, current));

        setter.accept(current);

        // visually move slider
        slider.syncToValue(current);

        return true;
    }

    /**
     * Inner slider class
     */
    private class ConfigSlider extends SliderWidget {

        public ConfigSlider(
                int x,
                int y,
                int width,
                int height,
                double value
        ) {
            super(
                    x,
                    y,
                    width,
                    height,
                    Text.empty(),
                    value
            );

            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(
                    label + ": "
                    + formatValue(getter.get())
            ));
        }

        @Override
        protected void applyValue() {

            float val =
                    min + (float) this.value
                    * (max - min);

            // snap to 0.05 increments
            val =
                    Math.round(val / STEP)
                    * STEP;

            // clamp
            val =
                    Math.max(min,
                    Math.min(max, val));

            setter.accept(val);

            // update knob position
            this.value =
                    (val - min)
                    / (max - min);

            updateMessage();
        }

        public void syncToValue(float val) {

            this.value =
                    (val - min)
                    / (max - min);

            updateMessage();
        }
    }
}