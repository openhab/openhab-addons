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
package org.openhab.binding.e3dc.internal.handler;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelRegistry;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCCommandRegistry;
import org.openhab.binding.e3dc.internal.E3DCCommandSpec;
import org.openhab.binding.e3dc.internal.E3DCDataListener;
import org.openhab.binding.e3dc.internal.E3DCIdlePeriod;
import org.openhab.binding.e3dc.internal.E3DCMessages;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the things that consume poll data from the bridge.
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

    /** The index of this thing's device in a container that holds several, or {@code null} for a single device. */
    protected @Nullable Integer getDeviceIndex() {
        return null;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "The bridge is not available");
            return;
        }
        bridge.registerDataListener(this);
        if (bridge.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
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

    /** The value of one channel in a poll response, or {@code null} when this answer does not carry it. */
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
                return findIndexed(nested, spec);
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
     * Picks the container of an inverter value that exists once per phase, tracker or sensor by its index.
     */
    private static @Nullable RscpValue findIndexed(List<RscpValue> containers, E3DCChannelSpec spec) {
        boolean indexed = false;
        for (RscpValue container : containers) {
            Number reported = container.findNumber(RscpTag.PVI_INDEX);
            if (reported != null) {
                indexed = true;
                if (reported.intValue() == spec.valueIndex()) {
                    return container.find(spec.tag());
                }
            }
        }
        if (indexed) {
            return null;
        }
        return spec.valueIndex() < containers.size() ? containers.get(spec.valueIndex()).find(spec.tag()) : null;
    }

    /** Picks the container instance belonging to this thing's device index. */
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
        boolean indexed = false;
        for (RscpValue candidate : candidates) {
            Number reported = candidate.findNumber(indexTag);
            if (reported != null) {
                indexed = true;
                if (reported.intValue() == deviceIndex) {
                    return candidate;
                }
            }
        }
        // another device's values must never stand in for the requested one; only a response that carries
        // no index at all is told apart by position
        return indexed ? null : candidates.get(0);
    }

    /** The tag that carries the index of a container that holds several devices. */
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
        if (channel != null && "DateTime".equals(channel.getAcceptedItemType())) {
            State moment = timestampState(value);
            if (moment != null) {
                updateState(channelUID, moment);
            }
            return;
        }
        boolean textChannel = channel != null && "String".equals(channel.getAcceptedItemType());
        State state = spec.tag() == RscpTag.BAT_AVAILABLE_BATTERIES && value.getType() == RscpType.CONTAINER
                ? new DecimalType(value.findAll(RscpTag.BAT_INDEX).size())
                : valueState(value, spec, textChannel);
        state = withUnit(state, channel == null ? null : channel.getAcceptedItemType(), spec);
        if (state != null) {
            updateState(channelUID, state);
            if (spec.container() == RscpTag.DB_HISTORY_DATA_YEAR) {
                sendYearValue(channelUID, state);
            }
        }
    }

    /**
     * Publishes values of the poll that describe the device as thing properties instead of channels. A value
     * that the answer does not carry leaves its property as it is.
     */
    protected void updateProperties(RscpValue response, Map<String, E3DCChannelSpec> properties) {
        for (Map.Entry<String, E3DCChannelSpec> property : properties.entrySet()) {
            RscpValue value = findValue(response, property.getValue(), getDeviceIndex());
            if (value != null) {
                State state = valueState(value, property.getValue(), true);
                if (state != null) {
                    setProperty(property.getKey(), state.toString());
                }
            }
        }
    }

    /** Publishes numeric values of the poll as thing properties, each followed by the unit it is stated in. */
    protected void updateProperties(RscpValue response, Map<String, E3DCChannelSpec> properties, String unit) {
        for (Map.Entry<String, E3DCChannelSpec> property : properties.entrySet()) {
            RscpValue value = findValue(response, property.getValue(), getDeviceIndex());
            Number number = value != null ? value.asNumber() : null;
            if (number != null) {
                setProperty(property.getKey(), quantity(number, unit));
            }
        }
    }

    /** A measured number as text, without a decimal part it does not need. */
    static String quantity(Number value, String unit) {
        double number = value.doubleValue();
        String text = number == Math.rint(number) && Math.abs(number) < 1e15 ? Long.toString((long) number)
                : Double.toString(number);
        return unit.isEmpty() ? text : text + " " + unit;
    }

    /** Updates a thing property only when its value changed, so a poll does not rewrite it every cycle. */
    protected void setProperty(String property, @Nullable String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!value.equals(getThing().getProperties().get(property))) {
            updateProperty(property, value);
        }
    }

    /**
     * The state with the unit of its channel. A measurement has to be published as a quantity: a plain number is
     * taken as being in the unit of the item, which shows a device reading of 2000 W as 2000 kW on a kW item. The
     * energy counters follow the scaling the device needs: reported in Wh, divided by a thousand is published in kWh.
     */
    static @Nullable State withUnit(@Nullable State state, @Nullable String acceptedItemType, E3DCChannelSpec spec) {
        if (!(state instanceof DecimalType decimal) || acceptedItemType == null) {
            return state;
        }
        Unit<?> unit = unitOf(acceptedItemType, spec);
        return unit != null ? new QuantityType<>(decimal.toBigDecimal(), unit) : state;
    }

    private static @Nullable Unit<?> unitOf(String acceptedItemType, E3DCChannelSpec spec) {
        return switch (acceptedItemType) {
            case "Number:Power" -> Units.WATT;
            case "Number:Energy" -> spec.divisor() == 1000 ? Units.KILOWATT_HOUR : Units.WATT_HOUR;
            case "Number:ElectricPotential" -> Units.VOLT;
            case "Number:ElectricCurrent" -> Units.AMPERE;
            case "Number:Temperature" -> SIUnits.CELSIUS;
            case "Number:Frequency" -> Units.HERTZ;
            case "Number:Dimensionless" -> Units.PERCENT;
            default -> null;
        };
    }

    /**
     * Publishes the yearly value as a time series entry stamped with the start of the year, so persistence holds
     * it under the year it belongs to. The entry is replaced as the running year grows.
     */
    private void sendYearValue(ChannelUID channelUID, State state) {
        LocalDate yearStart = LocalDate.now(ZoneOffset.UTC).withDayOfYear(1);
        TimeSeries series = new TimeSeries(TimeSeries.Policy.REPLACE);
        series.add(yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant(), state);
        sendTimeSeries(channelUID, series);
    }

    /**
     * The state of a moment value. A plain number is taken as seconds, or as milliseconds when it is too large;
     * a moment of zero means nothing happened yet and is undefined. {@code null} means no moment at all.
     */
    static @Nullable State timestampState(RscpValue value) {
        Object raw = value.getValue();
        if (raw instanceof Instant moment) {
            return moment.getEpochSecond() <= 0 && moment.getNano() == 0 ? UnDefType.UNDEF
                    : new DateTimeType(moment.atZone(ZoneId.systemDefault()));
        }
        if (raw instanceof Number number) {
            long seconds = number.longValue();
            if (seconds <= 0) {
                return UnDefType.UNDEF;
            }
            Instant moment = seconds > MILLISECONDS_THRESHOLD ? Instant.ofEpochMilli(seconds)
                    : Instant.ofEpochSecond(seconds);
            return new DateTimeType(moment.atZone(ZoneId.systemDefault()));
        }
        return null;
    }

    /** Seconds since 1970 stay below this until the year 5138, so a larger number counts milliseconds. */
    private static final long MILLISECONDS_THRESHOLD = 100_000_000_000L;

    /** The state a value publishes on its channel, or {@code null} when it carries none. */
    static @Nullable State valueState(RscpValue value, E3DCChannelSpec spec) {
        return valueState(value, spec, false);
    }

    /**
     * The state of a value for a channel that is either numeric or text. A text channel needs text even for a
     * number, because the framework converts a number on a String item to a date in 1970.
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
        return raw instanceof String string ? new StringType(string) : null;
    }

    /** Publishes the channels that render a list instead of a single value. */
    private void updateLists(RscpValue response) {
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : getRegistry().groups().entrySet()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                if (channel.getValue().isList()) {
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(),
                            group.getKey() + "#" + channel.getKey());
                    updateListChannel(channelUID, channel.getKey(), channel.getValue(), response);
                }
            }
        }
    }

    /**
     * Publishes one channel that renders a list. A device may report one list that feeds several channels,
     * so a handler tells the channels apart by their id.
     */
    protected void updateListChannel(ChannelUID channelUID, String channelId, E3DCChannelSpec spec,
            RscpValue response) {
        if (spec.tag() == RscpTag.EMS_STORED_ERRORS) {
            updateMessages(channelUID, channelId, response);
        } else {
            updateState(channelUID, new StringType(E3DCIdlePeriod.toTextFromResponse(response)));
        }
    }

    /** Publishes the messages of the energy manager; a poll without an answer leaves both channels unchanged. */
    private void updateMessages(ChannelUID channelUID, String channelId, RscpValue response) {
        List<E3DCMessages.Message> messages = E3DCMessages.fromResponse(response);
        if (messages == null) {
            return;
        }
        if (E3DCBindingConstants.CHANNEL_HAS_MESSAGES.equals(channelId)) {
            updateState(channelUID, OnOffType.from(!messages.isEmpty()));
        } else {
            updateState(channelUID, new StringType(E3DCMessages.toJson(messages)));
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
     * Publishes the capacities in kWh: the device reports them in Ah, so each is multiplied by the voltage
     * reported in the same response.
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

    /** The energy of a battery capacity at a given voltage. */
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
            logger.warn("Channel {} is not writable", channelUID);
            return;
        }
        E3DCBridgeHandler bridge = getBridgeHandler();
        if (bridge == null) {
            logger.debug("Cannot send a command without a bridge, channel {}", channelUID);
            return;
        }
        Integer index = getDeviceIndex();
        RscpValue value = spec.toValue(command);
        if (value == null) {
            logger.warn("Command {} is not valid for channel {} and was not sent", command, channelUID);
            return;
        }
        // the channel state is only updated once the device acknowledged the write
        if (commandHandler.execute(bridge, spec, value, index != null ? index : 0)) {
            State state = publishesWrittenState(channelUID) ? stateFor(value, spec) : null;
            if (state != null) {
                updateState(channelUID, state);
            }
        }
    }

    /**
     * The item state mirroring the written value. A switch is sent as a number on this interface, so its channel
     * needs a state the item accepts: a {@code DecimalType} on a Switch item is ignored by the framework.
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
     * The command specification of a channel, or {@code null} when the channel cannot be written. The registry is
     * keyed by the id inside the group, while the channel UID id carries the group as well.
     */
    static @Nullable E3DCCommandSpec findSpec(E3DCCommandRegistry commands, ChannelUID channelUID) {
        String groupId = channelUID.getGroupId();
        return groupId != null ? commands.spec(groupId, channelUID.getIdWithoutGroup()) : null;
    }

    /** Whether the value written to a channel is published as its state, which a command only channel does not do. */
    protected boolean publishesWrittenState(ChannelUID channelUID) {
        return true;
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
