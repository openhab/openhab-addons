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
package org.openhab.binding.energidataservice.internal.provider.subscription;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilter;
import org.openhab.binding.energidataservice.internal.api.GlobalLocationNumber;

/**
 * Class for datahub price subscription.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DatahubPriceSubscription implements ElectricityPriceSubscription {
    private final DatahubTariff datahubTariff;
    private final GlobalLocationNumber globalLocationNumber;
    private final DatahubTariffFilter filter;

    private DatahubPriceSubscription(DatahubTariff datahubTariff, GlobalLocationNumber globalLocationNumber,
            DatahubTariffFilter filter) {
        this.datahubTariff = datahubTariff;
        this.globalLocationNumber = globalLocationNumber;
        this.filter = filter;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DatahubPriceSubscription other)) {
            return false;
        }

        return this.globalLocationNumber.equals(other.globalLocationNumber) && this.filter.equals(other.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalLocationNumber, filter);
    }

    @Override
    public String toString() {
        return "DatahubPriceSubscription: GLN=" + globalLocationNumber + ", Filter=" + filter;
    }

    public DatahubTariff getDatahubTariff() {
        return datahubTariff;
    }

    public GlobalLocationNumber getGlobalLocationNumber() {
        return globalLocationNumber;
    }

    public DatahubTariffFilter getFilter() {
        return filter;
    }

    public static DatahubPriceSubscription of(DatahubTariff datahubTariff, GlobalLocationNumber globalLocationNumber,
            DatahubTariffFilter filter) {
        return new DatahubPriceSubscription(datahubTariff, globalLocationNumber, filter);
    }
}
