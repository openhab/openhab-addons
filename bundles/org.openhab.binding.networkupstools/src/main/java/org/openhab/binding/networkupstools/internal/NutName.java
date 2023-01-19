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
package org.openhab.binding.networkupstools.internal;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Supported NUT variables. Any NUT enum members have a complimentary channel definition in the XML thing definition.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @see https://github.com/networkupstools/nut/blob/master/docs/nut-names.txt
 */
@NonNullByDefault
enum NutName {
    // UPS
    UPS_ALARM("upsAlarm", "ups.alarm", StringType.class),
    UPS_LOAD("upsLoad", "ups.load", Units.PERCENT),
    UPS_POWER("upsPower", "ups.power", Units.VOLT_AMPERE),
    UPS_REALPOWER("upsRealpower", "ups.realpower", Units.WATT),
    UPS_STATUS("upsStatus", "ups.status", StringType.class),
    UPS_TEMPERATURE("upsTemperature", "ups.temperature", SIUnits.CELSIUS),
    UPS_TEST_RESULT("upsTestResult", "ups.test.result", StringType.class),

    // Input
    INPUT_CURRENT("inputCurrent", "input.current", Units.AMPERE),
    INPUT_CURRENT_STATUS("inputCurrentStatus", "input.current.status", StringType.class),
    INPUT_LOAD("inputLoad", "input.load", Units.PERCENT),
    INPUT_REALPOWER("inputRealpower", "input.realpower", Units.WATT),
    INPUT_QUALITY("inputQuality", "input.quality", StringType.class),
    INPUT_TRANSFER_REASON("inputTransferReason", "input.transfer.reason", StringType.class),
    INPUT_VOLTAGE("inputVoltage", "input.voltage", Units.VOLT),
    INPUT_VOLTAGE_STATUS("inputVoltageStatus", "input.voltage.status", StringType.class),

    // Output
    OUTPUT_CURRENT("outputCurrent", "output.current", Units.AMPERE),
    OUTPUT_VOLTAGE("outputVoltage", "output.voltage", Units.VOLT),

    // Battery
    BATTERY_CHARGE("batteryCharge", "battery.charge", Units.PERCENT),
    BATTERY_RUNTIME("batteryRuntime", "battery.runtime", Units.SECOND),
    BATTERY_VOLTAGE("batteryVoltage", "battery.voltage", Units.VOLT);

    static final Map<String, NutName> NUT_NAME_MAP = Stream.of(NutName.values())
            .collect(Collectors.toMap(NutName::getChannelId, Function.identity()));

    private final String channelId;
    private final String name;
    private final Class<? extends State> stateClass;
    // unit only as a value if using a QuantityType.
    private final @NonNullByDefault({}) Unit<?> unit;

    NutName(final String channelId, final String name, final Class<? extends State> stateClass) {
        this(channelId, name, stateClass, null);
    }

    NutName(final String channelId, final String name, final Unit<?> unit) {
        this(channelId, name, QuantityType.class, unit);
    }

    NutName(final String channelId, final String name, final Class<? extends State> stateClass,
            final @Nullable Unit<?> unit) {
        this.channelId = channelId;
        this.name = name;
        this.stateClass = stateClass;
        this.unit = unit;
    }

    /**
     * Returns the NUT enum for the given channel id or null if there is no NUT enum available for the given channel.
     *
     * @param channelId Channel to find the NUT enum for
     * @return The NUT enum or null if there is none.
     */
    public static @Nullable NutName channelIdToNutName(final String channelId) {
        return NUT_NAME_MAP.get(channelId);
    }

    /**
     * Returns the {@link State} value of the variable for this NUT as is found in the given map of variables.
     *
     * @param channelId
     * @param variables Map of variables that contain a value for this NUT (or doesn't contain it if not available)
     * @return The {@link State} value or UNDEF if not available in the variables map or if it can't be determined.
     */
    public static State toState(final String channelId, final Map<String, String> variables) {
        final NutName nutName = channelIdToNutName(channelId);

        if (nutName instanceof NutName) {
            return nutName.toState(variables);
        } else {
            throw new IllegalArgumentException("Channel name '" + channelId + "'is not a known data name");
        }
    }

    /**
     * Returns the {@link State} value of the variable for this NUT as is found in the given map of variables.
     *
     * @param variables Map of variables that contain a value for this NUT (or doesn't contain it if not available)
     * @return The {@link State} value or UNDEF if not available in the variables map or if it can't be determined.
     */
    public State toState(final @Nullable Map<String, String> variables) {
        final State state;
        final String value = variables == null ? null : variables.get(name);

        if (value == null) {
            state = UnDefType.UNDEF;
        } else {
            if (stateClass == StringType.class) {
                state = StringType.valueOf(value);
            } else if (stateClass == DecimalType.class) {
                state = DecimalType.valueOf(value);
            } else if (stateClass == PercentType.class) {
                state = PercentType.valueOf(value);
            } else if (stateClass == QuantityType.class) {
                state = QuantityType.valueOf(Double.valueOf(value), unit);
            } else {
                state = UnDefType.UNDEF;
            }
        }
        return state;
    }

    /**
     * @return The name of the Channel for this NUT variable as specified in the Thing
     */
    String getChannelId() {
        return channelId;
    }

    /**
     * @return The variable name as used by the NUT server
     */
    String getName() {
        return name;
    }

    /**
     * @return The {@link State} class type of this NUT variable
     */
    Class<? extends State> getStateType() {
        return stateClass;
    }

    /**
     * @return The {@link Unit} for this NUT variable if the type is a {@link QuantityType}
     */
    Unit<?> getUnit() {
        return unit;
    }
}
