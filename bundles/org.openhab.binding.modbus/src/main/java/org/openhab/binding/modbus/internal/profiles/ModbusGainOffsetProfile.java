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
package org.openhab.binding.modbus.internal.profiles;

import java.math.BigDecimal;
import java.util.Optional;

import javax.measure.UnconvertibleException;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile for applying gain and offset to values.
 *
 * Output of the profile is
 * - (incoming value + pre-gain-offset) * gain (update towards item)
 * - (incoming value / gain) - pre-gain-offset (command from item)
 *
 * Gain can also specify unit of the result, converting otherwise bare numbers to ones with quantity.
 *
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusGainOffsetProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(ModbusGainOffsetProfile.class);
    private static final String PREGAIN_OFFSET_PARAM = "pre-gain-offset";
    private static final String GAIN_PARAM = "gain";

    private final ProfileCallback callback;
    private final ProfileContext context;

    private Optional<QuantityType<?>> pregainOffset;
    private Optional<QuantityType<?>> gain;

    public ModbusGainOffsetProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.context = context;
        {
            Object rawOffsetValue = orDefault("0", this.context.getConfiguration().get(PREGAIN_OFFSET_PARAM));
            logger.debug("Configuring profile with {} parameter '{}'", PREGAIN_OFFSET_PARAM, rawOffsetValue);
            pregainOffset = parameterAsQuantityType(PREGAIN_OFFSET_PARAM, rawOffsetValue, Units.ONE);

        }
        {
            Object gainValue = orDefault("1", this.context.getConfiguration().get(GAIN_PARAM));
            logger.debug("Configuring profile with {} parameter '{}'", GAIN_PARAM, gainValue);
            gain = parameterAsQuantityType(GAIN_PARAM, gainValue);

        }
    }

    public boolean isValid() {
        return pregainOffset.isPresent() && gain.isPresent();
    }

    public Optional<QuantityType<?>> getPregainOffset() {
        return pregainOffset;
    }

    public Optional<QuantityType<?>> getGain() {
        return gain;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return ModbusProfiles.GAIN_OFFSET;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no-op
    }

    @Override
    public void onCommandFromItem(Command command) {
        Type result = applyGainOffset(command, false);
        if (result instanceof Command) {
            logger.trace("Command '{}' from item, sending converted '{}' state towards handler.", command, result);
            callback.handleCommand((Command) result);
        }
    }

    @Override
    public void onCommandFromHandler(Command command) {
        Type result = applyGainOffset(command, true);
        if (result instanceof Command) {
            logger.trace("Command '{}' from handler, sending converted '{}' command towards item.", command, result);
            callback.sendCommand((Command) result);
        }
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        State result = (State) applyGainOffset(state, true);
        logger.trace("State update '{}' from handler, sending converted '{}' state towards item.", state, result);
        callback.sendUpdate(result);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Type applyGainOffset(Type state, boolean towardsItem) {
        Type result = UnDefType.UNDEF;
        Optional<QuantityType<?>> localGain = gain;
        Optional<QuantityType<?>> localPregainOffset = pregainOffset;
        if (localGain.isEmpty() || localPregainOffset.isEmpty()) {
            logger.warn("Gain or offset unavailable. Check logs for configuration errors.");
            return UnDefType.UNDEF;
        } else if (state instanceof UnDefType) {
            return UnDefType.UNDEF;
        }

        QuantityType<?> gain = localGain.get();
        QuantityType<?> pregainOffsetQt = localPregainOffset.get();
        String formula = towardsItem ? String.format("( '%s' + '%s') * '%s'", state, pregainOffsetQt, gain)
                : String.format("'%s'/'%s' - '%s'", state, gain, pregainOffsetQt);
        if (state instanceof QuantityType) {
            final QuantityType<?> qtState;
            if (towardsItem) {
                qtState = ((QuantityType) state).toUnit(Units.ONE);
                if (qtState == null) {
                    logger.warn("Profile can only process plain numbers from handler. Got unit {}. Returning UNDEF",
                            ((QuantityType) state).getUnit());
                    return UnDefType.UNDEF;
                }
            } else {
                qtState = (QuantityType<?>) state;
            }
            try {
                if (towardsItem) {
                    result = applyGain(qtState.add((QuantityType) pregainOffsetQt), gain, true);
                } else {
                    result = applyGain(qtState, gain, false).subtract((QuantityType) pregainOffsetQt);
                }
            } catch (UnconvertibleException | UnsupportedOperationException e) {
                logger.warn(
                        "Cannot apply gain ('{}') and offset ('{}') to state ('{}') (formula {}) because types do not match (towardsItem={}): {}",
                        gain, pregainOffsetQt, qtState, formula, towardsItem, e.getMessage());
                return UnDefType.UNDEF;
            }
        } else if (state instanceof DecimalType) {
            DecimalType decState = (DecimalType) state;
            return applyGainOffset(new QuantityType(decState, Units.ONE), towardsItem);
        } else if (state instanceof RefreshType) {
            result = state;
        } else {
            logger.warn(
                    "Gain '{}' cannot be applied to the incompatible state '{}' of type {} sent from the binding (towardsItem={}). Returning original state.",
                    gain, state, state.getClass().getSimpleName(), towardsItem);
            result = state;
        }
        if (result == null) {
            // Should not happen, unit conversions are bound to succeed
            // This is here to make the compiler happy
            throw new IllegalStateException();
        }
        return result;
    }

    private Optional<QuantityType<?>> parameterAsQuantityType(String parameterName, Object parameterValue) {
        return parameterAsQuantityType(parameterName, parameterValue, null);
    }

    private Optional<QuantityType<?>> parameterAsQuantityType(String parameterName, Object parameterValue,
            @Nullable Unit<?> assertUnit) {
        Optional<QuantityType<?>> result = Optional.empty();
        if (parameterValue instanceof String) {
            try {
                QuantityType<?> quantityType = new QuantityType<>((String) parameterValue);
                if (assertUnit != null) {
                    QuantityType<?> normalizedQt = quantityType.toUnit(assertUnit);
                    if (normalizedQt != null) {
                        result = Optional.of(quantityType);
                    } else {
                        logger.error("Parameter '{}' is expected to be convertible into {}, unit was {}.",
                                parameterName, assertUnit, quantityType.getUnit());
                    }
                } else {
                    result = Optional.of(quantityType);
                }
            } catch (IllegalArgumentException e) {
                logger.error("Cannot convert value '{}' of parameter '{}' into a QuantityType.", parameterValue,
                        parameterName);
            }
        } else if (parameterValue instanceof BigDecimal) {
            BigDecimal parameterBigDecimal = (BigDecimal) parameterValue;
            result = Optional.of(new QuantityType<>(parameterBigDecimal.toString()));
        } else {
            logger.error("Parameter '{}' is not of type String or BigDecimal", parameterName);
        }
        return result;
    }

    /**
     * Calculate qtState * gain or qtState/gain
     *
     * When the conversion is towards the handler (towardsItem=false), unit will be ONE
     *
     */
    private QuantityType<?> applyGain(QuantityType<?> qtState, QuantityType<?> gainDelta, boolean towardsItem) {
        if (towardsItem) {
            return qtState.multiply(gainDelta);
        } else {
            QuantityType<?> plain = qtState.toUnit(gainDelta.getUnit());
            if (plain == null) {
                throw new UnconvertibleException(
                        String.format("Cannot process command '%s', unit should compatible with gain", qtState));
            }
            return new QuantityType<>(plain.toBigDecimal().divide(gainDelta.toBigDecimal()), Units.ONE);
        }
    }

    private static Object orDefault(Object defaultValue, @Nullable Object value) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof String && ((String) value).isBlank()) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
