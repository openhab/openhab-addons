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
package org.openhab.binding.nobohub.internal;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_COMPONENT_CURRENT_TEMPERATURE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.model.Component;
import org.openhab.binding.nobohub.internal.model.SerialNumber;
import org.openhab.binding.nobohub.internal.model.Zone;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows information about a Component in the Nobø Hub.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ComponentHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ComponentHandler.class);

    protected @Nullable SerialNumber serialNumber;

    public ComponentHandler(Thing thing) {
        super(thing);
    }

    public void onUpdate(Component component) {
        updateStatus(ThingStatus.ONLINE);

        double temp = component.getTemperature();
        if (!Double.isNaN(temp)) {
            try {
                DecimalType currentTemperature = new DecimalType(temp);
                updateState(CHANNEL_COMPONENT_CURRENT_TEMPERATURE, currentTemperature);
            } catch (NumberFormatException nfe) {
                logger.debug("Couldnt set decimal value to temperature: {}", temp);
            }
        }

        updateProperty("serialNumber", component.getSerialNumber().toString());
        updateProperty("name", component.getName());
        updateProperty("model", component.getSerialNumber().getComponentType());

        String zoneName = getZoneName(component.getZoneId());
        if (zoneName != null) {
            updateProperty("zone", zoneName);
        }

        String tempForZoneName = getZoneName(component.getTemperatureSensorForZoneId());
        if (tempForZoneName != null) {
            updateProperty("temperatureSensorForZone", tempForZoneName);
        }
    }

    private @Nullable String getZoneName(int zoneId) {
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (hubHandler != null) {
                Zone zone = hubHandler.getZone(zoneId);
                if (null != zone) {
                    return zone.getName();
                }
            }
        }

        return null;
    }

    @Override
    public void initialize() {
        String serialNumberString = getConfigAs(ComponentConfiguration.class).serialNumber;
        if (serialNumberString != null) {
            SerialNumber sn = new SerialNumber(serialNumberString);
            if (!sn.isWellFormed()) {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Illegal serial number: " + serialNumber);
            } else {
                this.serialNumber = sn;
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, "Missing serial number");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);
            if (null != serialNumber) {
                Component component = getComponent();
                if (null == component) {
                    logger.error("Could not find Component with serial number {} for channel {}", serialNumber,
                            channelUID);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                } else {
                    onUpdate(component);
                }
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.GONE);
                logger.error("id not set for channel {}", channelUID);
            }

            return;
        }

        logger.debug("This component is a read-only device and cannot handle commands.");
    }

    public @Nullable SerialNumber getSerialNumber() {
        return serialNumber;
    }

    private @Nullable Component getComponent() {
        Bridge noboHub = getBridge();
        if (null != noboHub) {
            NoboHubBridgeHandler hubHandler = (NoboHubBridgeHandler) noboHub.getHandler();
            if (null != serialNumber && null != hubHandler) {
                SerialNumber sn = Helpers.castToNonNull(serialNumber, "serialNumber");
                NoboHubBridgeHandler hh = Helpers.castToNonNull(hubHandler, "hubHandler");
                return hh.getComponent(sn);
            }
        }

        return null;
    }
}
