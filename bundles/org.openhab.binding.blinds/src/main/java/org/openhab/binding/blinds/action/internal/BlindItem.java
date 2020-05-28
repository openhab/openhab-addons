/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.blinds.action.internal;

import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus Pfleger - Initial contribution
 *
 */
public class BlindItem {
    private final Logger logger = LoggerFactory.getLogger(BlindItem.class);

    private final String rollershutterItemName;
    private final String autoRollershutterItemName;

    private final String slatItemName;
    private final String windowContactName;
    private final String temperatureItemName;

    private final BlindItemConfig raffstoreItemConfig = new BlindItemConfig();

    private State storedRollershutterState = null;
    private State storedSlatState = null;

    private long lastBlindMoveTimestamp;
    private long lastSlatMoveTimestamp;
    private ItemRegistry itemRegistry;

    public BlindItem(String rollershutterItemName, String autoRollershutterItemName, String slatItemName,
            String windowContactName, String temperatureItemName, ItemRegistry itemRegistry) {
        super();
        this.rollershutterItemName = rollershutterItemName;
        this.autoRollershutterItemName = autoRollershutterItemName;
        this.slatItemName = slatItemName;
        this.windowContactName = windowContactName;
        this.temperatureItemName = temperatureItemName;
        this.itemRegistry = itemRegistry;
    }

    public void storeState(State storedRollershutterState, State storedSlatState) {
        this.storedRollershutterState = storedRollershutterState;
        this.storedSlatState = storedSlatState;
    }

    public State getStoredRollershutterState() {
        return storedRollershutterState;
    }

    public State getStoredSlatState() {
        return storedSlatState;
    }

    public BlindItemConfig getConfig() {
        return raffstoreItemConfig;
    }

    public String getRollershutterItemName() {
        return rollershutterItemName;
    }

    public String getAutoRollershutterItemName() {
        return autoRollershutterItemName;
    }

    public String getSlatItemName() {
        return slatItemName;
    }

    public String getWindowContactName() {
        return windowContactName;
    }

    public String getTemperatureItemName() {
        return temperatureItemName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rollershutterItemName == null) ? 0 : rollershutterItemName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlindItem other = (BlindItem) obj;
        if (rollershutterItemName == null) {
            if (other.rollershutterItemName != null) {
                return false;
            }
        } else if (!rollershutterItemName.equals(other.rollershutterItemName)) {
            return false;
        }
        return true;
    }

    public boolean azimutMatches(int azimut, int tolerance) {
        if (getConfig().isAzimutRangeSet() && azimut >= getConfig().getAzimutLowerBound() - tolerance
                && azimut <= getConfig().getAzimutUpperBound() + tolerance) {
            return true;
        } else {
            return false;
        }
    }

    public boolean elevationMatches(int elevation, int tolerance) {
        if (getConfig().isElevationRangeSet() && elevation >= getConfig().getElevationLowerBound() - tolerance
                && elevation <= getConfig().getElevationUpperBound() + tolerance) {
            return true;
        } else {
            return false;
        }
    }

    public void setLatestBlindMoveTimestamp(long currentTimeMillis) {
        this.lastBlindMoveTimestamp = currentTimeMillis;
    }

    public long getMsSinceLastBlindMove() {
        return System.currentTimeMillis() - lastBlindMoveTimestamp;
    }

    public void setLatestSlatMoveTimestamp(long currentTimeMillis) {
        this.lastSlatMoveTimestamp = currentTimeMillis;
    }

    public long getMsSinceLastSlatMove() {
        return System.currentTimeMillis() - lastSlatMoveTimestamp;
    }

    public ContactItem getWindowContactItem() {
        ContactItem windowContactItem = null;
        if (getWindowContactName() != null) {
            try {
                windowContactItem = (ContactItem) itemRegistry.getItem(getWindowContactName());
            } catch (ItemNotFoundException e) {
                logger.warn("Unable to find related window contact with name {}", getWindowContactName());
            }
        }
        return windowContactItem;
    }

    public DimmerItem getSlatItem() {
        DimmerItem slatItem = null;
        try {
            slatItem = (DimmerItem) itemRegistry.getItem(getSlatItemName());
        } catch (ItemNotFoundException e) {
            logger.warn("Unable to find slat item with name {}", getSlatItemName());
        }
        return slatItem;
    }

    public RollershutterItem getAutoRollershutterItem() {
        RollershutterItem autoRollershutterItem = null;
        if (getAutoRollershutterItemName() != null) {
            try {
                autoRollershutterItem = (RollershutterItem) itemRegistry.getItem(getAutoRollershutterItemName());
            } catch (ItemNotFoundException e) {
                logger.warn("Unable to find auto rollershutter with name {}", getAutoRollershutterItemName());
            }
        }
        return autoRollershutterItem;
    }

    public RollershutterItem getRollershutterItem() {
        RollershutterItem rollershutterItem = null;
        try {
            rollershutterItem = (RollershutterItem) itemRegistry.getItem(getRollershutterItemName());
        } catch (ItemNotFoundException e) {
            logger.warn("Unable to find rollershutter with name {}", getRollershutterItemName());
        }
        return rollershutterItem;
    }
}
