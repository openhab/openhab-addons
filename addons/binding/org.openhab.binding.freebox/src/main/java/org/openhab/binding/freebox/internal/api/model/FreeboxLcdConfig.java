/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxLcdConfig} is the Java class used to map the "LcdConfig"
 * structure used by the LCD configuration API
 * https://dev.freebox.fr/sdk/os/lcd/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLcdConfig {
    private int brightness;
    private boolean orientationForced;
    private int orientation;

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public boolean isOrientationForced() {
        return orientationForced;
    }

    public void setOrientationForced(boolean orientationForced) {
        this.orientationForced = orientationForced;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
