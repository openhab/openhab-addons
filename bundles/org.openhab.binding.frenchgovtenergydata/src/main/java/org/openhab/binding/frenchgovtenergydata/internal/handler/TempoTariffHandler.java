/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.frenchgovtenergydata.internal.dto.TempoTariff;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.Thing;

/**
 * The {@link ThingTariffTempoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TempoTariffHandler extends TariffHandler<TempoTariff> {
    private static final String EMPTY_LINE = ";;;;;;;;";
    private static final String DATASET_ID = "0c3d1d36-c412-4620-8566-e5cbb4fa2b5a";

    public TempoTariffHandler(Thing thing) {
        super(thing, DATASET_ID);
    }

    @Override
    protected Stream<TempoTariff> interpretLines(List<String> lines) {
        return lines.stream().filter(line -> !line.equals(EMPTY_LINE)).map(TempoTariff::new);
    }

    @Override
    protected void updateChannels(TempoTariff tariff) {
        super.updateChannels(tariff);
        updateState(CHANNEL_RED_HP_HT, new QuantityType<>(tariff.redHpHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_RED_HP_TTC, new QuantityType<>(tariff.redHpTTC, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_RED_HC_HT, new QuantityType<>(tariff.redHcHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_RED_HC_TTC, new QuantityType<>(tariff.redHcTTC, CurrencyUnits.BASE_ENERGY_PRICE));

        updateState(CHANNEL_WHITE_HP_HT, new QuantityType<>(tariff.whiteHpHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_WHITE_HP_TTC, new QuantityType<>(tariff.whiteHpTTC, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_WHITE_HC_HT, new QuantityType<>(tariff.whiteHcHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_WHITE_HC_TTC, new QuantityType<>(tariff.whiteHcTTC, CurrencyUnits.BASE_ENERGY_PRICE));

        updateState(CHANNEL_BLUE_HP_HT, new QuantityType<>(tariff.blueHpHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_BLUE_HP_TTC, new QuantityType<>(tariff.blueHpTTC, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_BLUE_HC_HT, new QuantityType<>(tariff.blueHcHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_BLUE_HC_TTC, new QuantityType<>(tariff.blueHcTTC, CurrencyUnits.BASE_ENERGY_PRICE));
    }
}
