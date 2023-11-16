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
package org.openhab.io.homekit.internal.accessories;

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.impl.airquality.AirQualityCharacteristic;
import io.github.hapjava.characteristics.impl.airquality.AirQualityEnum;
import io.github.hapjava.characteristics.impl.common.ActiveCharacteristic;
import io.github.hapjava.characteristics.impl.common.ActiveEnum;
import io.github.hapjava.characteristics.impl.common.ActiveIdentifierCharacteristic;
import io.github.hapjava.characteristics.impl.common.ConfiguredNameCharacteristic;
import io.github.hapjava.characteristics.impl.common.IdentifierCharacteristic;
import io.github.hapjava.characteristics.impl.common.IsConfiguredCharacteristic;
import io.github.hapjava.characteristics.impl.common.IsConfiguredEnum;
import io.github.hapjava.characteristics.impl.common.NameCharacteristic;
import io.github.hapjava.characteristics.impl.common.ServiceLabelIndexCharacteristic;
import io.github.hapjava.characteristics.impl.heatercooler.CurrentHeaterCoolerStateCharacteristic;
import io.github.hapjava.characteristics.impl.heatercooler.CurrentHeaterCoolerStateEnum;
import io.github.hapjava.characteristics.impl.heatercooler.TargetHeaterCoolerStateCharacteristic;
import io.github.hapjava.characteristics.impl.heatercooler.TargetHeaterCoolerStateEnum;
import io.github.hapjava.characteristics.impl.inputsource.CurrentVisibilityStateCharacteristic;
import io.github.hapjava.characteristics.impl.inputsource.CurrentVisibilityStateEnum;
import io.github.hapjava.characteristics.impl.inputsource.InputDeviceTypeCharacteristic;
import io.github.hapjava.characteristics.impl.inputsource.InputDeviceTypeEnum;
import io.github.hapjava.characteristics.impl.inputsource.InputSourceTypeCharacteristic;
import io.github.hapjava.characteristics.impl.inputsource.InputSourceTypeEnum;
import io.github.hapjava.characteristics.impl.television.ClosedCaptionsCharacteristic;
import io.github.hapjava.characteristics.impl.television.ClosedCaptionsEnum;
import io.github.hapjava.characteristics.impl.television.PictureModeCharacteristic;
import io.github.hapjava.characteristics.impl.television.PictureModeEnum;
import io.github.hapjava.characteristics.impl.television.SleepDiscoveryModeCharacteristic;
import io.github.hapjava.characteristics.impl.television.SleepDiscoveryModeEnum;
import io.github.hapjava.characteristics.impl.televisionspeaker.VolumeControlTypeCharacteristic;
import io.github.hapjava.characteristics.impl.televisionspeaker.VolumeControlTypeEnum;
import io.github.hapjava.characteristics.impl.thermostat.CurrentHeatingCoolingStateCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.CurrentHeatingCoolingStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.TargetHeatingCoolingStateCharacteristic;
import io.github.hapjava.characteristics.impl.thermostat.TargetHeatingCoolingStateEnum;

