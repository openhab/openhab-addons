/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.anel.internal.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anel.internal.IAnelConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Get updates for {@link AnelState}s.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelStateUpdater {

    public @Nullable State getChannelUpdate(String channelId, @Nullable AnelState state) {
        if (state == null) {
            return null;
        }

        final int index = IAnelConstants.getIndexFromChannel(channelId);
        if (index >= 0) {
            if (IAnelConstants.CHANNEL_RELAY_NAME.contains(channelId)) {
                return getStringState(state.relayName[index]);
            }
            if (IAnelConstants.CHANNEL_RELAY_STATE.contains(channelId)) {
                return getSwitchState(state.relayState[index]);
            }
            if (IAnelConstants.CHANNEL_RELAY_LOCKED.contains(channelId)) {
                return getSwitchState(state.relayLocked[index]);
            }

            if (IAnelConstants.CHANNEL_IO_NAME.contains(channelId)) {
                return getStringState(state.ioName[index]);
            }
            if (IAnelConstants.CHANNEL_IO_STATE.contains(channelId)) {
                return getSwitchState(state.ioState[index]);
            }
            if (IAnelConstants.CHANNEL_IO_MODE.contains(channelId)) {
                return getSwitchState(state.ioState[index]);
            }
        } else {
            if (IAnelConstants.CHANNEL_NAME.equals(channelId)) {
                return getStringState(state.name);
            }
            if (IAnelConstants.CHANNEL_TEMPERATURE.equals(channelId)) {
                return getTemperatureState(state.temperature);
            }

            if (IAnelConstants.CHANNEL_SENSOR_TEMPERATURE.equals(channelId)) {
                return getTemperatureState(state.sensorTemperature);
            }
            if (IAnelConstants.CHANNEL_SENSOR_HUMIDITY.equals(channelId)) {
                return getDecimalState(state.sensorHumidity);
            }
            if (IAnelConstants.CHANNEL_SENSOR_BRIGHTNESS.equals(channelId)) {
                return getDecimalState(state.sensorBrightness);
            }
        }
        return null;
    }

    public Map<String, State> getChannelUpdates(@Nullable AnelState oldState, AnelState newState) {
        if (oldState != null && newState.status.equals(oldState.status)) {
            return Collections.emptyMap(); // definitely no change!
        }

        final Map<String, State> updates = new HashMap<>();

        // name and device temperature
        final State newName = getNewStringState(oldState == null ? null : oldState.name, newState.name);
        if (newName != null) {
            updates.put(IAnelConstants.CHANNEL_NAME, newName);
        }
        final State newTemperature = getNewTemperatureState(oldState == null ? null : oldState.temperature,
                newState.temperature);
        if (newTemperature != null) {
            updates.put(IAnelConstants.CHANNEL_TEMPERATURE, newTemperature);
        }

        // relay properties
        for (int i = 0; i < 8; i++) {
            final State newRelayName = getNewStringState(oldState == null ? null : oldState.relayName[i],
                    newState.relayName[i]);
            if (newRelayName != null) {
                updates.put(IAnelConstants.CHANNEL_RELAY_NAME.get(i), newRelayName);
            }

            final State newRelayState = getNewSwitchState(oldState == null ? null : oldState.relayState[i],
                    newState.relayState[i]);
            if (newRelayState != null) {
                updates.put(IAnelConstants.CHANNEL_RELAY_STATE.get(i), newRelayState);
            }

            final State newRelayLocked = getNewSwitchState(oldState == null ? null : oldState.relayLocked[i],
                    newState.relayLocked[i]);
            if (newRelayLocked != null) {
                updates.put(IAnelConstants.CHANNEL_RELAY_LOCKED.get(i), newRelayLocked);
            }
        }

        // IO properties
        for (int i = 0; i < 8; i++) {
            final State newIOName = getNewStringState(oldState == null ? null : oldState.ioName[i], newState.ioName[i]);
            if (newIOName != null) {
                updates.put(IAnelConstants.CHANNEL_IO_NAME.get(i), newIOName);
            }

            final State newIOIsInput = getNewSwitchState(oldState == null ? null : oldState.ioIsInput[i],
                    newState.ioIsInput[i]);
            if (newIOIsInput != null) {
                updates.put(IAnelConstants.CHANNEL_IO_MODE.get(i), newIOIsInput);
            }

            final State newIOState = getNewSwitchState(oldState == null ? null : oldState.ioState[i],
                    newState.ioState[i]);
            if (newIOState != null) {
                updates.put(IAnelConstants.CHANNEL_IO_STATE.get(i), newIOState);
            }
        }

        // sensor values
        final State newSensorTemperature = getNewTemperatureState(oldState == null ? null : oldState.sensorTemperature,
                newState.sensorTemperature);
        if (newSensorTemperature != null) {
            updates.put(IAnelConstants.CHANNEL_SENSOR_TEMPERATURE, newSensorTemperature);
        }
        final State newSensorHumidity = getNewDecimalState(oldState == null ? null : oldState.sensorHumidity,
                newState.sensorHumidity);
        if (newSensorHumidity != null) {
            updates.put(IAnelConstants.CHANNEL_SENSOR_HUMIDITY, newSensorHumidity);
        }
        final State newSensorBrightness = getNewDecimalState(oldState == null ? null : oldState.sensorBrightness,
                newState.sensorBrightness);
        if (newSensorBrightness != null) {
            updates.put(IAnelConstants.CHANNEL_SENSOR_BRIGHTNESS, newSensorBrightness);
        }

        return updates;
    }

    private @Nullable State getStringState(@Nullable String value) {
        return value == null ? null : new StringType(value);
    }

    private @Nullable State getDecimalState(@Nullable String value) {
        return value == null ? null : new DecimalType(value);
    }

    private @Nullable State getTemperatureState(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        final float floatValue = Float.parseFloat(value);
        return QuantityType.valueOf(floatValue, SIUnits.CELSIUS);
    }

    private @Nullable State getSwitchState(@Nullable Boolean value) {
        return value == null ? null : OnOffType.from(value.booleanValue());
    }

    private @Nullable State getNewStringState(@Nullable String oldValue, @Nullable String newValue) {
        return getNewState(oldValue, newValue, StringType::new);
    }

    private @Nullable State getNewDecimalState(@Nullable String oldValue, @Nullable String newValue) {
        return getNewState(oldValue, newValue, DecimalType::new);
    }

    private @Nullable State getNewTemperatureState(@Nullable String oldValue, @Nullable String newValue) {
        return getNewState(oldValue, newValue, value -> QuantityType.valueOf(Float.parseFloat(value), SIUnits.CELSIUS));
    }

    private @Nullable State getNewSwitchState(@Nullable Boolean oldValue, @Nullable Boolean newValue) {
        return getNewState(oldValue, newValue, value -> OnOffType.from(value.booleanValue()));
    }

    private <T> @Nullable State getNewState(@Nullable T oldValue, @Nullable T newValue,
            Function<T, State> createState) {
        if (oldValue == null) {
            if (newValue == null) {
                return null; // no change
            } else {
                return createState.apply(newValue); // from null to some value
            }
        } else if (newValue == null) {
            return UnDefType.NULL; // from some value to null
        } else if (oldValue.equals(newValue)) {
            return null; // no change
        }
        return createState.apply(newValue); // from some value to another value
    }
}
