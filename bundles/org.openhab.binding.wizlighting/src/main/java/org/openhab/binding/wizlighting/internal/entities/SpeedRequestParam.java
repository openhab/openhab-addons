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
 * This POJO represents Speed Request Param
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class SpeedRequestParam implements Param {
    private int speed;

    public SpeedRequestParam(int speed) {
        this.speed = speed;
    }

    public SpeedRequestParam(Command command) {
        if (command instanceof PercentType) {
            this.setSpeed(((PercentType) command).intValue());
        }
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
