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
import org.openhab.binding.frenchgovtenergydata.internal.dto.HpHcTariff;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.Thing;

/**
 * The {@link HpHcTariffHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HpHcTariffHandler extends TariffHandler<HpHcTariff> {
    private static final String EMPTY_LINE = ";;;;;;;;";
    private static final String DATASET_ID = "f7303b3a-93c7-4242-813d-84919034c416";

    public HpHcTariffHandler(Thing thing) {
        super(thing, DATASET_ID);
    }

    @Override
    protected Stream<HpHcTariff> interpretLines(List<String> lines) {
        return lines.stream().filter(line -> !line.equals(EMPTY_LINE)).map(HpHcTariff::new);
    }

    @Override
    protected void updateChannels(HpHcTariff tariff) {
        super.updateChannels(tariff);
        updateState(CHANNEL_HP_HT, new QuantityType<>(tariff.hpHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_HP_TTC, new QuantityType<>(tariff.hpTTC, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_HC_HT, new QuantityType<>(tariff.hcHT, CurrencyUnits.BASE_ENERGY_PRICE));
        updateState(CHANNEL_HC_TTC, new QuantityType<>(tariff.hcTTC, CurrencyUnits.BASE_ENERGY_PRICE));
    }
}
