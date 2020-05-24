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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.CharacteristicEnum;
import io.github.hapjava.characteristics.ExceptionalConsumer;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
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
    private final static Map<HomekitCharacteristicType, BiFunction<HomekitTaggedItem, HomekitAccessoryUpdater, Characteristic>> optional = new HashMap<HomekitCharacteristicType, BiFunction<HomekitTaggedItem, HomekitAccessoryUpdater, Characteristic>>() {
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
     * @param item corresponding OH item
     * @param updater update to keep OH item and HomeKit characteristic in sync
     * @return HomeKit characteristic
     */
    public static Characteristic createCharacteristic(final HomekitTaggedItem item, HomekitAccessoryUpdater updater)
            throws HomekitException {
        final @Nullable HomekitCharacteristicType type = item.getCharacteristicType();
        logger.trace("createCharacteristic, type {} item {}", type, item);
        if (optional.containsKey(type)) {
            return optional.get(type).apply(item, updater);
        }
        logger.warn("Unsupported optional characteristic. Accessory type {}, characteristic type {}",
                item.getAccessoryType(), type);
        throw new HomekitException("Unsupported optional characteristic. Characteristic type \"" + type + "\"");
    }

    // METHODS TO CREATE SINGLE CHARACTERISTIC FROM OH ITEM

    // supporting methods
    @SuppressWarnings("null")
    private static <T extends CharacteristicEnum> CompletableFuture<T> getEnumFromItem(final HomekitTaggedItem item,
            T offEnum, T onEnum, T defaultEnum) {
        final State state = item.getItem().getState();
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

    private static void setValueFromEnum(final HomekitTaggedItem item, CharacteristicEnum value,
            CharacteristicEnum offEnum, CharacteristicEnum onEnum) {
        if (item.getItem() instanceof SwitchItem) {
            if (value.equals(offEnum)) {
                ((SwitchItem) item.getItem()).send(OnOffType.OFF);
            } else if (value.equals(onEnum)) {
                ((SwitchItem) item.getItem()).send(OnOffType.ON);
            } else {
                logger.warn("Enum value {} is not supported. Only following values are supported: {},{}", value,
                        offEnum, onEnum);
            }
        } else if (item.getItem() instanceof NumberItem) {
            ((NumberItem) item.getItem()).send(new DecimalType(value.getCode()));
        } else {
            logger.warn("Item type {} is not supported. Only Switch and Number item types are supported.",
                    item.getItem().getType());
        }
    }

    @SuppressWarnings("null")
    private static int getIntFromItem(final HomekitTaggedItem item) {
        int value = 0;
        final State state = item.getItem().getState();
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
        return value;
    }

    private static Supplier<CompletableFuture<Integer>> getIntSupplier(final HomekitTaggedItem item) {
        return () -> CompletableFuture.completedFuture(getIntFromItem(item));
    }

    private static ExceptionalConsumer<Integer> setIntConsumer(final HomekitTaggedItem item) {
        return (value) -> {
            if (item.getItem() instanceof NumberItem) {
                ((NumberItem) item.getItem()).send(new DecimalType(value));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Number type is supported.",
                        item.getItem().getType(), item.getName());
            }
        };
    }

    private static Supplier<CompletableFuture<Double>> getDoubleSupplier(final HomekitTaggedItem item) {
        return () -> {
            final DecimalType value = item.getItem().getStateAs(DecimalType.class);
            return CompletableFuture.completedFuture(value != null ? value.doubleValue() : 0.0);
        };
    }

    protected static Consumer<HomekitCharacteristicChangeCallback> getSubscriber(final HomekitTaggedItem item,
            final HomekitCharacteristicType key, final HomekitAccessoryUpdater updater) {
        return (callback) -> updater.subscribe((GenericItem) item.getItem(), key.getTag(), callback);
    }

    protected static Runnable getUnsubscriber(final HomekitTaggedItem item, final HomekitCharacteristicType key,
            final HomekitAccessoryUpdater updater) {
        return () -> updater.unsubscribe((GenericItem) item.getItem(), key.getTag());
    }

    // create method for characteristic
    private static StatusLowBatteryCharacteristic createStatusLowBatteryCharacteristic(final HomekitTaggedItem item,
            final HomekitAccessoryUpdater updater) {
        return new StatusLowBatteryCharacteristic(
                () -> getEnumFromItem(item, StatusLowBatteryEnum.NORMAL, StatusLowBatteryEnum.LOW,
                        StatusLowBatteryEnum.NORMAL),
                getSubscriber(item, BATTERY_LOW_STATUS, updater), getUnsubscriber(item, BATTERY_LOW_STATUS, updater));
    }

    private static StatusFaultCharacteristic createStatusFaultCharacteristic(final HomekitTaggedItem item,
            final HomekitAccessoryUpdater updater) {
        return new StatusFaultCharacteristic(
                () -> getEnumFromItem(item, StatusFaultEnum.NO_FAULT, StatusFaultEnum.GENERAL_FAULT,
                        StatusFaultEnum.NO_FAULT),
                getSubscriber(item, FAULT_STATUS, updater), getUnsubscriber(item, FAULT_STATUS, updater));
    }

    private static StatusTamperedCharacteristic createStatusTamperedCharacteristic(final HomekitTaggedItem item,
            final HomekitAccessoryUpdater updater) {
        return new StatusTamperedCharacteristic(
                () -> getEnumFromItem(item, StatusTamperedEnum.NOT_TAMPERED, StatusTamperedEnum.TAMPERED,
                        StatusTamperedEnum.NOT_TAMPERED),
                getSubscriber(item, TAMPERED_STATUS, updater), getUnsubscriber(item, TAMPERED_STATUS, updater));
    }

    private static ObstructionDetectedCharacteristic createObstructionDetectedCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new ObstructionDetectedCharacteristic(
                () -> CompletableFuture.completedFuture(
                        item.getItem().getState() == OnOffType.ON || item.getItem().getState() == OpenClosedType.OPEN),
                getSubscriber(item, OBSTRUCTION_STATUS, updater), getUnsubscriber(item, OBSTRUCTION_STATUS, updater));
    }

    private static StatusActiveCharacteristic createStatusActiveCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new StatusActiveCharacteristic(
                () -> CompletableFuture.completedFuture(
                        item.getItem().getState() == OnOffType.ON || item.getItem().getState() == OpenClosedType.OPEN),
                getSubscriber(item, ACTIVE_STATUS, updater), getUnsubscriber(item, ACTIVE_STATUS, updater));
    }

    private static NameCharacteristic createNameCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new NameCharacteristic(() -> {
            final State state = item.getItem().getState();
            return CompletableFuture.completedFuture(state instanceof UnDefType ? "" : state.toString());
        });
    }

    private static HoldPositionCharacteristic createHoldPositionCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new HoldPositionCharacteristic(OnOffType::from);
    }

    private static CarbonMonoxideLevelCharacteristic createCarbonMonoxideLevelCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new CarbonMonoxideLevelCharacteristic(getDoubleSupplier(item),
                getSubscriber(item, CARBON_DIOXIDE_LEVEL, updater),
                getUnsubscriber(item, CARBON_DIOXIDE_LEVEL, updater));
    }

    private static CarbonMonoxidePeakLevelCharacteristic createCarbonMonoxidePeakLevelCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new CarbonMonoxidePeakLevelCharacteristic(getDoubleSupplier(item),
                getSubscriber(item, CARBON_DIOXIDE_PEAK_LEVEL, updater),
                getUnsubscriber(item, CARBON_DIOXIDE_PEAK_LEVEL, updater));
    }

    private static CarbonDioxideLevelCharacteristic createCarbonDioxideLevelCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new CarbonDioxideLevelCharacteristic(getDoubleSupplier(item),
                getSubscriber(item, CARBON_MONOXIDE_LEVEL, updater),
                getUnsubscriber(item, CARBON_MONOXIDE_LEVEL, updater));
    }

    private static CarbonDioxidePeakLevelCharacteristic createCarbonDioxidePeakLevelCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new CarbonDioxidePeakLevelCharacteristic(getDoubleSupplier(item),
                getSubscriber(item, CARBON_MONOXIDE_PEAK_LEVEL, updater),
                getUnsubscriber(item, CARBON_MONOXIDE_PEAK_LEVEL, updater));
    }

    private static CurrentHorizontalTiltAngleCharacteristic createCurrentHorizontalTiltAngleCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new CurrentHorizontalTiltAngleCharacteristic(getIntSupplier(item),
                getSubscriber(item, CURRENT_HORIZONTAL_TILT_ANGLE, updater),
                getUnsubscriber(item, CURRENT_HORIZONTAL_TILT_ANGLE, updater));
    }

    private static CurrentVerticalTiltAngleCharacteristic createCurrentVerticalTiltAngleCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new CurrentVerticalTiltAngleCharacteristic(getIntSupplier(item),
                getSubscriber(item, CURRENT_VERTICAL_TILT_ANGLE, updater),
                getUnsubscriber(item, CURRENT_VERTICAL_TILT_ANGLE, updater));
    }

    private static TargetHorizontalTiltAngleCharacteristic createTargetHorizontalTiltAngleCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new TargetHorizontalTiltAngleCharacteristic(getIntSupplier(item), setIntConsumer(item),
                getSubscriber(item, TARGET_HORIZONTAL_TILT_ANGLE, updater),
                getUnsubscriber(item, TARGET_HORIZONTAL_TILT_ANGLE, updater));
    }

    private static TargetVerticalTiltAngleCharacteristic createTargetVerticalTiltAngleCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new TargetVerticalTiltAngleCharacteristic(getIntSupplier(item), setIntConsumer(item),
                getSubscriber(item, TARGET_HORIZONTAL_TILT_ANGLE, updater),
                getUnsubscriber(item, TARGET_HORIZONTAL_TILT_ANGLE, updater));
    }

    private static HueCharacteristic createHueCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new HueCharacteristic(() -> {
            Double value = 0.0;
            State state = item.getItem().getState();
            if (state instanceof HSBType) {
                value = ((HSBType) state).getHue().doubleValue();
            }
            return CompletableFuture.completedFuture(value);
        }, (hue) -> {
            State state = item.getItem().getState();
            if (item.getItem() instanceof ColorItem) {
                ((ColorItem) item.getItem()).send(new HSBType(new DecimalType(hue), ((HSBType) state).getSaturation(),
                        ((HSBType) state).getBrightness()));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Color type is supported.",
                        item.getItem().getType(), item.getName());
            }
        }, getSubscriber(item, HUE, updater), getUnsubscriber(item, HUE, updater));
    }

    private static BrightnessCharacteristic createBrightnessCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new BrightnessCharacteristic(() -> {
            int value = 0;
            final State state = item.getItem().getState();
            if (state instanceof HSBType) {
                value = ((HSBType) state).getBrightness().intValue();
            } else if (state instanceof PercentType) {
                value = ((PercentType) state).intValue();
            }
            return CompletableFuture.completedFuture(value);
        }, (brightness) -> {
            final Item oItem = item.getItem();
            final State state = oItem.getState();
            if (oItem instanceof ColorItem) {
                ((ColorItem) oItem).send(new HSBType(((HSBType) state).getHue(), ((HSBType) state).getSaturation(),
                        new PercentType(brightness)));
            } else if (oItem instanceof DimmerItem) {
                ((DimmerItem) oItem).send(new PercentType(brightness));
            } else {
                logger.warn("Item type {} is not supported for {}. Only ColorItem and DimmerItem are supported.",
                        oItem.getType(), item.getName());
            }
        }, getSubscriber(item, BRIGHTNESS, updater), getUnsubscriber(item, BRIGHTNESS, updater));
    }

    private static SaturationCharacteristic createSaturationCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new SaturationCharacteristic(() -> {
            Double value = 0.0;
            State state = item.getItem().getState();
            if (state instanceof HSBType) {
                value = ((HSBType) state).getSaturation().doubleValue();
            } else if (state instanceof PercentType) {
                value = ((PercentType) state).doubleValue();
            }
            return CompletableFuture.completedFuture(value);
        }, (saturation) -> {
            final State state = item.getItem().getState();
            if (item.getItem() instanceof ColorItem) {
                ((ColorItem) item.getItem()).send(new HSBType(((HSBType) state).getHue(),
                        new PercentType(saturation.intValue()), ((HSBType) state).getBrightness()));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Color type is supported.",
                        item.getItem().getType(), item.getName());
            }
        }, getSubscriber(item, SATURATION, updater), getUnsubscriber(item, SATURATION, updater));
    }

    private static ColorTemperatureCharacteristic createColorTemperatureCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new ColorTemperatureCharacteristic(getIntSupplier(item), setIntConsumer(item),
                getSubscriber(item, COLOR_TEMPERATURE, updater), getUnsubscriber(item, COLOR_TEMPERATURE, updater));
    }

    private static CurrentFanStateCharacteristic createCurrentFanStateCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new CurrentFanStateCharacteristic(() -> {
            final DecimalType value = item.getItem().getStateAs(DecimalType.class);
            CurrentFanStateEnum currentFanStateEnum = value != null ? CurrentFanStateEnum.fromCode(value.intValue())
                    : null;
            if (currentFanStateEnum == null) {
                currentFanStateEnum = CurrentFanStateEnum.INACTIVE;
            }
            return CompletableFuture.completedFuture(currentFanStateEnum);
        }, getSubscriber(item, CURRENT_FAN_STATE, updater), getUnsubscriber(item, CURRENT_FAN_STATE, updater));
    }

    private static TargetFanStateCharacteristic createTargetFanStateCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new TargetFanStateCharacteristic(() -> {
            final DecimalType value = item.getItem().getStateAs(DecimalType.class);
            TargetFanStateEnum targetFanStateEnum = value != null ? TargetFanStateEnum.fromCode(value.intValue())
                    : null;
            if (targetFanStateEnum == null) {
                targetFanStateEnum = TargetFanStateEnum.AUTO;
            }
            return CompletableFuture.completedFuture(targetFanStateEnum);
        }, (targetState) -> {
            if (item.getItem() instanceof NumberItem) {
                ((NumberItem) item.getItem()).send(new DecimalType(targetState.getCode()));
            } else {
                logger.warn("Item type {} is not supported for {}. Only Number type is supported.",
                        item.getItem().getType(), item.getName());
            }
        }, getSubscriber(item, TARGET_FAN_STATE, updater), getUnsubscriber(item, TARGET_FAN_STATE, updater));
    }

    private static RotationDirectionCharacteristic createRotationDirectionCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new RotationDirectionCharacteristic(
                () -> getEnumFromItem(item, RotationDirectionEnum.CLOCKWISE, RotationDirectionEnum.COUNTER_CLOCKWISE,
                        RotationDirectionEnum.CLOCKWISE),
                (value) -> setValueFromEnum(item, value, RotationDirectionEnum.CLOCKWISE,
                        RotationDirectionEnum.COUNTER_CLOCKWISE),
                getSubscriber(item, ROTATION_DIRECTION, updater), getUnsubscriber(item, ROTATION_DIRECTION, updater));
    }

    private static SwingModeCharacteristic createSwingModeCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new SwingModeCharacteristic(
                () -> getEnumFromItem(item, SwingModeEnum.SWING_DISABLED, SwingModeEnum.SWING_ENABLED,
                        SwingModeEnum.SWING_DISABLED),
                (value) -> setValueFromEnum(item, value, SwingModeEnum.SWING_DISABLED, SwingModeEnum.SWING_ENABLED),
                getSubscriber(item, SWING_MODE, updater), getUnsubscriber(item, SWING_MODE, updater));
    }

    private static LockPhysicalControlsCharacteristic createLockPhysicalControlsCharacteristic(
            final HomekitTaggedItem item, HomekitAccessoryUpdater updater) {
        return new LockPhysicalControlsCharacteristic(
                () -> getEnumFromItem(item, LockPhysicalControlsEnum.CONTROL_LOCK_DISABLED,
                        LockPhysicalControlsEnum.CONTROL_LOCK_ENABLED, LockPhysicalControlsEnum.CONTROL_LOCK_DISABLED),
                (value) -> setValueFromEnum(item, value, LockPhysicalControlsEnum.CONTROL_LOCK_DISABLED,
                        LockPhysicalControlsEnum.CONTROL_LOCK_ENABLED),
                getSubscriber(item, LOCK_CONTROL, updater), getUnsubscriber(item, LOCK_CONTROL, updater));
    }

    private static RotationSpeedCharacteristic createRotationSpeedCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new RotationSpeedCharacteristic(getIntSupplier(item), setIntConsumer(item),
                getSubscriber(item, ROTATION_SPEED, updater), getUnsubscriber(item, ROTATION_SPEED, updater));
    }

    private static SetDurationCharacteristic createDurationCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new SetDurationCharacteristic(() -> {
            int value = getIntFromItem(item);
            if (value == 0) { // check for default duration
                final Object duration = item.getConfiguration().get(HomekitValveImpl.CONFIG_DEFAULT_DURATION);
                if ((duration != null) && (duration instanceof BigDecimal)) {
                    value = ((BigDecimal) duration).intValue();
                    if (item.getItem() instanceof NumberItem) {
                        ((NumberItem) item.getItem()).setState(new DecimalType(value));
                    }
                }
            }
            return CompletableFuture.completedFuture(value);
        }, setIntConsumer(item), getSubscriber(item, DURATION, updater), getUnsubscriber(item, DURATION, updater));
    }

    private static RemainingDurationCharacteristic createRemainingDurationCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new RemainingDurationCharacteristic(getIntSupplier(item),
                getSubscriber(item, REMAINING_DURATION, updater), getUnsubscriber(item, REMAINING_DURATION, updater));
    }

    private static VolumeCharacteristic createVolumeCharacteristic(final HomekitTaggedItem item,
            HomekitAccessoryUpdater updater) {
        return new VolumeCharacteristic(getIntSupplier(item),
                (volume) -> ((NumberItem) item.getItem()).send(new DecimalType(volume)),
                getSubscriber(item, DURATION, updater), getUnsubscriber(item, DURATION, updater));
    }
}
