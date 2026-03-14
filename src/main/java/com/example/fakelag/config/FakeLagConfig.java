package com.example.fakelag.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "fakelag")
public class FakeLagConfig implements ConfigData {

    public boolean enabled = false;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Mode mode = Mode.PULSE;

    public enum Mode { DELAY, PULSE }

    @ConfigEntry.BoundedDiscrete(min = 0, max = 2000)
    public int outboundDelayMs = 100;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 2000)
    public int inboundDelayMs = 0;

    @ConfigEntry.BoundedDiscrete(min = 50, max = 5000)
    public int pulseHoldMs = 500;

    @ConfigEntry.BoundedDiscrete(min = 10, max = 500)
    public int pulseReleaseMs = 100;

    public boolean logPackets = false;
}
