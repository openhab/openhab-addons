/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneStatus;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link EvohomeHeatingZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Neil Renaud - Working implementation
 * @author Jasper van Zuijlen - Refactor + Permanent Zone temperature setting
 * @author Leo Siepel - Add UoM
 */
@NonNullByDefault
public class EvohomeHeatingZoneHandler extends BaseEvohomeHandler {

    private static final int CANCEL_SET_POINT_OVERRIDE = 0;
    private @Nullable ThingStatus tcsStatus;
    private @Nullable ZoneStatus zoneStatus;

    public EvohomeHeatingZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public void update(@Nullable ThingStatus tcsStatus, @Nullable ZoneStatus zoneStatus) {
        this.tcsStatus = tcsStatus;
        this.zoneStatus = zoneStatus;

        // Make the zone offline when the related display is offline
        // If the related display is not a thing, ignore this
        if (tcsStatus != null && tcsStatus.equals(ThingStatus.OFFLINE)) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Display Controller offline");
        } else if (zoneStatus == null) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Status not found, check the zone id");
        } else if (!handleActiveFaults(zoneStatus)) {
            updateEvohomeThingStatus(ThingStatus.ONLINE);

            updateState(EvohomeBindingConstants.ZONE_TEMPERATURE_CHANNEL,
                    new QuantityType<>(zoneStatus.getTemperature().getTemperature(), SIUnits.CELSIUS));
            updateState(EvohomeBindingConstants.ZONE_SET_POINT_STATUS_CHANNEL,
                    new StringType(zoneStatus.getHeatSetpoint().getSetpointMode()));
            updateState(EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL,
                    new QuantityType<>(zoneStatus.getHeatSetpoint().getTargetTemperature(), SIUnits.CELSIUS));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            update(tcsStatus, zoneStatus);
        } else {
            EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
            if (bridge != null) {
                String channelId = channelUID.getId();
                if (EvohomeBindingConstants.ZONE_SET_POINT_CHANNEL.equals(channelId)) {
                    if (command instanceof QuantityType quantityCommand) {
                        QuantityType<?> state = quantityCommand.toUnit(SIUnits.CELSIUS);
                        double newTempInCelsius = state.doubleValue();

                        if (newTempInCelsius == CANCEL_SET_POINT_OVERRIDE) {
                            bridge.cancelSetPointOverride(getEvohomeThingConfig().id);
                        } else if (newTempInCelsius < 5) {
                            newTempInCelsius = 5;
                        }
                        if (newTempInCelsius >= 5 && newTempInCelsius <= 35) {
                            bridge.setPermanentSetPoint(getEvohomeThingConfig().id, newTempInCelsius);
                        }
                    }
                }
            }
        }
    }

    private boolean handleActiveFaults(ZoneStatus zoneStatus) {
        if (zoneStatus.hasActiveFaults()) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    zoneStatus.getActiveFault(0).getFaultType());
            return true;
        }
        return false;
    }
}