/**
 * Creates an optional characteristics from metadata
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class HomekitMetadataCharacteristicFactory {

    // List of optional characteristics that can be set via metadata, and the corresponding method to create them.
    private static final Map<HomekitCharacteristicType, Function<Object, Characteristic>> OPTIONAL = new HashMap<>() {
        {
            put(ACTIVE_IDENTIFIER, HomekitMetadataCharacteristicFactory::createActiveIdentifierCharacteristic);
            put(ACTIVE_STATUS, HomekitMetadataCharacteristicFactory::createActiveStatusCharacteristic);
            put(AIR_QUALITY, HomekitMetadataCharacteristicFactory::createAirQualityCharacteristic);
            put(CLOSED_CAPTIONS, HomekitMetadataCharacteristicFactory::createClosedCaptionsCharacteristic);
            put(CONFIGURED, HomekitMetadataCharacteristicFactory::createIsConfiguredCharacteristic);
            put(CONFIGURED_NAME, HomekitMetadataCharacteristicFactory::createConfiguredNameCharacteristic);
            put(CURRENT_HEATER_COOLER_STATE,
                    HomekitMetadataCharacteristicFactory::createCurrentHeaterCoolerStateCharacteristic);
            put(CURRENT_HEATING_COOLING_STATE,
                    HomekitMetadataCharacteristicFactory::createCurrentHeatingCoolingStateCharacteristic);
            put(CURRENT_VISIBILITY, HomekitMetadataCharacteristicFactory::createCurrentVisibilityCharacteristic);
            put(IDENTIFIER, HomekitMetadataCharacteristicFactory::createIdentifierCharacteristic);
            put(INPUT_DEVICE_TYPE, HomekitMetadataCharacteristicFactory::createInputDeviceTypeCharacteristic);
            put(INPUT_SOURCE_TYPE, HomekitMetadataCharacteristicFactory::createInputSourceTypeCharacteristic);
            put(NAME, HomekitMetadataCharacteristicFactory::createNameCharacteristic);
            put(PICTURE_MODE, HomekitMetadataCharacteristicFactory::createPictureModeCharacteristic);
            put(SERVICE_INDEX, HomekitMetadataCharacteristicFactory::createServiceIndexCharacteristic);
            put(SLEEP_DISCOVERY_MODE, HomekitMetadataCharacteristicFactory::createSleepDiscoveryModeCharacteristic);
            put(TARGET_HEATER_COOLER_STATE,
                    HomekitMetadataCharacteristicFactory::createTargetHeaterCoolerStateCharacteristic);
            put(TARGET_HEATING_COOLING_STATE,
                    HomekitMetadataCharacteristicFactory::createTargetHeatingCoolingStateCharacteristic);
            put(VOLUME_CONTROL_TYPE, HomekitMetadataCharacteristicFactory::createVolumeControlTypeCharacteristic);
        }
    };

    public static Optional<Characteristic> createCharacteristic(String characteristic, Object value) {
        var type = HomekitCharacteristicType.valueOfTag(characteristic);
        if (type.isEmpty() || !OPTIONAL.containsKey(type.get())) {
            return Optional.empty();
        }
        return Optional.of(OPTIONAL.get(type.get()).apply(value));
    }

    private static Supplier<CompletableFuture<Integer>> getInteger(Object value) {
        int intValue;
        if (value instanceof BigDecimal valueAsBigDecimal) {
            intValue = valueAsBigDecimal.intValue();
        } else if (value instanceof Float) {
            intValue = ((Float) value).intValue();
        } else if (value instanceof Integer) {
            intValue = (Integer) value;
        } else if (value instanceof Long) {
            intValue = ((Long) value).intValue();
        } else {
            intValue = Integer.valueOf(value.toString());
        }
        return () -> CompletableFuture.completedFuture(intValue);
    }

    private static Supplier<CompletableFuture<String>> getString(Object value) {
        return () -> CompletableFuture.completedFuture(value.toString());
    }

    private static <T extends Enum<T>> Supplier<CompletableFuture<T>> getEnum(Object value, Class<T> klazz) {
        T enumValue = Enum.valueOf(klazz, value.toString());
        return () -> CompletableFuture.completedFuture(enumValue);
    }

    private static <T extends Enum<T>> Supplier<CompletableFuture<T>> getEnum(Object value, Class<T> klazz, T trueValue,
            T falseValue) {
        if (value.equals(true) || value.equals("true")) {
            return () -> CompletableFuture.completedFuture(trueValue);
        } else if (value.equals(false) || value.equals("false")) {
            return () -> CompletableFuture.completedFuture(falseValue);
        }
        return getEnum(value, klazz);
    }

    private static Characteristic createActiveIdentifierCharacteristic(Object value) {
        return new ActiveIdentifierCharacteristic(getInteger(value), v -> {
        }, v -> {
        }, () -> {
        });
    }

    private static Characteristic createActiveStatusCharacteristic(Object value) {
        return new ActiveCharacteristic(getEnum(value, ActiveEnum.class, ActiveEnum.ACTIVE, ActiveEnum.INACTIVE), v -> {
        }, v -> {
        }, () -> {
        });
    }

    private static Characteristic createAirQualityCharacteristic(Object value) {
        return new AirQualityCharacteristic(getEnum(value, AirQualityEnum.class), v -> {
        }, () -> {
        });
    }

    private static Characteristic createClosedCaptionsCharacteristic(Object value) {
        return new ClosedCaptionsCharacteristic(
                getEnum(value, ClosedCaptionsEnum.class, ClosedCaptionsEnum.ENABLED, ClosedCaptionsEnum.DISABLED),
                v -> {
                }, v -> {
                }, () -> {
                });
    }

    private static Characteristic createIsConfiguredCharacteristic(Object value) {
        return new IsConfiguredCharacteristic(
                getEnum(value, IsConfiguredEnum.class, IsConfiguredEnum.CONFIGURED, IsConfiguredEnum.NOT_CONFIGURED),
                v -> {
                }, v -> {
                }, () -> {
                });
    }

    private static Characteristic createConfiguredNameCharacteristic(Object value) {
        return new ConfiguredNameCharacteristic(getString(value), v -> {
        }, v -> {
        }, () -> {
        });
    }

    private static Characteristic createCurrentVisibilityCharacteristic(Object value) {
        return new CurrentVisibilityStateCharacteristic(getEnum(value, CurrentVisibilityStateEnum.class,
                CurrentVisibilityStateEnum.SHOWN, CurrentVisibilityStateEnum.HIDDEN), v -> {
                }, () -> {
                });
    }

    private static Characteristic createCurrentHeaterCoolerStateCharacteristic(Object value) {
        var enumSupplier = getEnum(value, CurrentHeaterCoolerStateEnum.class);
        CurrentHeaterCoolerStateEnum enumValue;
        try {
            enumValue = enumSupplier.get().get();
        } catch (InterruptedException | ExecutionException e) {
            enumValue = CurrentHeaterCoolerStateEnum.INACTIVE;
        }
        return new CurrentHeaterCoolerStateCharacteristic(new CurrentHeaterCoolerStateEnum[] { enumValue },
                enumSupplier, v -> {
                }, () -> {
                });
    }

    private static Characteristic createCurrentHeatingCoolingStateCharacteristic(Object value) {
        var enumSupplier = getEnum(value, CurrentHeatingCoolingStateEnum.class);
        CurrentHeatingCoolingStateEnum enumValue;
        try {
            enumValue = enumSupplier.get().get();
        } catch (InterruptedException | ExecutionException e) {
            enumValue = CurrentHeatingCoolingStateEnum.OFF;
        }
        return new CurrentHeatingCoolingStateCharacteristic(new CurrentHeatingCoolingStateEnum[] { enumValue },
                enumSupplier, v -> {
                }, () -> {
                });
    }

    private static Characteristic createIdentifierCharacteristic(Object value) {
        return new IdentifierCharacteristic(getInteger(value));
    }

    private static Characteristic createInputDeviceTypeCharacteristic(Object value) {
        return new InputDeviceTypeCharacteristic(getEnum(value, InputDeviceTypeEnum.class), v -> {
        }, () -> {
        });
    }

    private static Characteristic createInputSourceTypeCharacteristic(Object value) {
        return new InputSourceTypeCharacteristic(getEnum(value, InputSourceTypeEnum.class), v -> {
        }, () -> {
        });
    }

    private static Characteristic createNameCharacteristic(Object value) {
        return new NameCharacteristic(getString(value));
    }

    private static Characteristic createPictureModeCharacteristic(Object value) {
        return new PictureModeCharacteristic(getEnum(value, PictureModeEnum.class), v -> {
        }, v -> {
        }, () -> {
        });
    }

    private static Characteristic createServiceIndexCharacteristic(Object value) {
        return new ServiceLabelIndexCharacteristic(getInteger(value));
    }

    private static Characteristic createSleepDiscoveryModeCharacteristic(Object value) {
        return new SleepDiscoveryModeCharacteristic(getEnum(value, SleepDiscoveryModeEnum.class,
                SleepDiscoveryModeEnum.ALWAYS_DISCOVERABLE, SleepDiscoveryModeEnum.NOT_DISCOVERABLE), v -> {
                }, () -> {
                });
    }

    private static Characteristic createTargetHeaterCoolerStateCharacteristic(Object value) {
        var enumSupplier = getEnum(value, TargetHeaterCoolerStateEnum.class);
        TargetHeaterCoolerStateEnum enumValue;
        try {
            enumValue = enumSupplier.get().get();
        } catch (InterruptedException | ExecutionException e) {
            enumValue = TargetHeaterCoolerStateEnum.AUTO;
        }

        return new TargetHeaterCoolerStateCharacteristic(new TargetHeaterCoolerStateEnum[] { enumValue }, enumSupplier,
                v -> {
                }, v -> {
                }, () -> {
                });
    }

    private static Characteristic createTargetHeatingCoolingStateCharacteristic(Object value) {
        var enumSupplier = getEnum(value, TargetHeatingCoolingStateEnum.class);
        TargetHeatingCoolingStateEnum enumValue;
        try {
            enumValue = enumSupplier.get().get();
        } catch (InterruptedException | ExecutionException e) {
            enumValue = TargetHeatingCoolingStateEnum.OFF;
        }

        return new TargetHeatingCoolingStateCharacteristic(new TargetHeatingCoolingStateEnum[] { enumValue },
                enumSupplier, v -> {
                }, v -> {
                }, () -> {
                });
    }

    private static Characteristic createVolumeControlTypeCharacteristic(Object value) {
        return new VolumeControlTypeCharacteristic(getEnum(value, VolumeControlTypeEnum.class), v -> {
        }, () -> {
        });
    }
}
