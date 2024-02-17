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
package org.openhab.binding.nobohub.internal;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_COMPONENT_CURRENT_TEMPERATURE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_MODEL;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_TEMPERATURE_SENSOR_FOR_ZONE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_ZONE;

import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.model.Component;
import org.openhab.binding.nobohub.internal.model.SerialNumber;
import org.openhab.binding.nobohub.internal.model.Zone;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
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

    private final NoboHubTranslationProvider messages;

    protected @Nullable SerialNumber serialNumber;

    public ComponentHandler(Thing thing, NoboHubTranslationProvider messages) {
        super(thing);
        this.messages = messages;
    }

    public void onUpdate(Component component) {
        updateStatus(ThingStatus.ONLINE);

        double temp = component.getTemperature();
        if (!Double.isNaN(temp)) {
            QuantityType<Temperature> currentTemperature = new QuantityType<>(temp, SIUnits.CELSIUS);
            updateState(CHANNEL_COMPONENT_CURRENT_TEMPERATURE, currentTemperature);
        }

        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, component.getSerialNumber().toString());
        properties.put(PROPERTY_NAME, component.getName());
        properties.put(PROPERTY_MODEL, component.getSerialNumber().getComponentType());

        String zoneName = getZoneName(component.getZoneId());
        if (zoneName != null) {
            properties.put(PROPERTY_ZONE, zoneName);
        }

        String tempForZoneName = getZoneName(component.getTemperatureSensorForZoneId());
        if (tempForZoneName != null) {
            properties.put(PROPERTY_TEMPERATURE_SENSOR_FOR_ZONE, tempForZoneName);
        }
        updateProperties(properties);
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
        if (serialNumberString != null && !serialNumberString.isEmpty()) {
            SerialNumber sn = new SerialNumber(serialNumberString);
            if (!sn.isWellFormed()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/message.component.illegal.serial [\"" + serialNumberString + "\"]");
            } else {
                this.serialNumber = sn;
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/message.missing.serial");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);
            if (null != serialNumber) {
                Component component = getComponent();
                if (null == component) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                            messages.getText("message.component.notfound", serialNumber, channelUID));
                } else {
                    onUpdate(component);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                        "@text/message.component.missing.id [\"" + channelUID + "\"]");
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
            SerialNumber serialNumber = this.serialNumber;
            if (null != serialNumber && null != hubHandler) {
                return hubHandler.getComponent(serialNumber);
            }
        }

        return null;
    }
}
