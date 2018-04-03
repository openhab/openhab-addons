/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
