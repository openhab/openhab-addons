/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ColorCommand} is a POJO for sending a color command
 * to the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ColorCommand extends HyperionCommand {

    @SerializedName("origin")
    private String origin;
    private static final String NAME = "color";

    @SerializedName("priority")
    private int priority;

    @SerializedName("color")
    private int[] color = new int[3];

    public ColorCommand(int red, int green, int blue, int priority) {
        super(NAME);
        setPriority(priority);
        setColor(red, green, blue);
    }

    public void setColor(int red, int green, int blue) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int[] getColor() {
        return color;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

}
