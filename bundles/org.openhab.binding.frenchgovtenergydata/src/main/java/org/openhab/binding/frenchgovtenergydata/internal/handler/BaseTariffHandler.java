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
package org.openhab.binding.frenchgovtenergydata.internal.handler;

import static org.openhab.binding.frenchgovtenergydata.internal.FrenchGovtEnergyDataBindingConstants.*;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.frenchgovtenergydata.internal.dto.BaseTariff;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.Thing;

/**
 * The {@link BaseTariffHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class BaseTariffHandler extends TariffHandler<BaseTariff> {
    private static final String DATASET_ID = "c13d05e5-9e55-4d03-bf7e-042a2ade7e49";

    public BaseTariffHandler(Thing thing) {
        super(thing, DATASET_ID);
    }

    @Override
    protected Stream<BaseTariff> interpretLines(List<String> lines) {
        return lines.stream().map(BaseTariff::new);
    }

    @Override
    protected void updateChannels(BaseTariff tariff) {
        super.updateChannels(tariff);
        updateState(CHANNEL_VARIABLE_HT, new QuantityType<>(tariff.variableHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_VARIABLE_TTC, new QuantityType<>(tariff.variableTTC, CurrencyUnits.BASE_ENERGY_PRICE));
    }
}
