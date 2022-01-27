/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Zone} Paradox zone.
 * ID is always numeric (1-192 for Evo192)
 * States are taken from cached RAM memory map and parsed.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class Zone extends Entity {

    private final Logger logger = LoggerFactory.getLogger(Zone.class);

    private ZoneState zoneState;

    public Zone(int id, String label) {
        super(id, label);
    }

    public ZoneState getZoneState() {
        return zoneState;
    }

    public void setZoneState(ZoneState zoneState) {
        this.zoneState = zoneState;
        logger.debug("Zone {} state updated to:\tOpened: {}, Tampered: {}, LowBattery: {}", getLabel(),
                zoneState.isOpened(), zoneState.isTampered(), zoneState.hasLowBattery());
    }
}
