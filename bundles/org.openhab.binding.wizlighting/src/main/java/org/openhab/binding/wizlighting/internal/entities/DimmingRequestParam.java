/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;

/**
 * This POJO represents Dimming Request Param
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class DimmingRequestParam implements Param {
    private int dimming;

    public DimmingRequestParam(int dimming) {
        this.dimming = dimming;
    }

    public DimmingRequestParam(Command command) {
        if (command instanceof PercentType) {
            this.setDimming(((PercentType) command).intValue());
        } else {
            this.setDimming(100);
        }
    }

    public int getDimming() {
        return dimming;
    }

    public void setDimming(int dimming) {
        // Bulb can't be dimmed below 10%
        if (dimming < 10) {
            dimming = 10;
        }
        this.dimming = dimming;
    }
}