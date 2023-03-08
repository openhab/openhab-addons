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
package org.openhab.binding.hdpowerview.internal.handler;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;
import static org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hdpowerview.internal.GatewayWebTargets;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Shade;
import org.openhab.binding.hdpowerview.internal.dto.gen3.ShadePosition;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing handler for shades in an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ShadeThingHandler.class);

    private static final String INVALID_CHANNEL = "invalid channel";
    private static final String INVALID_COMMAND = "invalid command";
    private static final String COMMAND_CALIBRATE = "CALIBRATE";
    private static final String COMMAND_IDENTIFY = "IDENTIFY";

    private final Shade thisShade = new Shade();
    private boolean isInitialized;

    public ShadeThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    /**
     * Getter for the hub handler.
     *
     * @return the hub handler.
     * @throws IllegalStateException if the bridge or its handler are not initialized.
     */
    private GatewayBridgeHandler getHandler() throws IllegalStateException {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            throw new IllegalStateException("Bridge not initialised.");
        }
        BridgeHandler handler = bridge.getHandler();
        if (!(handler instanceof GatewayBridgeHandler)) {
            throw new IllegalStateException("Bridge handler not initialised.");
        }
        return (GatewayBridgeHandler) handler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            getHandler().handleCommand(channelUID, command);
            return;
        }

        GatewayWebTargets webTargets = getHandler().getWebTargets();
        ShadePosition position = new ShadePosition();
        int shadeId = thisShade.getId();
        try {
            switch (channelUID.getId()) {
                case CHANNEL_SHADE_POSITION:
                    if (command instanceof PercentType) {
                        position.setPosition(PRIMARY_POSITION, ((PercentType) command).intValue());
                        webTargets.moveShade(shadeId, position);
                        break;
                    } else if (command instanceof UpDownType) {
                        position.setPosition(PRIMARY_POSITION, UpDownType.UP == command ? 0 : 100);
                        webTargets.moveShade(shadeId, position);
                        break;
                    } else if (StopMoveType.STOP == command) {
                        webTargets.stopShade(shadeId);
                        break;
                    }
                    throw new IllegalArgumentException(INVALID_COMMAND);

                case CHANNEL_SHADE_SECONDARY_POSITION:
                    if (command instanceof PercentType) {
                        position.setPosition(SECONDARY_POSITION, ((PercentType) command).intValue());
                        webTargets.moveShade(shadeId, position);
                        break;
                    } else if (command instanceof UpDownType) {
                        position.setPosition(SECONDARY_POSITION, UpDownType.UP == command ? 0 : 100);
                        webTargets.moveShade(shadeId, position);
                        break;
                    } else if (StopMoveType.STOP == command) {
                        webTargets.stopShade(shadeId);
                        break;
                    }
                    throw new IllegalArgumentException(INVALID_COMMAND);

                case CHANNEL_SHADE_VANE:
                    if (command instanceof PercentType) {
                        position.setPosition(VANE_TILT_POSITION, ((PercentType) command).intValue());
                        webTargets.moveShade(shadeId, position);
                        break;
                    } else if (command instanceof UpDownType) {
                        position.setPosition(VANE_TILT_POSITION, UpDownType.UP == command ? 0 : 100);
                        webTargets.moveShade(shadeId, position);
                        break;
                    }
                    throw new IllegalArgumentException(INVALID_COMMAND);

                case CHANNEL_SHADE_COMMAND:
                    if (command instanceof StringType) {
                        if (COMMAND_IDENTIFY.equals(((StringType) command).toString())) {
                            webTargets.jogShade(shadeId);
                            break;
                        } else if (COMMAND_CALIBRATE.equals(((StringType) command).toString())) {
                            webTargets.calibrateShade(shadeId);
                            break;
                        }
                    }
                    throw new IllegalArgumentException(INVALID_COMMAND);

                default:
                    throw new IllegalArgumentException(INVALID_CHANNEL);
            }
        } catch (HubProcessingException | IllegalArgumentException e) {
            logger.warn("handleCommand() shadeId:{}, channelUID:{}, command:{}, exception:{}, message:{}", shadeId,
                    channelUID, command, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void initialize() {
        thisShade.setId(getConfigAs(HDPowerViewShadeConfiguration.class).id);
        Bridge bridge = getBridge();
        BridgeHandler bridgeHandler = bridge != null ? bridge.getHandler() : null;
        if (!(bridgeHandler instanceof GatewayBridgeHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.invalid-bridge-handler");
            return;
        }
        isInitialized = false;
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.schedule(() -> ((GatewayBridgeHandler) bridgeHandler).refreshShade(thisShade.getId()), 5,
                TimeUnit.SECONDS);
    }

    /**
     * Handle shade state change notifications.
     *
     * @param shade the new shade state.
     * @return true if we handled the call.
     */
    public boolean notify(Shade shade) {
        if (thisShade.getId() == shade.getId()) {
            updateStatus(ThingStatus.ONLINE);
            if (!isInitialized) {
                ShadePosition position = shade.getShadePositions();
                if (position != null) {
                    thisShade.setShadePosition(position);
                }
                updateProperties(shade);
                updateDynamicChannels(shade);
            }
            isInitialized = true;
            updateChannels(shade);
            return true;
        }
        return false;
    }

    /**
     * Update channels based on the data in the passed shade instance.
     *
     * @param shade containing the channel data.
     */
    private void updateChannels(Shade shade) {
        updateState(CHANNEL_SHADE_POSITION, shade.getPosition(PRIMARY_POSITION));
        updateState(CHANNEL_SHADE_VANE, shade.getPosition(VANE_TILT_POSITION));
        updateState(CHANNEL_SHADE_SECONDARY_POSITION, shade.getPosition(SECONDARY_POSITION));
        if (shade.hasFullState()) {
            updateState(CHANNEL_SHADE_LOW_BATTERY, shade.getLowBattery());
            updateState(CHANNEL_SHADE_BATTERY_LEVEL, shade.getBatteryLevel());
            updateState(CHANNEL_SHADE_SIGNAL_STRENGTH, shade.getSignalStrength());
        }
    }

    /**
     * If the given channel exists in the thing, but is NOT required in the thing, then add it to a list of channels to
     * be removed. Or if the channel does NOT exist in the thing, but is required in the thing, then log a warning.
     *
     * @param removeList the list of channels to be removed from the thing.
     * @param channelId the id of the channel to be (eventually) removed.
     * @param channelRequired true if the thing requires this channel.
     */
    private void updateDynamicChannel(List<Channel> removeList, String channelId, boolean channelRequired) {
        Channel channel = thing.getChannel(channelId);
        if (!channelRequired && channel != null) {
            removeList.add(channel);
        } else if (channelRequired && channel == null) {
            logger.warn("updateDynamicChannel() shadeId:{} is missing channel:{} => please recreate the thing",
                    thisShade.getId(), channelId);
        }
    }

    /**
     * Remove previously statically created channels if the shade does not support them or they are not relevant.
     *
     * @param shade containing the channel data.
     */
    private void updateDynamicChannels(Shade shade) {
        List<Channel> removeChannels = new ArrayList<>();

        ShadePosition positions = shade.getShadePositions();
        if (positions != null) {
            updateDynamicChannel(removeChannels, CHANNEL_SHADE_POSITION, positions.supportsPrimary());
            updateDynamicChannel(removeChannels, CHANNEL_SHADE_SECONDARY_POSITION, positions.supportsSecondary());
            updateDynamicChannel(removeChannels, CHANNEL_SHADE_VANE, positions.supportsTilt());
        }

        updateDynamicChannel(removeChannels, CHANNEL_SHADE_BATTERY_LEVEL, shade.isMainsPowered());
        updateDynamicChannel(removeChannels, CHANNEL_SHADE_LOW_BATTERY, shade.isMainsPowered());

        if (!removeChannels.isEmpty()) {
            if (logger.isDebugEnabled()) {
                StringJoiner joiner = new StringJoiner(", ");
                removeChannels.forEach(c -> joiner.add(c.getUID().getId()));
                logger.debug("updateDynamicChannels() shadeId:{}, removing unsupported channels:{}", thisShade.getId(),
                        joiner.toString());
            }
            updateThing(editThing().withoutChannels(removeChannels).build());
        }
    }

    /**
     * Update thing properties based on the data in the passed shade instance.
     *
     * @param shade containing the property data.
     */
    private void updateProperties(Shade shade) {
        if (shade.hasFullState()) {
            thing.setProperties(Stream.of(new String[][] { //
                    { HDPowerViewBindingConstants.PROPERTY_NAME, shade.getName() },
                    { HDPowerViewBindingConstants.PROPERTY_SHADE_TYPE, shade.getTypeString() },
                    { HDPowerViewBindingConstants.PROPERTY_SHADE_CAPABILITIES, shade.getCapabilitieString() },
                    { HDPowerViewBindingConstants.PROPERTY_POWER_TYPE, shade.getPowerType() },
                    { HDPowerViewBindingConstants.PROPERTY_BLE_NAME, shade.getBleName() },
                    { Thing.PROPERTY_FIRMWARE_VERSION, shade.getFirmware() } //
            }).collect(Collectors.toMap(data -> data[0], data -> data[1])));
        }
    }
}
