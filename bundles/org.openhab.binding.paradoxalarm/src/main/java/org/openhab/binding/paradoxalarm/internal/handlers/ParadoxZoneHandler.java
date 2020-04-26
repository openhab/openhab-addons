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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.binding.paradoxalarm.internal.model.Zone;
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
        List<Zone> zones = ParadoxPanel.getInstance().getZones();
        if (zones != null && zones.size() > index) {
            Zone zone = zones.get(index);
            if (zone != null) {
                updateState(ZONE_LABEL_CHANNEL_UID, new StringType(zone.getLabel()));
                updateState(ZONE_OPENED_CHANNEL_UID, booleanToContactState(zone.getZoneState().isOpened()));
                updateState(ZONE_TAMPERED_CHANNEL_UID, booleanToSwitchState(zone.getZoneState().isTampered()));
                updateState(ZONE_LOW_BATTERY_CHANNEL_UID, booleanToSwitchState(zone.getZoneState().hasLowBattery()));
            }
        } else {
            logger.warn("Attempted to access zone out of bounds of current zone list. Index: {}, List: {}", index,
                    zones);
        }
    }

    private OpenClosedType booleanToContactState(boolean value) {
        return value ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    private OnOffType booleanToSwitchState(boolean value) {
        return value ? OnOffType.ON : OnOffType.OFF;
    }
}
