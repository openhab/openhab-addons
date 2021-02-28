/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants;
import org.openhab.binding.freeathomesystem.internal.valuestateconverters.BooleanValueStateConverter;
import org.openhab.binding.freeathomesystem.internal.valuestateconverters.DecimalValueStateConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeThermostatHandler extends FreeAtHomeSystemBaseHandler {

    private @Nullable String deviceID;
    private @Nullable String deviceChannel;
    private @Nullable String measuredTempOdp;
    private @Nullable String heatingDemandOdp;
    private @Nullable String heatingActiveOdp;
    private @Nullable String setpointTempOdp;
    private @Nullable String statesOdp;
    private @Nullable String setpointTempIdp;
    private @Nullable String onoffSwitchIdp;
    private @Nullable String ecoSwitchIdp;
    private @Nullable String onoffIndicationOdp;
    private @Nullable String ecoIndicationOdp;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeThermostatHandler.class);

    // private ScheduledFuture pollingJob = null;

    public FreeAtHomeThermostatHandler(Thing thing) {
        super(thing);
    }

    public void setupUpdateChannelsForEvents(FreeAtHomeBridgeHandler freeAtHomeBridge) {

        // Register device and specific channel for event based state updated
        ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

        updateHandler.registerChannel(deviceID, deviceChannel, measuredTempOdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_MEASUREDTEMP_ID),
                new DecimalValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, heatingDemandOdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_HEATDEMAND_ID),
                new DecimalValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, heatingActiveOdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_HEATINGACTIVE_ID),
                new DecimalValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, setpointTempOdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_SETPOINTTEMP_ID),
                new DecimalValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, setpointTempIdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_SETPOINTTEMP_ID),
                new DecimalValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, onoffSwitchIdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ONOFFWITCH_ID),
                new BooleanValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, ecoSwitchIdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ECOSWITCH_ID),
                new BooleanValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, onoffIndicationOdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ONOFFWITCH_ID),
                new BooleanValueStateConverter());

        updateHandler.registerChannel(deviceID, deviceChannel, ecoIndicationOdp, this,
                new ChannelUID(this.getThing().getUID(),
                        FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ECOSWITCH_ID),
                new BooleanValueStateConverter());
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        // Initialize the communication device channel properties
        deviceID = properties.get("deviceId");
        deviceChannel = properties.get("channelId");
        measuredTempOdp = properties.get("measuredTempOdp");
        heatingDemandOdp = properties.get("heatingDemandOdp");
        heatingActiveOdp = properties.get("heatingActiveOdp");
        setpointTempOdp = properties.get("setpointTempOdp");
        statesOdp = properties.get("statesOdp");
        setpointTempIdp = properties.get("setpointTempIdp");
        onoffSwitchIdp = properties.get("onoffSwitchIdp");
        ecoSwitchIdp = properties.get("ecoSwitchIdp");
        onoffIndicationOdp = properties.get("onoffIndicationOdp");
        ecoIndicationOdp = properties.get("ecoIndicationOdp");

        if ((null == deviceID) || (null == deviceChannel) || (null == measuredTempOdp) || (null == heatingDemandOdp)
                || (null == heatingActiveOdp) || (null == setpointTempOdp) || (null == statesOdp)
                || (null == setpointTempIdp) || (null == onoffSwitchIdp) || (null == ecoSwitchIdp)
                || (null == onoffIndicationOdp) || (null == ecoIndicationOdp)) {
            // Device properties are not found
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device properties are not found");
            return;
        }

        Bridge bridge = this.getBridge();

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {

                FreeAtHomeBridgeHandler freeAtHomeBridge;
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                // Register device and specific channel for event based state updated
                setupUpdateChannelsForEvents(freeAtHomeBridge);

                logger.debug("Device - online: {}", deviceID);

                updateStatus(ThingStatus.ONLINE);

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);

                logger.debug("Incorrect bridge class: {}", deviceID);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge available");

            logger.debug("No bridge for device: {}", deviceID);
        }
    }

    @Override
    public void dispose() {
        Bridge bridge = this.getBridge();

        // Unregister device and specific channel for event based state updated
        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                FreeAtHomeBridgeHandler freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                updateHandler.unregisterChannel(deviceID, deviceChannel, measuredTempOdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, heatingDemandOdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, heatingActiveOdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, setpointTempOdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, setpointTempIdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, onoffSwitchIdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, ecoSwitchIdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, onoffIndicationOdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, ecoIndicationOdp);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;
            }
        }

        if (null != freeAtHomeBridge) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        if (command instanceof RefreshType) {
            if (channelUID.getId()
                    .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_MEASUREDTEMP_ID)) {
                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, measuredTempOdp);
                DecimalType dec = new DecimalType(value);

                updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_MEASUREDTEMP_ID, dec);
            }

            if (channelUID.getId()
                    .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_SETPOINTTEMP_ID)) {
                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, setpointTempOdp);
                DecimalType dec = new DecimalType(value);

                updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_SETPOINTTEMP_ID, dec);
            }

            if (channelUID.getId()
                    .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_HEATDEMAND_ID)) {

                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, heatingDemandOdp);
                DecimalType dec = new DecimalType(value);
                updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_HEATDEMAND_ID, dec);
            }

            if (channelUID.getId()
                    .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_HEATINGACTIVE_ID)) {
                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, heatingActiveOdp);
                DecimalType dec = new DecimalType(value);
                updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_HEATINGACTIVE_ID, dec);
            }

            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_STATE_ID)) {

                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, statesOdp);
                DecimalType dec = new DecimalType(value);
                updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_STATE_ID, dec);
            }

            if (channelUID.getId()
                    .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ONOFFWITCH_ID)) {

                // 54 out states: on/off; heating/cooling; eco/comfort; frost/not frost bitmask:
                // 0x01 - comfort mode
                // 0x02 - standby
                // 0x04 - eco mode
                // 0x08 - building protect
                // 0x10 - dew alarm
                // 0x20 - heat (set) / cool (unset)
                // 0x40 - no heating/cooling (set)
                // 0x80 - frost alarm
                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, statesOdp);
                int intValue = Integer.decode(value);
                int result;

                result = intValue & 0x01;

                if (0x01 == result) {
                    updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ONOFFWITCH_ID, OnOffType.ON);
                } else {
                    updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ONOFFWITCH_ID, OnOffType.OFF);

                }
            }

            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ECOSWITCH_ID)) {

                // 54 out states: on/off; heating/cooling; eco/comfort; frost/not frost bitmask:
                // 0x01 - comfort mode
                // 0x02 - standby
                // 0x04 - eco mode
                // 0x08 - building protect
                // 0x10 - dew alarm
                // 0x20 - heat (set) / cool (unset)
                // 0x40 - no heating/cooling (set)
                // 0x80 - frost alarm
                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, statesOdp);
                int intValue = Integer.decode(value);
                int result;

                result = intValue & 0x04;

                if (0x04 == result) {
                    updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ECOSWITCH_ID, OnOffType.ON);
                } else {
                    updateState(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ECOSWITCH_ID, OnOffType.OFF);
                }
            }
        }

        if (command instanceof OnOffType) {
            OnOffType locCommand = (OnOffType) command;

            if (locCommand.equals(OnOffType.ON)) {
                if (channelUID.getId()
                        .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ONOFFWITCH_ID)) {

                    freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, onoffSwitchIdp, "1");
                    updateState(channelUID, OnOffType.ON);
                }

                if (channelUID.getId()
                        .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ECOSWITCH_ID)) {

                    freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, ecoSwitchIdp, "1");
                    updateState(channelUID, OnOffType.ON);
                }
            }

            if (locCommand.equals(OnOffType.OFF)) {
                if (channelUID.getId()
                        .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ONOFFWITCH_ID)) {

                    freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, onoffSwitchIdp, "0");
                    updateState(channelUID, OnOffType.OFF);
                }

                if (channelUID.getId()
                        .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_ECOSWITCH_ID)) {

                    freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, ecoSwitchIdp, "0");
                    updateState(channelUID, OnOffType.OFF);
                }
            }
        }

        if (command instanceof QuantityType) {
            if (channelUID.getId()
                    .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_SETPOINTTEMP_ID)) {

                String valueString = ((DecimalType) command).toString();

                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, setpointTempIdp, valueString);

                updateState(channelUID, ((DecimalType) command));
            }
        }

        if (command instanceof DecimalType) {
            if (channelUID.getId()
                    .equalsIgnoreCase(FreeAtHomeSystemBindingConstants.THERMOSTAT_CHANNEL_SETPOINTTEMP_ID)) {

                String valueString = ((DecimalType) command).toString();

                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, setpointTempIdp, valueString);

                updateState(channelUID, ((DecimalType) command));
            }
        }

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
