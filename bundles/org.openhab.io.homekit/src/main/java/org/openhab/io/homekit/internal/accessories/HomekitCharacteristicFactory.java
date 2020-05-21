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
package org.openhab.io.homekit.internal.accessories;

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.CharacteristicEnum;
import io.github.hapjava.characteristics.ExceptionalConsumer;
import io.github.hapjava.characteristics.impl.audio.VolumeCharacteristic;
import io.github.hapjava.characteristics.impl.battery.StatusLowBatteryCharacteristic;
import io.github.hapjava.characteristics.impl.battery.StatusLowBatteryEnum;
import io.github.hapjava.characteristics.impl.carbondioxidesensor.CarbonDioxideLevelCharacteristic;
import io.github.hapjava.characteristics.impl.carbondioxidesensor.CarbonDioxidePeakLevelCharacteristic;
import io.github.hapjava.characteristics.impl.carbonmonoxidesensor.CarbonMonoxideLevelCharacteristic;
import io.github.hapjava.characteristics.impl.carbonmonoxidesensor.CarbonMonoxidePeakLevelCharacteristic;
import io.github.hapjava.characteristics.impl.common.NameCharacteristic;
import io.github.hapjava.characteristics.impl.common.ObstructionDetectedCharacteristic;
import io.github.hapjava.characteristics.impl.common.StatusActiveCharacteristic;
import io.github.hapjava.characteristics.impl.common.StatusFaultCharacteristic;
import io.github.hapjava.characteristics.impl.common.StatusFaultEnum;
import io.github.hapjava.characteristics.impl.common.StatusTamperedCharacteristic;
import io.github.hapjava.characteristics.impl.common.StatusTamperedEnum;
import io.github.hapjava.characteristics.impl.fan.*;
import io.github.hapjava.characteristics.impl.lightbulb.BrightnessCharacteristic;
import io.github.hapjava.characteristics.impl.lightbulb.ColorTemperatureCharacteristic;
import io.github.hapjava.characteristics.impl.lightbulb.HueCharacteristic;
import io.github.hapjava.characteristics.impl.lightbulb.SaturationCharacteristic;
import io.github.hapjava.characteristics.impl.valve.RemainingDurationCharacteristic;
import io.github.hapjava.characteristics.impl.valve.SetDurationCharacteristic;
import io.github.hapjava.characteristics.impl.windowcovering.CurrentHorizontalTiltAngleCharacteristic;
import io.github.hapjava.characteristics.impl.windowcovering.CurrentVerticalTiltAngleCharacteristic;
import io.github.hapjava.characteristics.impl.windowcovering.HoldPositionCharacteristic;
import io.github.hapjava.characteristics.impl.windowcovering.TargetHorizontalTiltAngleCharacteristic;
import io.github.hapjava.characteristics.impl.windowcovering.TargetVerticalTiltAngleCharacteristic;

