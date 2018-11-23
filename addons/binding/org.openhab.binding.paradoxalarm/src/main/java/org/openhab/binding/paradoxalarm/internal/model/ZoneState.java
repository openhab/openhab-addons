/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link ZoneStateFlags} Paradox zone state flags. Retrieved and parsed from RAM memory responses.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ZoneState {
    private boolean isOpened;
    private boolean isTampered;
    private boolean hasLowBattery;

    public ZoneState(boolean isOpened, boolean isTampered, boolean hasLowBattery) {
        this.isOpened = isOpened;
        this.isTampered = isTampered;
        this.hasLowBattery = hasLowBattery;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean isOpened) {
        this.isOpened = isOpened;
    }

    public boolean isTampered() {
        return isTampered;
    }

    public void setTampered(boolean isTampered) {
        this.isTampered = isTampered;
    }

    public boolean hasLowBattery() {
        return hasLowBattery;
    }

    public void setHasLowBattery(boolean hasLowBattery) {
        this.hasLowBattery = hasLowBattery;
    }
}
