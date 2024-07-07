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
package org.openhab.binding.argoclima.internal.device.api.protocol;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.configuration.IScheduleConfigurationProvider;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.ActiveTimerModeParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.CurrentTimeParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.CurrentWeekdayParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.DelayMinutesParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.EnumParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.FwVersionParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.IArgoCommandableElement.IArgoElement;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.OnOffParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.RangeParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.TemperatureParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.TimeParam;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.TimeParam.TimeParamType;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.WeekdayParam;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;
import org.openhab.binding.argoclima.internal.device.api.types.FanLevel;
import org.openhab.binding.argoclima.internal.device.api.types.FlapLevel;
import org.openhab.binding.argoclima.internal.device.api.types.OperationMode;
import org.openhab.binding.argoclima.internal.device.api.types.TemperatureScale;
import org.openhab.binding.argoclima.internal.exception.ArgoApiProtocolViolationException;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actual HVAC device status tracked by this binding. Converts to and from Argo protocol messages
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoDeviceStatus implements IArgoSettingProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IScheduleConfigurationProvider scheduleSettingsProvider;

    /**
     * A placeholder value in the protocol indicating no value/null (or no command) carried instead of an actual data
     * element. Useful for allowing to change only a few settings, not the entire state at once
     */
    public static final String NO_VALUE = "N";
    /**
     * Number of data elements carried in a device-side "HMI" update - FROM the device
     *
     * @implNote Not sure what HMI stands for, but is used by Argo for a name for a query param, so adopting this name
     */
    public static final int HMI_UPDATE_ELEMENT_COUNT = 39;
    /**
     * Number of data elements carried in a remote-side status/"HMI" command sent TO the device
     */
    public static final int HMI_COMMAND_ELEMENT_COUNT = 36;
    public static final String HMI_ELEMENT_SEPARATOR = ",";

    /**
     * The actual protocol elements, by their kind, type and read/write indexes in the response
     *
     * @implNote In the future consider applying builder pattern to make it more readable w/o IDE
     */
    private final List<ArgoApiDataElement<IArgoElement>> allElements = List.of(
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.TARGET_TEMPERATURE,
                    new TemperatureParam(this, 19.0, 36.0, 0.5), 0, 0),
            ArgoApiDataElement.readOnlyElement(ArgoDeviceSettingType.ACTUAL_TEMPERATURE,
                    new TemperatureParam(this, 19.0, 36.0, 0.1), 1), // Unfortunately iFeel temperature seems impossible
                                                                     // to be set remotely (needs IR remote)
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.POWER, new OnOffParam(this), 2, 2),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.MODE, new EnumParam<>(this, OperationMode.class),
                    3, 3),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.FAN_LEVEL, new EnumParam<>(this, FanLevel.class),
                    4, 4),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.FLAP_LEVEL,
                    new EnumParam<>(this, FlapLevel.class), 5, 5),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.I_FEEL_TEMPERATURE, new OnOffParam(this), 6, 6),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.FILTER_MODE, new OnOffParam(this), 7, 7),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.ECO_MODE, new OnOffParam(this), 8, 8),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.TURBO_MODE, new OnOffParam(this), 9, 9),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.NIGHT_MODE, new OnOffParam(this), 10, 10),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.LIGHT, new OnOffParam(this), 11, 11),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.ACTIVE_TIMER, new ActiveTimerModeParam(this), 12,
                    12),
            ArgoApiDataElement.writeOnlyElement(ArgoDeviceSettingType.CURRENT_DAY_OF_WEEK,
                    new CurrentWeekdayParam(this), 18),
            ArgoApiDataElement.writeOnlyElement(ArgoDeviceSettingType.TIMER_N_ENABLED_DAYS, new WeekdayParam(this), 19),
            ArgoApiDataElement.writeOnlyElement(ArgoDeviceSettingType.CURRENT_TIME, new CurrentTimeParam(this), 20),
            ArgoApiDataElement.writeOnlyElement(ArgoDeviceSettingType.TIMER_0_DELAY_TIME,
                    new DelayMinutesParam(this, TimeParam.fromHhMm(0, 10), TimeParam.fromHhMm(19, 50), 10,
                            Optional.of(60)),
                    21),
            ArgoApiDataElement.writeOnlyElement(ArgoDeviceSettingType.TIMER_N_ON_TIME,
                    new TimeParam(this, TimeParamType.ON), 22),
            ArgoApiDataElement.writeOnlyElement(ArgoDeviceSettingType.TIMER_N_OFF_TIME,
                    new TimeParam(this, TimeParamType.OFF), 23),
            ArgoApiDataElement.writeOnlyElement(ArgoDeviceSettingType.RESET_TO_FACTORY_SETTINGS, new OnOffParam(this),
                    24),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.ECO_POWER_LIMIT, new RangeParam(this, 30, 99), 22,
                    25),
            ArgoApiDataElement.readWriteElement(ArgoDeviceSettingType.DISPLAY_TEMPERATURE_SCALE,
                    new EnumParam<>(this, TemperatureScale.class), 24, 26),
            ArgoApiDataElement.readOnlyElement(ArgoDeviceSettingType.UNIT_FIRMWARE_VERSION, new FwVersionParam(this),
                    23));

    /**
     * The same elements as in {@link #allElements}, but grouped by kind/type for easier access
     *
     * @implNote Not using {@code Collectors.toMap()} due to possible false-positive(!) unchecked warnings w/ the
     *           accumulator|stream
     */
    private final Map<ArgoDeviceSettingType, ArgoApiDataElement<IArgoElement>> dataElements = allElements.stream()
            .collect(TreeMap::new, (m, v) -> m.put(v.settingType, v), TreeMap::putAll);

    /**
     * C-tor
     *
     * @param scheduleSettingsProvider schedule settings provider
     */
    public ArgoDeviceStatus(IScheduleConfigurationProvider scheduleSettingsProvider) {
        this.scheduleSettingsProvider = scheduleSettingsProvider;
    }

    @Override
    public ArgoApiDataElement<IArgoElement> getSetting(ArgoDeviceSettingType type) {
        if (dataElements.containsKey(type)) {
            return Objects.requireNonNull(dataElements.get(type));
        }
        throw new IllegalArgumentException("Wrong setting type: " + type.toString());
    }

    /**
     * Get the current HVAC state in a SettingKind=CurrentValue compact format
     */
    @Override
    public String toString() {
        return dataElements.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(x -> String.format("%s=%s", x.getKey(), x.getValue().toString(false)))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public IScheduleConfigurationProvider getScheduleProvider() {
        return this.scheduleSettingsProvider;
    }

    /**
     * Get a full current HVAC state in a framework-compatible format
     *
     * @return OH-compatible HVAC state, by element kind
     */
    public Map<ArgoDeviceSettingType, State> getCurrentStateMap() {
        return dataElements.entrySet().stream().sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .filter(x -> x.getValue().isReadable())
                .collect(TreeMap::new, (m, v) -> m.put(v.getKey(), v.getValue().getState()), TreeMap::putAll);
    }

    /**
     * Update *this* state from device-side update
     *
     * @param deviceOutput The device-side 'HMI' update
     * @throws ArgoApiProtocolViolationException If API response doesn't match protocol format
     */
    public void fromDeviceString(String deviceOutput) throws ArgoApiProtocolViolationException {
        var values = Arrays.asList(deviceOutput.split(HMI_ELEMENT_SEPARATOR));
        if (values.size() != HMI_UPDATE_ELEMENT_COUNT) {
            throw new ArgoApiProtocolViolationException(MessageFormat.format(
                    "Invalid device API response: [{0}]. Expected to contain {1} elements while has {2}.", deviceOutput,
                    HMI_UPDATE_ELEMENT_COUNT, values.size()));
        }
        synchronized (this) {
            dataElements.entrySet().stream().forEach(v -> v.getValue().fromDeviceResponse(values));
        }
        logger.trace("Current HVAC state(after update): {}", this.toString());
    }

    /**
     * Convert *this* state to a device-facing command
     * <p>
     * Does NOT represent entire state (to avoid triggering actions which were just due to stale data), but rather sends
     * only pending commands and "static" parts of the protocol, such as current time
     *
     * @implNote The value 'N' in the protocol seems to be for "NULL" (or "no update") and is used as placeholder for
     *           values that are not changing
     * @return The command ready to be sent to the device, effecting *this* state (its withstanding/pending part)
     */
    public String getDeviceCommandStatus() {
        var commands = new ArrayList<String>(
                Objects.requireNonNull(Collections.nCopies(HMI_COMMAND_ELEMENT_COUNT, NO_VALUE)));

        var itemsToSend = dataElements.entrySet().stream().filter(x -> x.getValue().shouldBeSentToDevice()).toList();

        if (logger.isDebugEnabled() || logger.isTraceEnabled()) {
            var stringifiedItemsToSend = itemsToSend.stream().map(x -> x.getKey().toString())
                    .collect(Collectors.joining(", "));
            if (hasUpdatesPending()) {
                logger.debug("Sending {} updates to device {}", itemsToSend.size(), stringifiedItemsToSend);
            } else {
                logger.trace("Sending {} updates to device {}", itemsToSend.size(), stringifiedItemsToSend);
            }
        }

        itemsToSend.stream().map(x -> x.getValue().toDeviceResponse()).forEach(p -> {
            try {
                commands.set(p.orElseThrow().updateIndex(), p.orElseThrow().apiValue());
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException(String.format(
                        "Attempting to set device command %d := %s, while only commands 0..%d are supported",
                        p.orElseThrow().updateIndex(), p.orElseThrow().apiValue(), commands.size()));
            }
        });

        return String.join(HMI_ELEMENT_SEPARATOR, commands);
    }

    /**
     * Check if this state has any updates pending (to be sent or confirmed by the HVAC device)
     * The concrete items with update pending can be retrieved using {@link #getItemsWithPendingUpdates()}
     * <p>
     * The "static" parts of protocol (such as current time) do not count as updates!
     *
     * @return True if there are any updates pending. False - otherwise
     */
    public boolean hasUpdatesPending() {
        return this.dataElements.values().stream().anyMatch(x -> x.isUpdatePending());
    }

    /**
     * Retrieve the elements of this state that have updates pending
     *
     * @return List of items with withstanding updates
     */
    public List<ArgoApiDataElement<IArgoElement>> getItemsWithPendingUpdates() {
        return this.dataElements.values().stream().filter(x -> x.isUpdatePending())
                .sorted((x, y) -> Integer.compare(x.statusUpdateRequestIndex, y.statusUpdateRequestIndex))
                .collect(Collectors.toList());
    }
}
