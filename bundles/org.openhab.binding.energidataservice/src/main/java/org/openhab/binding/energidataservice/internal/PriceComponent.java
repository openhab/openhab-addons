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
package org.openhab.binding.energidataservice.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link PriceComponent} represents the different components making up the total electricity price.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum PriceComponent {
    SPOT_PRICE("SpotPrice", null),
    GRID_TARIFF("GridTariff", DatahubTariff.GRID_TARIFF),
    SYSTEM_TARIFF("SystemTariff", DatahubTariff.SYSTEM_TARIFF),
    TRANSMISSION_GRID_TARIFF("TransmissionGridTariff", DatahubTariff.TRANSMISSION_GRID_TARIFF),
    ELECTRICITY_TAX("ElectricityTax", DatahubTariff.ELECTRICITY_TAX),
    REDUCED_ELECTRICITY_TAX("ReducedElectricityTax", DatahubTariff.REDUCED_ELECTRICITY_TAX);

    private static final Map<String, PriceComponent> NAME_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(PriceComponent::toLowerCaseString, Function.identity()));

    private String name;
    private @Nullable DatahubTariff datahubTariff;

    private PriceComponent(String name, @Nullable DatahubTariff datahubTariff) {
        this.name = name;
        this.datahubTariff = datahubTariff;
    }

    @Override
    public String toString() {
        return name;
    }

    private String toLowerCaseString() {
        return name.toLowerCase();
    }

    public static PriceComponent fromString(final String name) {
        PriceComponent component = NAME_MAP.get(name.toLowerCase());
        if (component == null) {
            throw new IllegalArgumentException(
                    "'" + name + "' has no corresponding value. Accepted values: " + Arrays.toString(values()));
        }
        return component;
    }

    public @Nullable DatahubTariff getDatahubTariff() {
        return datahubTariff;
    }
}
