/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link ZoneStateFlags} Paradox zone state flags. Retrieved and parsed from RAM memory responses.
 *
 * @author Konstantin Polihronov - Initial contribution
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
