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
import org.openhab.binding.paradoxalarm.internal.model.ZoneState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
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
        ParadoxIP150BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Paradox bridge handler is null. Skipping update.");
            return;
        }

        ParadoxPanel panel = bridgeHandler.getPanel();
        List<Zone> zones = panel.getZones();
        if (zones == null) {
            logger.debug(
                    "Zones collection of Paradox Panel object is null. Probably not yet initialized. Skipping update.");
            return;
        }

        int index = calculateEntityIndex();
        if (zones.size() <= index) {
            logger.debug("Attempted to access zone out of bounds of current zone list. Index: {}, List: {}", index,
                    zones);
            return;
        }

        Zone zone = zones.get(index);
        if (zone != null) {
            updateState(ZONE_LABEL_CHANNEL_UID, new StringType(zone.getLabel()));
            ZoneState zoneState = zone.getZoneState();
            if (zoneState != null) {
                updateState(ZONE_OPENED_CHANNEL_UID, booleanToContactState(zoneState.isOpened()));
                updateState(ZONE_TAMPERED_CHANNEL_UID, booleanToSwitchState(zoneState.isTampered()));
                updateState(ZONE_LOW_BATTERY_CHANNEL_UID, booleanToSwitchState(zoneState.hasLowBattery()));

                updateState(ZONE_SUPERVISION_TROUBLE_UID, booleanToSwitchState(zoneState.isSupervisionTrouble()));
                updateState(ZONE_IN_TX_DELAY_UID, booleanToSwitchState(zoneState.isInTxDelay()));
                updateState(ZONE_SHUTDOWN_UID, booleanToSwitchState(zoneState.isShutdown()));
                updateState(ZONE_BYPASSED_UID, booleanToSwitchState(zoneState.isBypassed()));
                updateState(ZONE_HAS_ACTIVATED_INTELLIZONE_DELAY_UID,
                        booleanToSwitchState(zoneState.isHasActivatedIntellizoneDelay()));
                updateState(ZONE_HAS_ACTIVATED_ENTRY_DELAY_UID,
                        booleanToSwitchState(zoneState.isHasActivatedEntryDelay()));
                updateState(ZONE_PRESENTLY_IN_ALARM_UID, booleanToSwitchState(zoneState.isPresentlyInAlarm()));
                updateState(ZONE_GENERATED_ALARM_UID, booleanToSwitchState(zoneState.isGeneratedAlarm()));
            }
        }
    }

    private OpenClosedType booleanToContactState(boolean value) {
        return value ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    private OnOffType booleanToSwitchState(boolean value) {
        return value ? OnOffType.ON : OnOffType.OFF;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (command instanceof StringType) {
            Zone zone = getZone();
            if (zone != null) {
                zone.handleCommand(command.toString());
            }
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    protected Zone getZone() {
        ParadoxIP150BridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Paradox bridge handler is null. Skipping update.");
            return null;
        }

        ParadoxPanel panel = bridgeHandler.getPanel();
        List<Zone> zones = panel.getZones();
        if (zones == null) {
            logger.debug(
                    "Zones collection of Paradox Panel object is null. Probably not yet initialized. Skipping update.");
            return null;
        }

        int index = calculateEntityIndex();
        if (zones.size() <= index) {
            logger.debug("Attempted to access a zone out of bounds of current zone list. Index: {}, List: {}", index,
                    zones);
            return null;
        }

        Zone zone = zones.get(index);
        return zone;
    }

    private ParadoxIP150BridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Paradox bridge is null. Skipping update.");
            return null;
        }

        return (ParadoxIP150BridgeHandler) bridge.getHandler();
    }
}
