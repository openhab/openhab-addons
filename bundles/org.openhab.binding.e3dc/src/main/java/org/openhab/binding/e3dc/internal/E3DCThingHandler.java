/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the things that consume poll data from the bridge.
 *
 * <p>
 * Subclasses only provide the channel registry. This class extracts each channel value from the poll
 * response according to its {@link E3DCChannelSpec} and updates the channel.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public abstract class E3DCThingHandler extends BaseThingHandler implements E3DCDataListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final E3DCCommandHandler commandHandler = new E3DCCommandHandler();

    protected E3DCThingHandler(Thing thing) {
        super(thing);
    }

    /** The channels this handler publishes, grouped by channel group. */
    protected abstract E3DCChannelRegistry getRegistry();

    /**
     * The index of this thing's device within a container that holds several, or `null` when the
     * container holds a single entry.
     */
    protected @Nullable Integer getDeviceIndex() {
        return null;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "The bridge is not available");
            return;
        }
        bridge.registerDataListener(this);
        updateStatus(bridge.isConnected() ? ThingStatus.ONLINE : ThingStatus.OFFLINE,
                bridge.isConnected() ? ThingStatusDetail.NONE : ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Override
    public void dispose() {
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge != null) {
            bridge.unregisterDataListener(this);
        }
        super.dispose();
    }

    @Override
    public void onData(RscpValue response) {
        onPowerSettings(response);
        updateLists(response);
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : getRegistry().groups().entrySet()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                RscpValue value = findValue(response, channel.getValue());
                if (value != null) {
                    updateChannel(group.getKey(), channel.getKey(), channel.getValue(), value);
                }
            }
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private @Nullable RscpValue findValue(RscpValue response, E3DCChannelSpec spec) {
        RscpValue scope = response;
        if (spec.container() != 0) {
            scope = selectContainer(response, spec.container());
            if (scope == null) {
                return null;
            }
        }
        if (spec.nestedTag() != 0) {
            // values of the history data sit in a sum container inside the period container
            List<RscpValue> sums = scope.findAll(spec.nestedTag());
            if (sums.isEmpty()) {
                return null;
            }
            scope = sums.get(0);
        }
        if (spec.valueIndex() >= 0) {
            // the container reports the value once per device, for example once per photovoltaic tracker
            List<RscpValue> occurrences = scope.findAll(spec.tag());
            return spec.valueIndex() < occurrences.size() ? occurrences.get(spec.valueIndex()) : null;
        }
        return scope.find(spec.tag());
    }

    /**
     * Picks the container instance belonging to this thing's device index. Devices that can exist more
     * than once are returned as several containers, each carrying its own index.
     */
    private @Nullable RscpValue selectContainer(RscpValue response, int container) {
        List<RscpValue> candidates = response.findAll(container);
        if (candidates.isEmpty()) {
            return null;
        }
        Integer wanted = getDeviceIndex();
        if (wanted == null) {
            return candidates.get(0);
        }
        int indexTag = indexTagFor(container);
        for (RscpValue candidate : candidates) {
            Number reported = candidate.findNumber(indexTag);
            if (reported != null && reported.intValue() == wanted) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    /**
     * The tag that carries the index of a container that holds several devices.
     *
     * <p>
     * The cell modules of a battery are addressed inside their own container rather than by position,
     * so the index tag belongs to the module container itself.
     */
    private int indexTagFor(int container) {
        if (container == RscpTag.PM_DATA) {
            return RscpTag.PM_INDEX;
        }
        if (container == RscpTag.BAT_DCB_INFO) {
            return RscpTag.BAT_DCB_INDEX;
        }
        return RscpTag.WB_INDEX;
    }

    private void updateChannel(String groupId, String channelId, E3DCChannelSpec spec, RscpValue value) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), groupId + "#" + channelId);
        if (spec.isBitField()) {
            Integer raw = readInt(value, spec);
            if (raw != null) {
                updateState(channelUID, (raw & spec.bitMask()) != 0 ? OnOffType.ON : OnOffType.OFF);
            }
            return;
        }
        Object raw = value.getValue();
        if (raw instanceof Number number) {
            updateState(channelUID, new DecimalType(spec.scale(number.doubleValue())));
        } else if (raw instanceof String string) {
            updateState(channelUID, new StringType(string));
        }
        // other types, for example byte arrays not mapped to a bit field, carry no publishable value
    }

    /**
     * Publishes the channels that render a list instead of a single value. The list is taken from the
     * response the handler currently holds, so it follows the poll cycle like the other channels.
     */
    private void updateLists(RscpValue response) {
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : getRegistry().groups().entrySet()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                if (channel.getValue().isList()) {
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(),
                            group.getKey() + "#" + channel.getKey());
                    updateState(channelUID, new StringType(E3DCIdlePeriod.toTextFromResponse(response)));
                }
            }
        }
    }

    private @Nullable Integer readInt(RscpValue value, E3DCChannelSpec spec) {
        Object raw = value.getValue();
        if (spec.byteIndex() >= 0) {
            if (raw instanceof byte[] bytes && spec.byteIndex() < bytes.length) {
                return bytes[spec.byteIndex()] & 0xFF;
            }
            return null;
        }
        return raw instanceof Number number ? number.intValue() : null;
    }

    protected @Nullable E3DCBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof E3DCBridgeHandler handler) {
            return handler;
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // values arrive with the poll cycle, so a refresh asks the bridge for one instead of
            // publishing a value of its own
            E3DCBridgeHandler bridge = getBridgeHandler();
            if (bridge != null) {
                bridge.requestRefresh();
            } else {
                logger.debug("Cannot refresh channel {} without a bridge", channelUID);
            }
            return;
        }
        E3DCCommandRegistry commands = getCommandRegistry();
        String groupId = channelUID.getGroupId();
        if (commands == null || groupId == null) {
            return;
        }
        E3DCCommandSpec spec = commands.spec(groupId, channelUID.getId());
        if (spec == null) {
            logger.debug("Channel {} is not writable", channelUID);
            return;
        }
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null) {
            logger.debug("Cannot send a command without a bridge, channel {}", channelUID);
            return;
        }
        Integer index = getDeviceIndex();
        RscpValue value = spec.toValue(command);
        RscpValue request = commandHandler.buildRequest(spec, command, index != null ? index : 0);
        if (value == null || request == null) {
            logger.debug("Command {} is not valid for channel {}", command, channelUID);
            return;
        }
        // the channel state is only updated once the device acknowledged the write
        if (commandHandler.send(bridge, request)) {
            State state = stateFor(value);
            if (state != null) {
                updateState(channelUID, state);
            }
        }
    }

    /** The item state mirroring the value that was written. */
    private @Nullable State stateFor(RscpValue value) {
        Object raw = value.getValue();
        if (raw instanceof Boolean flag) {
            return flag ? OnOffType.ON : OnOffType.OFF;
        }
        return raw instanceof Number number ? new DecimalType(number.doubleValue()) : null;
    }

    /** The writable channels of this thing, or {@code null} when it has none. */
    protected @Nullable E3DCCommandRegistry getCommandRegistry() {
        return null;
    }

    /** Applies power settings received from the device to the command snapshot. */
    protected void onPowerSettings(RscpValue response) {
        commandHandler.updateFromResponse(response);
    }
}