/**
 * Creates a optional characteristics .
 *
 * @author Eugen Freiter - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("deprecation")
public class HomekitCharacteristicFactory {
    private static final Logger logger = LoggerFactory.getLogger(HomekitCharacteristicFactory.class);

    // List of optional characteristics and corresponding method to create them.
    private final static Map<HomekitCharacteristicType, BiFunction<GenericItem, HomekitAccessoryUpdater, Characteristic>> optional = new HashMap<HomekitCharacteristicType, BiFunction<GenericItem, HomekitAccessoryUpdater, Characteristic>>() {
        {
            put(NAME, HomekitCharacteristicFactory::createNameCharacteristic);
            put(BATTERY_LOW_STATUS, HomekitCharacteristicFactory::createStatusLowBatteryCharacteristic);
            put(FAULT_STATUS, HomekitCharacteristicFactory::createStatusFaultCharacteristic);
            put(TAMPERED_STATUS, HomekitCharacteristicFactory::createStatusTamperedCharacteristic);
            put(ACTIVE_STATUS, HomekitCharacteristicFactory::createStatusActiveCharacteristic);
            put(CARBON_MONOXIDE_LEVEL, HomekitCharacteristicFactory::createCarbonMonoxideLevelCharacteristic);
            put(CARBON_MONOXIDE_PEAK_LEVEL, HomekitCharacteristicFactory::createCarbonMonoxidePeakLevelCharacteristic);
            put(CARBON_DIOXIDE_LEVEL, HomekitCharacteristicFactory::createCarbonDioxideLevelCharacteristic);
            put(CARBON_DIOXIDE_PEAK_LEVEL, HomekitCharacteristicFactory::createCarbonDioxidePeakLevelCharacteristic);
            put(HOLD_POSITION, HomekitCharacteristicFactory::createHoldPositionCharacteristic);
            put(OBSTRUCTION_STATUS, HomekitCharacteristicFactory::createObstructionDetectedCharacteristic);
            put(CURRENT_HORIZONTAL_TILT_ANGLE,
                    HomekitCharacteristicFactory::createCurrentHorizontalTiltAngleCharacteristic);
            put(CURRENT_VERTICAL_TILT_ANGLE,
                    HomekitCharacteristicFactory::createCurrentVerticalTiltAngleCharacteristic);
            put(TARGET_HORIZONTAL_TILT_ANGLE,
                    HomekitCharacteristicFactory::createTargetHorizontalTiltAngleCharacteristic);
            put(TARGET_VERTICAL_TILT_ANGLE, HomekitCharacteristicFactory::createTargetVerticalTiltAngleCharacteristic);
            put(HUE, HomekitCharacteristicFactory::createHueCharacteristic);
            put(BRIGHTNESS, HomekitCharacteristicFactory::createBrightnessCharacteristic);
            put(SATURATION, HomekitCharacteristicFactory::createSaturationCharacteristic);
            put(COLOR_TEMPERATURE, HomekitCharacteristicFactory::createColorTemperatureCharacteristic);
            put(CURRENT_FAN_STATE, HomekitCharacteristicFactory::createCurrentFanStateCharacteristic);
            put(TARGET_FAN_STATE, HomekitCharacteristicFactory::createTargetFanStateCharacteristic);
            put(ROTATION_DIRECTION, HomekitCharacteristicFactory::createRotationDirectionCharacteristic);
            put(ROTATION_SPEED, HomekitCharacteristicFactory::createRotationSpeedCharacteristic);
            put(SWING_MODE, HomekitCharacteristicFactory::createSwingModeCharacteristic);
            put(LOCK_CONTROL, HomekitCharacteristicFactory::createLockPhysicalControlsCharacteristic);
            put(DURATION, HomekitCharacteristicFactory::createDurationCharacteristic);
            put(VOLUME, HomekitCharacteristicFactory::createVolumeCharacteristic);

            put(REMAINING_DURATION, HomekitCharacteristicFactory::createRemainingDurationCharacteristic);
            // LEGACY
            put(OLD_BATTERY_LOW_STATUS, HomekitCharacteristicFactory::createStatusLowBatteryCharacteristic);
        }
    };

    /**
     * create optional HomeKit characteristic
     *
     * @param type type of characteristic
     * @param item corresponding OH item
     * @param updater update to keep OH item and HomeKit characteristic in sync
     * @return HomeKit characteristic
     */
    public static Characteristic createCharacteristic(HomekitCharacteristicType type, GenericItem item,
            HomekitAccessoryUpdater updater) throws HomekitException {
        logger.trace("createCharacteristic, type {} item {}", type, item);
        if (optional.containsKey(type)) {
            return optional.get(type).apply(item, updater);
        }
        logger.warn("Unsupported optional characteristic. Item type {}, characteristic type {}", item.getType(), type);
        throw new HomekitException("Unsupported optional characteristic. Characteristic type \"" + item.getType());
    }

    // METHODS TO CREATE SINGLE CHARACTERISTIC FROM OH ITEM

    // supporting methods
    @SuppressWarnings("null")
    private static <T extends CharacteristicEnum> CompletableFuture<T> getEnumFromItem(GenericItem item, T offEnum,
            T onEnum, T defaultEnum) {
        final State state = item.getState();
        if (state instanceof OnOffType) {
            return CompletableFuture.completedFuture(state.equals(OnOffType.OFF) ? offEnum : onEnum);
        } else if (state instanceof OpenClosedType) {
            return CompletableFuture.completedFuture(state.equals(OpenClosedType.CLOSED) ? offEnum : onEnum);
        } else if (state instanceof DecimalType) {
            return CompletableFuture.completedFuture(state.as(DecimalType.class).intValue() == 0 ? offEnum : onEnum);
        } else if (state instanceof UnDefType) {
            return CompletableFuture.completedFuture(defaultEnum);
        }
        logger.warn(
                "Item state {} is not supported. Only OnOffType,OpenClosedType and Decimal (0/1) are supported. Ignore item {}",
                state, item.getName());
        return CompletableFuture.completedFuture(defaultEnum);
    }

    private static void setValueFromEnum(GenericItem item, CharacteristicEnum value, CharacteristicEnum offEnum,
            CharacteristicEnum onEnum) {
        if (item instanceof SwitchItem) {
            if (value.equals(offEnum)) {
                ((SwitchItem) item).send(OnOffType.OFF);
            } else if (value.equals(onEnum)) {
                ((SwitchItem) item).send(OnOffType.ON);
            } else {
                logger.warn("Enum value {} is not supported. Only following values are supported: {},{}", value,
                        offEnum, onEnum);
            }
        } else if (item instanceof NumberItem) {
            ((NumberItem) item).send(new DecimalType(value.getCode()));
        } else {
            logger.warn("Item type {} is not supported. Only Switch and Number item types are supported.",
                    item.getType());
        }
    }

    @SuppressWarnings("null")
    private static Supplier<CompletableFuture<Integer>> getIntSupplier(GenericItem item) {
        return () -> {
            int value = 0;
            final State state = item.getState();
            if (state instanceof PercentType) {
                value = state.as(PercentType.class).intValue();
            } else if (state instanceof DecimalType) {
                value = state.as(DecimalType.class).intValue();
            } else if (state instanceof UnDefType) {
                logger.debug("Item state {} is UNDEF {}.", state, item.getName());
            } else {
                logger.warn(
                        "Item state {} is not supported for {}. Only PercentType and DecimalType (0/100) are supported.",
                        state, item.getName());
            }
            logger.trace(" Get Int for {} value {}", item.getLabel(), value);
            return CompletableFuture.completedFuture(value);
        };
    }

    private static ExceptionalConsumer<Integer> setIntConsumer(final GenericItem item) {
        return (value) -> {
            if (item instanceof NumberItem) {
                ((NumberItem) item).send(new DecimalType(value));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Number type is supported.", item.getType(),
                        item.getName());
            }
        };
    }

    private static Supplier<CompletableFuture<Double>> getDoubleSupplier(Item item) {
        return () -> {
            final DecimalType value = item.getStateAs(DecimalType.class);
            return CompletableFuture.completedFuture(value != null ? value.doubleValue() : 0.0);
        };
    }

    // create method for characteristic
    private static StatusLowBatteryCharacteristic createStatusLowBatteryCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new StatusLowBatteryCharacteristic(
                () -> getEnumFromItem(item, StatusLowBatteryEnum.NORMAL, StatusLowBatteryEnum.LOW,
                        StatusLowBatteryEnum.NORMAL),
                (callback) -> updater.subscribe(item, BATTERY_LOW_STATUS.getTag(), callback),
                () -> updater.unsubscribe(item, BATTERY_LOW_STATUS.getTag()));
    }

    private static StatusFaultCharacteristic createStatusFaultCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new StatusFaultCharacteristic(
                () -> getEnumFromItem(item, StatusFaultEnum.NO_FAULT, StatusFaultEnum.GENERAL_FAULT,
                        StatusFaultEnum.NO_FAULT),
                (callback) -> updater.subscribe(item, FAULT_STATUS.getTag(), callback),
                () -> updater.unsubscribe(item, FAULT_STATUS.getTag()));
    }

    private static StatusTamperedCharacteristic createStatusTamperedCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new StatusTamperedCharacteristic(
                () -> getEnumFromItem(item, StatusTamperedEnum.NOT_TAMPERED, StatusTamperedEnum.TAMPERED,
                        StatusTamperedEnum.NOT_TAMPERED),
                (callback) -> updater.subscribe(item, TAMPERED_STATUS.getTag(), callback),
                () -> updater.unsubscribe(item, TAMPERED_STATUS.getTag()));
    }

    private static ObstructionDetectedCharacteristic createObstructionDetectedCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new ObstructionDetectedCharacteristic(
                () -> CompletableFuture
                        .completedFuture(item.getState() == OnOffType.ON || item.getState() == OpenClosedType.OPEN),
                (callback) -> updater.subscribe(item, OBSTRUCTION_STATUS.getTag(), callback),
                () -> updater.unsubscribe(item, OBSTRUCTION_STATUS.getTag()));
    }

    private static StatusActiveCharacteristic createStatusActiveCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new StatusActiveCharacteristic(
                () -> CompletableFuture
                        .completedFuture(item.getState() == OnOffType.ON || item.getState() == OpenClosedType.OPEN),
                (callback) -> updater.subscribe(item, ACTIVE_STATUS.getTag(), callback),
                () -> updater.unsubscribe(item, ACTIVE_STATUS.getTag()));
    }

    private static NameCharacteristic createNameCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new NameCharacteristic(() -> {
            final State state = item.getState();
            return CompletableFuture.completedFuture(state instanceof UnDefType ? "" : state.toString());
        });
    }

    private static HoldPositionCharacteristic createHoldPositionCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new HoldPositionCharacteristic(OnOffType::from);
    }

    private static CarbonMonoxideLevelCharacteristic createCarbonMonoxideLevelCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new CarbonMonoxideLevelCharacteristic(getDoubleSupplier(item),
                (callback) -> updater.subscribe(item, CARBON_DIOXIDE_LEVEL.getTag(), callback),
                () -> updater.unsubscribe(item, CARBON_DIOXIDE_LEVEL.getTag()));
    }

    private static CarbonMonoxidePeakLevelCharacteristic createCarbonMonoxidePeakLevelCharacteristic(
            final GenericItem item, HomekitAccessoryUpdater updater) {
        return new CarbonMonoxidePeakLevelCharacteristic(getDoubleSupplier(item),
                (callback) -> updater.subscribe(item, CARBON_DIOXIDE_PEAK_LEVEL.getTag(), callback),
                () -> updater.unsubscribe(item, CARBON_DIOXIDE_PEAK_LEVEL.getTag()));
    }

    private static CarbonDioxideLevelCharacteristic createCarbonDioxideLevelCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new CarbonDioxideLevelCharacteristic(getDoubleSupplier(item),
                (callback) -> updater.subscribe(item, CARBON_MONOXIDE_LEVEL.getTag(), callback),
                () -> updater.unsubscribe(item, CARBON_MONOXIDE_LEVEL.getTag()));
    }

    private static CarbonDioxidePeakLevelCharacteristic createCarbonDioxidePeakLevelCharacteristic(
            final GenericItem item, HomekitAccessoryUpdater updater) {
        return new CarbonDioxidePeakLevelCharacteristic(getDoubleSupplier(item),
                (callback) -> updater.subscribe(item, CARBON_MONOXIDE_PEAK_LEVEL.getTag(), callback),
                () -> updater.unsubscribe(item, CARBON_MONOXIDE_PEAK_LEVEL.getTag()));
    }

    private static CurrentHorizontalTiltAngleCharacteristic createCurrentHorizontalTiltAngleCharacteristic(
            final GenericItem item, HomekitAccessoryUpdater updater) {
        return new CurrentHorizontalTiltAngleCharacteristic(getIntSupplier(item),
                (callback) -> updater.subscribe(item, CURRENT_HORIZONTAL_TILT_ANGLE.getTag(), callback),
                () -> updater.unsubscribe(item, CURRENT_HORIZONTAL_TILT_ANGLE.getTag()));
    }

    private static CurrentVerticalTiltAngleCharacteristic createCurrentVerticalTiltAngleCharacteristic(
            final GenericItem item, HomekitAccessoryUpdater updater) {
        return new CurrentVerticalTiltAngleCharacteristic(getIntSupplier(item),
                (callback) -> updater.subscribe(item, CURRENT_VERTICAL_TILT_ANGLE.getTag(), callback),
                () -> updater.unsubscribe(item, CURRENT_VERTICAL_TILT_ANGLE.getTag()));
    }

    private static TargetHorizontalTiltAngleCharacteristic createTargetHorizontalTiltAngleCharacteristic(
            final GenericItem item, HomekitAccessoryUpdater updater) {
        return new TargetHorizontalTiltAngleCharacteristic(getIntSupplier(item), setIntConsumer(item),
                (callback) -> updater.subscribe(item, TARGET_HORIZONTAL_TILT_ANGLE.getTag(), callback),
                () -> updater.unsubscribe(item, TARGET_HORIZONTAL_TILT_ANGLE.getTag()));
    }

    private static TargetVerticalTiltAngleCharacteristic createTargetVerticalTiltAngleCharacteristic(
            final GenericItem item, HomekitAccessoryUpdater updater) {
        return new TargetVerticalTiltAngleCharacteristic(getIntSupplier(item), setIntConsumer(item),
                (callback) -> updater.subscribe(item, TARGET_VERTICAL_TILT_ANGLE.getTag(), callback),
                () -> updater.unsubscribe(item, TARGET_VERTICAL_TILT_ANGLE.getTag()));
    }

    private static HueCharacteristic createHueCharacteristic(final GenericItem item, HomekitAccessoryUpdater updater) {
        return new HueCharacteristic(() -> {
            Double value = 0.0;
            State state = item.getState();
            if (state instanceof HSBType) {
                value = ((HSBType) state).getHue().doubleValue();
            }
            return CompletableFuture.completedFuture(value);
        }, (hue) -> {
            State state = item.getState();
            if (item instanceof ColorItem) {
                ((ColorItem) item).send(new HSBType(new DecimalType(hue), ((HSBType) state).getSaturation(),
                        ((HSBType) state).getBrightness()));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Color type is supported.", item.getType(),
                        item.getName());
            }
        }, (callback) -> updater.subscribe(item, HUE.getTag(), callback),
                () -> updater.unsubscribe(item, HUE.getTag()));
    }

    private static BrightnessCharacteristic createBrightnessCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new BrightnessCharacteristic(() -> {
            int value = 0;
            final State state = item.getState();
            if (state instanceof HSBType) {
                value = ((HSBType) state).getBrightness().intValue();
            } else if (state instanceof PercentType) {
                value = ((PercentType) state).intValue();
            }
            return CompletableFuture.completedFuture(value);
        }, (brightness) -> {
            final State state = item.getState();
            if (item instanceof ColorItem) {
                ((ColorItem) item).send(new HSBType(((HSBType) state).getHue(), ((HSBType) state).getSaturation(),
                        new PercentType(brightness)));
            } else if (item instanceof DimmerItem) {
                ((DimmerItem) item).send(new PercentType(brightness));
            } else {
                logger.warn("Item type {} is not supported for {}. Only ColorItem and DimmerIterm are supported.",
                        item.getType(), item.getName());
            }
        }, (callback) -> updater.subscribe(item, BRIGHTNESS.getTag(), callback),
                () -> updater.unsubscribe(item, BRIGHTNESS.getTag()));
    }

    private static SaturationCharacteristic createSaturationCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new SaturationCharacteristic(() -> {
            Double value = 0.0;
            State state = item.getState();
            if (state instanceof HSBType) {
                value = ((HSBType) state).getSaturation().doubleValue();
            } else if (state instanceof PercentType) {
                value = ((PercentType) state).doubleValue();
            }
            return CompletableFuture.completedFuture(value);
        }, (saturation) -> {
            final State state = item.getState();
            if (item instanceof ColorItem) {
                ((ColorItem) item).send(new HSBType(((HSBType) state).getHue(), new PercentType(saturation.intValue()),
                        ((HSBType) state).getBrightness()));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Color type is supported.", item.getType(),
                        item.getName());
            }
        }, (callback) -> updater.subscribe(item, SATURATION.getTag(), callback),
                () -> updater.unsubscribe(item, SATURATION.getTag()));
    }

    private static ColorTemperatureCharacteristic createColorTemperatureCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new ColorTemperatureCharacteristic(getIntSupplier(item), setIntConsumer(item),
                (callback) -> updater.subscribe(item, COLOR_TEMPERATURE.getTag(), callback),
                () -> updater.unsubscribe(item, COLOR_TEMPERATURE.getTag()));
    }

    private static CurrentFanStateCharacteristic createCurrentFanStateCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new CurrentFanStateCharacteristic(() -> {
            final DecimalType value = item.getStateAs(DecimalType.class);
            CurrentFanStateEnum currentFanStateEnum = value != null ? CurrentFanStateEnum.fromCode(value.intValue())
                    : null;
            if (currentFanStateEnum == null) {
                currentFanStateEnum = CurrentFanStateEnum.INACTIVE;
            }
            return CompletableFuture.completedFuture(currentFanStateEnum);
        }, (callback) -> updater.subscribe(item, CURRENT_FAN_STATE.getTag(), callback),
                () -> updater.unsubscribe(item, CURRENT_FAN_STATE.getTag()));
    }

    private static TargetFanStateCharacteristic createTargetFanStateCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new TargetFanStateCharacteristic(() -> {
            final DecimalType value = item.getStateAs(DecimalType.class);
            TargetFanStateEnum targetFanStateEnum = value != null ? TargetFanStateEnum.fromCode(value.intValue())
                    : null;
            if (targetFanStateEnum == null) {
                targetFanStateEnum = TargetFanStateEnum.AUTO;
            }
            return CompletableFuture.completedFuture(targetFanStateEnum);
        }, (targetState) -> {
            if (item instanceof NumberItem) {
                ((NumberItem) item).send(new DecimalType(targetState.getCode()));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Number type is supported.", item.getType(),
                        item.getName());
            }
        }, (callback) -> updater.subscribe(item, TARGET_FAN_STATE.getTag(), callback),
                () -> updater.unsubscribe(item, TARGET_FAN_STATE.getTag()));
    }

    private static RotationDirectionCharacteristic createRotationDirectionCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new RotationDirectionCharacteristic(
                () -> getEnumFromItem(item, RotationDirectionEnum.CLOCKWISE, RotationDirectionEnum.COUNTER_CLOCKWISE,
                        RotationDirectionEnum.CLOCKWISE),
                (value) -> setValueFromEnum(item, value, RotationDirectionEnum.CLOCKWISE,
                        RotationDirectionEnum.COUNTER_CLOCKWISE),
                (callback) -> updater.subscribe(item, ROTATION_DIRECTION.getTag(), callback),
                () -> updater.unsubscribe(item, ROTATION_DIRECTION.getTag()));
    }

    private static SwingModeCharacteristic createSwingModeCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new SwingModeCharacteristic(
                () -> getEnumFromItem(item, SwingModeEnum.SWING_DISABLED, SwingModeEnum.SWING_ENABLED,
                        SwingModeEnum.SWING_DISABLED),
                (value) -> setValueFromEnum(item, value, SwingModeEnum.SWING_DISABLED, SwingModeEnum.SWING_ENABLED),
                (callback) -> updater.subscribe(item, SWING_MODE.getTag(), callback),
                () -> updater.unsubscribe(item, SWING_MODE.getTag()));
    }

    private static LockPhysicalControlsCharacteristic createLockPhysicalControlsCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new LockPhysicalControlsCharacteristic(
                () -> getEnumFromItem(item, LockPhysicalControlsEnum.CONTROL_LOCK_DISABLED,
                        LockPhysicalControlsEnum.CONTROL_LOCK_ENABLED, LockPhysicalControlsEnum.CONTROL_LOCK_DISABLED),
                (value) -> setValueFromEnum(item, value, LockPhysicalControlsEnum.CONTROL_LOCK_DISABLED,
                        LockPhysicalControlsEnum.CONTROL_LOCK_ENABLED),
                (callback) -> updater.subscribe(item, LOCK_CONTROL.getTag(), callback),
                () -> updater.unsubscribe(item, LOCK_CONTROL.getTag()));
    }

    private static RotationSpeedCharacteristic createRotationSpeedCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new RotationSpeedCharacteristic(getIntSupplier(item), setIntConsumer(item),
                (callback) -> updater.subscribe(item, ROTATION_SPEED.getTag(), callback),
                () -> updater.unsubscribe(item, ROTATION_SPEED.getTag()));
    }

    private static SetDurationCharacteristic createDurationCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new SetDurationCharacteristic(getIntSupplier(item), setIntConsumer(item),
                (callback) -> updater.subscribe(item, DURATION.getTag(), callback),
                () -> updater.unsubscribe(item, DURATION.getTag()));
    }

    private static RemainingDurationCharacteristic createRemainingDurationCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new RemainingDurationCharacteristic(getIntSupplier(item),
                (callback) -> updater.subscribe(item, REMAINING_DURATION.getTag(), callback),
                () -> updater.unsubscribe(item, REMAINING_DURATION.getTag()));
    }

    private static VolumeCharacteristic createVolumeCharacteristic(final GenericItem item,
            HomekitAccessoryUpdater updater) {
        return new VolumeCharacteristic(getIntSupplier(item),
                (volume) -> ((NumberItem) item).send(new DecimalType(volume)),
                (callback) -> updater.subscribe(item, DURATION.getTag(), callback),
                () -> updater.unsubscribe(item, DURATION.getTag()));
    }
}
