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
import org.openhab.binding.energidataservice.internal.api.Dataset;

/**
 * Class for CO2 emission subscription.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Co2EmissionSubscription implements Subscription {
    private final String priceArea;
    private final Type type;

    public enum Type {
        Prognosis(Dataset.CO2EmissionPrognosis),
        Realtime(Dataset.CO2Emission);

        private final Dataset dataset;

        Type(Dataset dataset) {
            this.dataset = dataset;
        }

        public Dataset getDataset() {
            return dataset;
        }
    }

    private Co2EmissionSubscription(String priceArea, Type type) {
        this.priceArea = priceArea;
        this.type = type;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Co2EmissionSubscription other)) {
            return false;
        }

        return this.priceArea.equals(other.priceArea) && this.type.equals((other.type));
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceArea, type);
    }

    @Override
    public String toString() {
        return "Co2EmissionSubscription: PriceArea=" + priceArea + ", Type=" + type;
    }

    public String getPriceArea() {
        return priceArea;
    }

    public Type getType() {
        return type;
    }

    public static Co2EmissionSubscription of(String priceArea, Type type) {
        return new Co2EmissionSubscription(priceArea, type);
    }
}
