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
package org.openhab.binding.pixometer.internal.config;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ReadingInstance} class is the representing java model for the json result for a reading from the pixometer
 * api
 *
 * @author Jerome Luckenbach - Initial Contribution
 *
 */
@NonNullByDefault
public class ReadingInstance {

    private @NonNullByDefault({}) String url;
    private @NonNullByDefault({}) String appliedMethod;
    private @NonNullByDefault({}) ZonedDateTime readingDate;
    private double value;
    private double valueSecondTariff;
    private int providedFractionDigits;
    private int providedFractionDigitsSecondTariff;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppliedMethod() {
        return appliedMethod;
    }

    public void setAppliedMethod(String appliedMethod) {
        this.appliedMethod = appliedMethod;
    }

    public ZonedDateTime getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(ZonedDateTime readingDate) {
        this.readingDate = readingDate;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValueSecondTariff() {
        return valueSecondTariff;
    }

    public void setValueSecondTariff(double valueSecondTariff) {
        this.valueSecondTariff = valueSecondTariff;
    }

    public int getProvidedFractionDigits() {
        return providedFractionDigits;
    }

    public void setProvidedFractionDigits(int provided_fraction_digits) {
        this.providedFractionDigits = provided_fraction_digits;
    }

    public int getProvidedFractionDigitsSecondTariff() {
        return providedFractionDigitsSecondTariff;
    }

    public void setProvidedFractionDigitsSecondTariff(int provided_fraction_digits_second_tariff) {
        this.providedFractionDigitsSecondTariff = provided_fraction_digits_second_tariff;
    }
}
