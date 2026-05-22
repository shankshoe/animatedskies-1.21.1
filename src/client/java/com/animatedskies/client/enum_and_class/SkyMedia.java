package com.animatedskies.client.enum_and_class;

import net.minecraft.util.Identifier;

public class SkyMedia {

    private final String name;
    private final MediaType mediaType;
    public TimeVisibility timeVisibility;

    private final String processedPath;

    private final int frameWidth;
    private final int frameHeight;
    private final int frameCount;
    private final int ticksPerFrame;

    private Identifier textureId;

    public DesignatedDimension designatedDimension = DesignatedDimension.OVERWORLD;

    public float QuadXpos = -135f;
    public float QuadYpos = 45f;

    /*
    starting pos = north
    +X = west
    +Y = up
    */

    public float QuadXscale = 1f;
    public float QuadYscale = 1f;

    public float QuadXrot = 0f;
    public float QuadYrot = 0f;
    public float QuadZrot = 0f;

    public SkyMedia(
            String name,
            MediaType mediaType,
            TimeVisibility timeVisibility,
            String processedPath,
            int frameWidth,
            int frameHeight,
            int frameCount,
            int ticksPerFrame
    ) {
        this.name = name;
        this.mediaType = mediaType;
        this.timeVisibility = timeVisibility;
        this.processedPath = processedPath;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameCount = frameCount;
        this.ticksPerFrame = ticksPerFrame;
    }

    public String getName() {
        return name;
    }

    public String getProcessedPath() {
        return processedPath;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public TimeVisibility getTimeVisibility() {
        return timeVisibility;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getTicksPerFrame() {
        return ticksPerFrame;
    }

    public boolean isAnimated() {
        return frameCount > 1;
    }

    public Identifier getTextureId() {
        return textureId;
    }

    public void setTextureId(Identifier textureId) {
        this.textureId = textureId;
    }

    public void setTimeVisibility(TimeVisibility timeVisibility) {
        this.timeVisibility = timeVisibility;
    }
}