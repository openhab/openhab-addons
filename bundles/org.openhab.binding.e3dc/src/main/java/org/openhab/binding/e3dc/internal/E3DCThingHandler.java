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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
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
                RscpValue value = findValue(response, channel.getValue(), getDeviceIndex());
                if (value != null) {
                    updateChannel(group.getKey(), channel.getKey(), channel.getValue(), value);
                }
            }
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * The value of one channel in a poll response.
     *
     * @param response the answer of the device
     * @param spec the channel specification
     * @param deviceIndex the index of this thing's device, or {@code null} when the container holds a single
     *            entry
     */
    static @Nullable RscpValue findValue(RscpValue response, E3DCChannelSpec spec, @Nullable Integer deviceIndex) {
        RscpValue scope = response;
        if (spec.container() != 0) {
            scope = selectContainer(response, spec.container(), deviceIndex);
            if (scope == null) {
                return null;
            }
        }
        if (spec.nestedTag() != 0) {
            // values of the history data sit in a sum container inside the period container, and the values of
            // the inverter in a container of their own, which the trackers report once per tracker
            List<RscpValue> nested = scope.findAll(spec.nestedTag());
            if (nested.isEmpty()) {
                return null;
            }
            if (spec.valueIndex() >= 0) {
                return spec.valueIndex() < nested.size() ? nested.get(spec.valueIndex()).find(spec.tag()) : null;
            }
            scope = nested.get(0);
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
    private static @Nullable RscpValue selectContainer(RscpValue response, int container,
            @Nullable Integer deviceIndex) {
        List<RscpValue> candidates = response.findAll(container);
        if (candidates.isEmpty()) {
            return null;
        }
        if (deviceIndex == null) {
            return candidates.get(0);
        }
        int indexTag = indexTagFor(container);
        for (RscpValue candidate : candidates) {
            Number reported = candidate.findNumber(indexTag);
            if (reported != null && reported.intValue() == deviceIndex) {
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
    private static int indexTagFor(int container) {
        if (container == RscpTag.PM_DATA) {
            return RscpTag.PM_INDEX;
        }
        if (container == RscpTag.BAT_DATA) {
            return RscpTag.BAT_INDEX;
        }
        if (container == RscpTag.BAT_DCB_INFO) {
            return RscpTag.BAT_DCB_INDEX;
        }
        return RscpTag.WB_INDEX;
    }

    void updateChannel(String groupId, String channelId, E3DCChannelSpec spec, RscpValue value) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), groupId + "#" + channelId);
        if (spec.isBitField()) {
            Integer raw = readInt(value, spec);
            if (raw != null) {
                updateState(channelUID, (raw & spec.bitMask()) != 0 ? OnOffType.ON : OnOffType.OFF);
            }
            return;
        }
        Channel channel = getThing().getChannel(channelUID);
        boolean textChannel = channel != null && "String".equals(channel.getAcceptedItemType());
        State state = valueState(value, spec, textChannel);
        if (state != null) {
            updateState(channelUID, state);
            if (spec.container() == RscpTag.DB_HISTORY_DATA_YEAR) {
                sendYearValue(channelUID, state);
            }
        }
    }

    /**
     * Publishes the value of the current year as one entry of a time series, stamped with the start of the
     * year.
     *
     * <p>
     * The year channels are a time series of yearly values. The state of the channel is the current year;
     * the entry makes the same value available to persistence under the year it belongs to, so the
     * years add up to one series. An entry that already exists for the year is replaced, as the value of
     * the running year grows.
     */
    private void sendYearValue(ChannelUID channelUID, State state) {
        LocalDate yearStart = LocalDate.now(ZoneOffset.UTC).withDayOfYear(1);
        TimeSeries series = new TimeSeries(TimeSeries.Policy.REPLACE);
        series.add(yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant(), state);
        sendTimeSeries(channelUID, series);
    }

    /**
     * The state a value of this interface publishes on its channel, or {@code null} when it carries none.
     *
     * <p>
     * Most values are numbers, but the interface also reports plain booleans, for example the charging mode of
     * a wallbox. Treated by their kind alone they would silently keep the channel at its previous state.
     */
    static @Nullable State valueState(RscpValue value, E3DCChannelSpec spec) {
        return valueState(value, spec, false);
    }

    /**
     * The state of a value for a channel that is either numeric or text.
     *
     * <p>
     * A text channel must receive text even when the device reports a number, such as the version of a battery
     * module: a number sent to a String item is converted by the framework to a date, which shows as a time
     * in 1970. Text the device reports arrives as bytes, which are decoded here.
     *
     * @param textChannel whether the channel carries a String item
     */
    static @Nullable State valueState(RscpValue value, E3DCChannelSpec spec, boolean textChannel) {
        Object raw = value.getValue();
        if (raw instanceof byte[] bytes && value.getType() == RscpType.CSTRING) {
            return new StringType(new String(bytes, StandardCharsets.UTF_8).replace("\0", "").trim());
        }
        if (raw instanceof Number number && textChannel) {
            double scaled = spec.scale(number.doubleValue());
            return new StringType(scaled == Math.rint(scaled) && Math.abs(scaled) < 1e15 ? Long.toString((long) scaled)
                    : Double.toString(scaled));
        }
        if (raw instanceof Number number) {
            return new DecimalType(spec.scale(number.doubleValue()));
        }
        if (raw instanceof Boolean flag) {
            return flag ? OnOffType.ON : OnOffType.OFF;
        }
        // other kinds, for example byte arrays not mapped to a bit field, carry no publishable value
        return raw instanceof String string ? new StringType(string) : null;
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
        if (raw instanceof Boolean flag) {
            return flag ? 1 : 0;
        }
        return raw instanceof Number number ? number.intValue() : null;
    }

    /**
     * Publishes capacities in kWh. The device reports them in Ah, so each is multiplied by the voltage
     * reported in the same response.
     *
     * @param response the poll response
     * @param groupId the channel group holding the capacity, voltage and energy channels
     * @param voltageChannel the channel the voltage is read from
     * @param sources the energy channels, each with the capacity channel it is calculated from
     */
    protected void updateEnergies(RscpValue response, String groupId, String voltageChannel,
            Map<String, String> sources) {
        Double volts = readValue(response, groupId, voltageChannel);
        if (volts == null || volts <= 0) {
            return;
        }
        for (Map.Entry<String, String> energy : sources.entrySet()) {
            Double kilowattHours = energyKwh(readValue(response, groupId, energy.getValue()), volts);
            if (kilowattHours != null) {
                updateState(new ChannelUID(getThing().getUID(), groupId + "#" + energy.getKey()),
                        new QuantityType<>(kilowattHours, Units.KILOWATT_HOUR));
            }
        }
    }

    private @Nullable Double readValue(RscpValue response, String groupId, String channelId) {
        E3DCChannelSpec spec = getRegistry().spec(groupId, channelId);
        if (spec == null) {
            return null;
        }
        RscpValue value = findValue(response, spec, getDeviceIndex());
        return value != null && value.getValue() instanceof Number number ? spec.scale(number.doubleValue()) : null;
    }

    /**
     * The energy of a battery capacity at a given voltage.
     *
     * @param ampereHours the capacity in Ah, or {@code null} when the device did not report it
     * @param volts the voltage in V
     * @return the energy in kWh, or {@code null} when there is no capacity
     */
    static @Nullable Double energyKwh(@Nullable Double ampereHours, double volts) {
        return ampereHours == null ? null : ampereHours * volts / 1000.0;
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
        E3DCCommandSpec spec = findSpec(commands, channelUID);
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
        List<RscpValue> request = commandHandler.buildRequest(spec, command, index != null ? index : 0);
        if (value == null || request == null) {
            logger.warn("Command {} is not valid for channel {} and was not sent", command, channelUID);
            return;
        }
        // the channel state is only updated once the device acknowledged the write
        if (commandHandler.send(bridge, request)) {
            State state = stateFor(value, spec);
            if (state != null) {
                updateState(channelUID, state);
            }
        }
    }

    /**
     * The item state mirroring the value that was written.
     *
     * <p>
     * A switch is sent as a number on this interface, so its channel has to be updated with a state the item
     * accepts: a {@code DecimalType} on a Switch item is ignored by the framework.
     */
    private @Nullable State stateFor(RscpValue value, E3DCCommandSpec spec) {
        Object raw = value.getValue();
        if (raw instanceof Boolean flag) {
            return flag ? OnOffType.ON : OnOffType.OFF;
        }
        if (spec.switchValue() && raw instanceof Number number) {
            return number.intValue() != 0 ? OnOffType.ON : OnOffType.OFF;
        }
        return raw instanceof Number number ? new DecimalType(number.doubleValue()) : null;
    }

    /**
     * The command specification of a channel, or {@code null} when the channel cannot be written.
     *
     * <p>
     * The registry is keyed by the id of the channel inside its group, while the id of a channel UID
     * carries the group as well, so the lookup has to use the id without the group.
     */
    static @Nullable E3DCCommandSpec findSpec(E3DCCommandRegistry commands, ChannelUID channelUID) {
        String groupId = channelUID.getGroupId();
        return groupId != null ? commands.spec(groupId, channelUID.getIdWithoutGroup()) : null;
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
