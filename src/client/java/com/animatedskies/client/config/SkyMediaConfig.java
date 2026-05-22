package com.animatedskies.client.config;

import com.animatedskies.client.enum_and_class.DesignatedDimension;
import com.animatedskies.client.enum_and_class.TimeVisibility;

public class SkyMediaConfig {

    public boolean enabled = false;

    public TimeVisibility timeVisibility = TimeVisibility.BOTH;

    public DesignatedDimension designatedDimension = DesignatedDimension.OVERWORLD;
    
    public float quadXpos = -135f;
    public float quadYpos = 45f;

    public float quadXscale = 1f;
    public float quadYscale = 1f;

    public float quadXrot = 0f;
    public float quadYrot = 0f;
    public float quadZrot = 0f;
}