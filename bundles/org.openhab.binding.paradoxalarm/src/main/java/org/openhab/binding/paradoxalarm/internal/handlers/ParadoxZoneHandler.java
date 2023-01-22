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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.binding.paradoxalarm.internal.model.Zone;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxZoneHandler} Handler that updates states of paradox zones from the cache.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxZoneHandler extends EntityBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(ParadoxZoneHandler.class);

    public ParadoxZoneHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateEntity() {
        int index = calculateEntityIndex();
        ParadoxIP150BridgeHandler bridge = (ParadoxIP150BridgeHandler) getBridge().getHandler();
        ParadoxPanel panel = bridge.getPanel();
        List<Zone> zones = panel.getZones();
        if (zones == null) {
            logger.debug(
                    "Zones collection of Paradox Panel object is null. Probably not yet initialized. Skipping update.");
            return;
        }
        if (zones.size() <= index) {
            logger.debug("Attempted to access zone out of bounds of current zone list. Index: {}, List: {}", index,
                    zones);
            return;
        }

        Zone zone = zones.get(index);
        if (zone != null) {
            updateState(ZONE_LABEL_CHANNEL_UID, new StringType(zone.getLabel()));
            updateState(ZONE_OPENED_CHANNEL_UID, booleanToContactState(zone.getZoneState().isOpened()));
            updateState(ZONE_TAMPERED_CHANNEL_UID, booleanToSwitchState(zone.getZoneState().isTampered()));
            updateState(ZONE_LOW_BATTERY_CHANNEL_UID, booleanToSwitchState(zone.getZoneState().hasLowBattery()));
        }
    }

    private OpenClosedType booleanToContactState(boolean value) {
        return value ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    private OnOffType booleanToSwitchState(boolean value) {
        return value ? OnOffType.ON : OnOffType.OFF;
    }
}
