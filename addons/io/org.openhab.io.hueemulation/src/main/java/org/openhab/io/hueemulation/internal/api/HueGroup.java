package org.openhab.io.hueemulation.internal.api;

/**
 * Hue API group object
 *
 * @author Dan Cunningham
 *
 */
public class HueGroup {
    public HueState state;
    public String type = "LightGroup";
    public String name;
    public String[] lights;
    public HueState action;

    public HueGroup(String name, String[] lights, HueState action) {
        this.name = name;
        this.lights = lights;
        this.action = action;
    }
}
