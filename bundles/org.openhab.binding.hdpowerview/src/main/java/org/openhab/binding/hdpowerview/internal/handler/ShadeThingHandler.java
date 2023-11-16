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
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.GatewayWebTargets;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Type;
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
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing handler for shades in an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeThingHandler extends BaseThingHandler {

    private static final String INVALID_CHANNEL = "invalid channel";
    private static final String INVALID_COMMAND = "invalid command";

    private static final String COMMAND_IDENTIFY = "IDENTIFY";

    private static final ShadeCapabilitiesDatabase DB = new ShadeCapabilitiesDatabase();

    private final Logger logger = LoggerFactory.getLogger(ShadeThingHandler.class);

    private int shadeId;
    private boolean isInitialized;
    private @Nullable Capabilities capabilities;

    public ShadeThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Getter for the hub handler.
     *
     * @return the hub handler.
     * @throws IllegalStateException if the bridge or its handler are not initialized.
     */
    private GatewayBridgeHandler getBridgeHandler() throws IllegalStateException {
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

    public int getShadeId() {
        return shadeId;
    }

    private Type getType(Shade shade) {
        Integer type = shade.getType();
        return type != null ? DB.getType(type) : new ShadeCapabilitiesDatabase.Type(0);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            getBridgeHandler().handleCommand(channelUID, command);
            return;
        }

        GatewayWebTargets webTargets = getBridgeHandler().getWebTargets();
        ShadePosition position = new ShadePosition();
        try {
            switch (channelUID.getId()) {
                case CHANNEL_SHADE_POSITION:
                    if (command instanceof PercentType percentCommand) {
                        position.setPosition(PRIMARY_POSITION, percentCommand);
                        webTargets.moveShade(shadeId, new Shade().setShadePosition(position));
                        break;
                    } else if (command instanceof UpDownType) {
                        position.setPosition(PRIMARY_POSITION,
                                (UpDownType.UP == command) && !Objects.requireNonNull(capabilities).isPrimaryInverted()
                                        ? PercentType.HUNDRED
                                        : PercentType.ZERO);
                        webTargets.moveShade(shadeId, new Shade().setShadePosition(position));
                        break;
                    } else if (StopMoveType.STOP == command) {
                        webTargets.stopShade(shadeId);
                        break;
                    }
                    throw new IllegalArgumentException(INVALID_COMMAND);

                case CHANNEL_SHADE_SECONDARY_POSITION:
                    if (command instanceof PercentType percentCommand) {
                        position.setPosition(SECONDARY_POSITION, percentCommand);
                        webTargets.moveShade(shadeId, new Shade().setShadePosition(position));
                        break;
                    } else if (command instanceof UpDownType) {
                        position.setPosition(SECONDARY_POSITION,
                                (UpDownType.UP == command)
                                        && !Objects.requireNonNull(capabilities).supportsSecondaryOverlapped()
                                                ? PercentType.ZERO
                                                : PercentType.HUNDRED);
                        webTargets.moveShade(shadeId, new Shade().setShadePosition(position));
                        break;
                    } else if (StopMoveType.STOP == command) {
                        webTargets.stopShade(shadeId);
                        break;
                    }
                    throw new IllegalArgumentException(INVALID_COMMAND);

                case CHANNEL_SHADE_VANE:
                    if (command instanceof PercentType percentCommand) {
                        position.setPosition(VANE_TILT_POSITION, percentCommand);
                        webTargets.moveShade(shadeId, new Shade().setShadePosition(position));
                        break;
                    } else if (command instanceof UpDownType) {
                        position.setPosition(VANE_TILT_POSITION,
                                UpDownType.UP == command ? PercentType.HUNDRED : PercentType.ZERO);
                        webTargets.moveShade(shadeId, new Shade().setShadePosition(position));
                        break;
                    }
                    throw new IllegalArgumentException(INVALID_COMMAND);

                case CHANNEL_SHADE_COMMAND:
                    if ((command instanceof StringType stringCommand)
                            && COMMAND_IDENTIFY.equals(stringCommand.toString())) {
                        webTargets.jogShade(shadeId);
                        break;
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

    private boolean hasPrimary() {
        return Objects.requireNonNull(capabilities).supportsPrimary();
    }

    private boolean hasSecondary() {
        Capabilities capabilities = Objects.requireNonNull(this.capabilities);
        return capabilities.supportsSecondary() || capabilities.supportsSecondaryOverlapped();
    }

    private boolean hasVane() {
        Capabilities capabilities = Objects.requireNonNull(this.capabilities);
        return capabilities.supportsTilt180() || capabilities.supportsTiltAnywhere()
                || capabilities.supportsTiltOnClosed();
    }

    @Override
    public void initialize() {
        shadeId = getConfigAs(HDPowerViewShadeConfiguration.class).id;
        Bridge bridge = getBridge();
        BridgeHandler bridgeHandler = bridge != null ? bridge.getHandler() : null;
        if (!(bridgeHandler instanceof GatewayBridgeHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.invalid-bridge-handler");
            return;
        }
        isInitialized = false;
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Handle shade state change notifications.
     *
     * @param shade the new shade state.
     * @return true if we handled the call.
     */
    public boolean notify(Shade shade) {
        if (shadeId == shade.getId()) {
            updateStatus(ThingStatus.ONLINE);
            if (!isInitialized && shade.hasFullState()) {
                updateCapabilities(shade);
                updateProperties(shade);
                updateDynamicChannels(shade);
                isInitialized = true;
            }
            updateChannels(shade);
            return true;
        }
        return false;
    }

    /**
     * Update the capabilities object based on the data in the passed shade instance.
     *
     * @param shade containing the channel data.
     */
    private void updateCapabilities(Shade shade) {
        Capabilities capabilities = this.capabilities;
        if (capabilities == null) {
            capabilities = DB.getCapabilities(shade.getCapabilities());
            this.capabilities = capabilities;
        }
    }

    /**
     * Update channels based on the data in the passed shade instance.
     *
     * @param shade containing the channel data.
     */
    private void updateChannels(Shade shade) {
        updateState(CHANNEL_SHADE_POSITION, hasPrimary() ? shade.getPosition(PRIMARY_POSITION) : UnDefType.UNDEF);
        updateState(CHANNEL_SHADE_VANE, hasVane() ? shade.getPosition(VANE_TILT_POSITION) : UnDefType.UNDEF);
        updateState(CHANNEL_SHADE_SECONDARY_POSITION,
                hasSecondary() ? shade.getPosition(SECONDARY_POSITION) : UnDefType.UNDEF);
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
            logger.warn("updateDynamicChannel() shadeId:{} is missing channel:{} => please recreate the thing", shadeId,
                    channelId);
        }
    }

    /**
     * Remove previously statically created channels if the shade does not support them or they are not relevant.
     *
     * @param shade containing the channel data.
     */
    private void updateDynamicChannels(Shade shade) {
        List<Channel> removeChannels = new ArrayList<>();
        updateDynamicChannel(removeChannels, CHANNEL_SHADE_POSITION, hasPrimary());
        updateDynamicChannel(removeChannels, CHANNEL_SHADE_SECONDARY_POSITION, hasSecondary());
        updateDynamicChannel(removeChannels, CHANNEL_SHADE_VANE, hasVane());
        updateDynamicChannel(removeChannels, CHANNEL_SHADE_BATTERY_LEVEL, !shade.isMainsPowered());
        updateDynamicChannel(removeChannels, CHANNEL_SHADE_LOW_BATTERY, !shade.isMainsPowered());
        if (!removeChannels.isEmpty()) {
            if (logger.isDebugEnabled()) {
                StringJoiner joiner = new StringJoiner(", ");
                removeChannels.forEach(c -> joiner.add(c.getUID().getId()));
                logger.debug("updateDynamicChannels() shadeId:{}, removing unsupported channels:{}", shadeId,
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
        thing.setProperties(Stream.of(new String[][] { //
                { HDPowerViewBindingConstants.PROPERTY_NAME, shade.getName() },
                { HDPowerViewBindingConstants.PROPERTY_SHADE_TYPE, getType(shade).toString() },
                { HDPowerViewBindingConstants.PROPERTY_SHADE_CAPABILITIES,
                        Objects.requireNonNull(capabilities).toString() },
                { HDPowerViewBindingConstants.PROPERTY_POWER_TYPE, shade.getPowerType().name().toLowerCase() },
                { HDPowerViewBindingConstants.PROPERTY_BLE_NAME, shade.getBleName() },
                { Thing.PROPERTY_FIRMWARE_VERSION, shade.getFirmware() } //
        }).collect(Collectors.toMap(data -> data[0], data -> data[1])));
    }

    /**
     * Override base method to only update the channel if it actually exists.
     *
     * @param channelID id of the channel, which was updated
     * @param state new state
     */
    @Override
    protected void updateState(String channelID, State state) {
        if (thing.getChannel(channelID) != null) {
            super.updateState(channelID, state);
        }
    }
}
