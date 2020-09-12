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

package org.openhab.binding.dlinksmarthome.internal.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.dlinksmarthome.internal.DLinkHNAP;
import org.openhab.binding.dlinksmarthome.internal.DLinkSmartHomeBindingConstants;
import org.openhab.binding.dlinksmarthome.internal.DLinkThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DLinkMotionSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pascal Bies - Initial contribution
 */
public class DLinkSmartPlugHandler extends BaseThingHandler {

    final DLinkHNAP hnap;
    private Logger logger = LoggerFactory.getLogger(DLinkSmartPlugHandler.class);

    public DLinkSmartPlugHandler(final Thing thing) {
        super(thing);
        hnap = new DLinkHNAP() {

            @Override
            public void poll() {
                for (final String channelID : DLinkSmartHomeBindingConstants.SMART_PLUG_CHANNEL_IDS) {
                    updateState(getChannelUID(channelID), getNewChannelState(channelID));
                }

            }

            @Override
            public void handleCommandAuthenticated(ChannelUID channelUID, Command command) {
                if (command == RefreshType.REFRESH) {
                    updateState(channelUID, getNewChannelState(channelUID.getId()));
                } else if (channelUID.equals(getChannelUID(DLinkSmartHomeBindingConstants.STATE))) {
                    try {
                        DLinkSmartPlugHandler.this.setState((OnOffType) command);
                    } catch (ClassCastException e) {
                        logger.error("Unexpected command type for channel '{}'.", DLinkSmartHomeBindingConstants.STATE);
                    }
                }

            }

            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail, String message) {
                DLinkSmartPlugHandler.this.updateStatus(status, detail, message);
            }
        };
    }

    @Override
    public void dispose() {
        super.dispose();
        hnap.stop();
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        hnap.handleCommand(channelUID, command);
    }

    @Override
    public void initialize() {
        final DLinkThingConfig config = getConfigAs(DLinkThingConfig.class);
        hnap.setPin(config.pin);
        hnap.setIp(config.ipAddress);
        hnap.start(scheduler);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        hnap.reset();
    }

    private State getCurrentConsuption() {
        return hnap.queryUnitState("GetCurrentPowerConsumption", "CurrentConsumption", "2", SmartHomeUnits.WATT);
    }

    private State getTotalConsumption() {
        return hnap.queryUnitState("GetPMWarningThreshold", "TotalConsumption", "2", SmartHomeUnits.KILOWATT_HOUR);
    }

    private State getTemperature() {
        return hnap.queryUnitState("GetCurrentTemperature", "CurrentTemperature", "3", SIUnits.CELSIUS);
    }

    private State getState() {
        return hnap.queryOnOffState("GetSocketSettings", "OPStatus", "1");
    }

    private void setState(OnOffType state) {
        hnap.setState("SetSocketSettings", "SetSocketSettingsResult", "1", "Socket 1", "Socket 1", state);
    }

    private ChannelUID getChannelUID(final String name) {
        Channel channel = thing.getChannel(name);
        if (channel == null) {
            logger.error("Did not find channel '{}'.", name);
            return null;
        } else {
            return channel.getUID();
        }
    }

    private State getNewChannelState(final String channelID) {
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            switch (channelID) {
                case DLinkSmartHomeBindingConstants.CURRENT_CONSUMPTION:
                    return getCurrentConsuption();
                case DLinkSmartHomeBindingConstants.TOTAL_CONSUMPTION:
                    return getTotalConsumption();
                case DLinkSmartHomeBindingConstants.TEMPERATURE:
                    return getTemperature();
                case DLinkSmartHomeBindingConstants.STATE:
                    return getState();
                default:
                    logger.error("Unexpected channel: '{}'.", channelID);
                    throw new IllegalArgumentException("Unexpected channel.");
            }
        } else {
            return UnDefType.UNDEF;
        }
    }
}
