/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Zone} Paradox zone.
 * ID is always numeric (1-8 for Evo192)
 * States are taken from cached RAM memory map and parsed.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class Zone extends Entity {
    private boolean isOpened;
    private boolean isTampered;
    private boolean hasLowBattery;

    private static Logger logger = LoggerFactory.getLogger(Partition.class);

    public Zone(int id, String label) {
        super(id, label);
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
