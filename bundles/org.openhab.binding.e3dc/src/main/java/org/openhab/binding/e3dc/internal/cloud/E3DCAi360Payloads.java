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
package org.openhab.binding.e3dc.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Maps the JSON bodies of the AI 360 endpoints to the channel state and back.
 *
 * <p>
 * Both endpoints are written with the complete object rather than with the field that changes, so a write
 * always starts from the last read body and replaces the fields it is meant to change. That keeps the
 * fields a channel does not cover, such as the wallbox price limit, untouched.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCAi360Payloads {

    /** Battery takes part in dynamic prices. */
    public static final String EMS_BATTERY = "battery";
    /** Battery is controlled through an SG Ready signal. */
    public static final String EMS_SG_READY = "sgReady";
    /** How charging reacts to the feed in tariff. */
    public static final String EMS_FEED_IN_TARIFF = "feedInTariffType";

    /** AI 360 is active for the storage. */
    public static final String PRICES_DYNAMIC_EMS = "enableDynamicEms";
    /** Battery takes part in dynamic prices, the storage side counterpart of {@link #EMS_BATTERY}. */
    public static final String PRICES_BATTERY_ENABLED = "dppEnabledBattery";
    /** Price limit of the battery in ct/kWh. */
    public static final String PRICES_PRICE_LIMIT = "priceLimitBattery";
    /** State of charge in percent up to which the battery is charged from the grid. */
    public static final String PRICES_MAX_SOC = "maxSocBattery";
    /** Months the price limit applies to, bit 0 is January. */
    public static final String PRICES_MONTHS = "activeMonthsBattery";

    private static final int MONTHS = 12;

    private E3DCAi360Payloads() {
    }

    /**
     * The state of both bodies together.
     *
     * @param ems the body of the installation endpoint, or {@code null} when it could not be read
     * @param prices the body of the storage endpoint, or {@code null} when it could not be read
     */
    public static E3DCAi360State state(@Nullable JsonObject ems, @Nullable JsonObject prices) {
        return new E3DCAi360State(//
                bool(prices, PRICES_DYNAMIC_EMS), //
                bool(ems, EMS_BATTERY), //
                bool(ems, EMS_SG_READY), //
                text(ems, EMS_FEED_IN_TARIFF), //
                number(prices, PRICES_PRICE_LIMIT), //
                (int) Math.round(number(prices, PRICES_MAX_SOC)), //
                (int) Math.round(number(prices, PRICES_MONTHS)));
    }

    /**
     * The body that switches the battery into or out of dynamic prices on the installation.
     *
     * @param read the last read body
     * @param battery whether the battery takes part
     * @param sgReady whether the battery follows an SG Ready signal
     * @param feedInTariffType the tariff type, or {@code null} to clear it
     */
    public static @Nullable JsonObject ems(@Nullable JsonObject read, boolean battery, boolean sgReady,
            @Nullable String feedInTariffType) {
        if (read == null) {
            return null;
        }
        JsonObject body = read.deepCopy();
        body.addProperty(EMS_BATTERY, battery);
        body.addProperty(EMS_SG_READY, sgReady);
        if (feedInTariffType == null || feedInTariffType.isBlank()) {
            body.add(EMS_FEED_IN_TARIFF, JsonNull.INSTANCE);
        } else {
            body.addProperty(EMS_FEED_IN_TARIFF, feedInTariffType);
        }
        return body;
    }

    /** The body that enables or disables AI 360 on the storage. */
    public static @Nullable JsonObject pricesEnabled(@Nullable JsonObject read, boolean enabled) {
        return prices(read, enabled, enabled, null, null, null);
    }

    /**
     * The body that changes the price based charging of the battery on the storage.
     *
     * @param read the last read body
     * @param batteryPriceCharging whether the battery is charged when the price is below the limit
     * @param enabled whether AI 360 is active, which is only effective together with
     *            {@code batteryPriceCharging}
     * @param priceLimit the limit in ct/kWh, or {@code null} to keep the read value
     * @param chargeUntilSoc the state of charge in percent, or {@code null} to keep the read value
     * @param activeMonths the months as a bit mask, or {@code null} to keep the read value
     */
    public static @Nullable JsonObject prices(@Nullable JsonObject read, boolean batteryPriceCharging, boolean enabled,
            @Nullable Double priceLimit, @Nullable Integer chargeUntilSoc, @Nullable Integer activeMonths) {
        if (read == null) {
            // without the read body the base fields of the object, such as the grid fee, are unknown
            return null;
        }
        JsonObject body = read.deepCopy();
        body.addProperty(PRICES_BATTERY_ENABLED, batteryPriceCharging);
        body.addProperty(PRICES_DYNAMIC_EMS, enabled);
        if (priceLimit != null) {
            body.addProperty(PRICES_PRICE_LIMIT, priceLimit);
        }
        if (chargeUntilSoc != null) {
            body.addProperty(PRICES_MAX_SOC, chargeUntilSoc);
        }
        if (activeMonths != null) {
            body.addProperty(PRICES_MONTHS, activeMonths);
        }
        return body;
    }

    /** The months of a bit mask as twelve characters, starting with January. */
    public static String monthsText(int mask) {
        StringBuilder text = new StringBuilder(MONTHS);
        for (int month = 0; month < MONTHS; month++) {
            text.append((mask & (1 << month)) != 0 ? '1' : '0');
        }
        return text.toString();
    }

    /**
     * The bit mask of twelve characters, starting with January.
     *
     * @throws IllegalArgumentException when the text is not twelve characters of {@code 0} and {@code 1}
     */
    public static int monthsMask(String text) {
        String trimmed = text.trim();
        if (trimmed.length() != MONTHS) {
            throw new IllegalArgumentException("Expected twelve characters, one per month, but got '" + text + "'");
        }
        int mask = 0;
        for (int month = 0; month < MONTHS; month++) {
            char flag = trimmed.charAt(month);
            if (flag == '1') {
                mask |= 1 << month;
            } else if (flag != '0') {
                throw new IllegalArgumentException("Month " + (month + 1) + " is neither 0 nor 1 in '" + text + "'");
            }
        }
        return mask;
    }

    private static boolean bool(@Nullable JsonObject object, String name) {
        JsonElement element = member(object, name);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                && element.getAsBoolean();
    }

    private static double number(@Nullable JsonObject object, String name) {
        JsonElement element = member(object, name);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return 0;
        }
        return element.getAsDouble();
    }

    private static String text(@Nullable JsonObject object, String name) {
        JsonElement element = member(object, name);
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return E3DCAi360State.NO_FEED_IN_TARIFF;
        }
        return element.getAsString();
    }

    private static @Nullable JsonElement member(@Nullable JsonObject object, String name) {
        if (object == null) {
            return null;
        }
        JsonElement element = object.get(name);
        return element == null || element.isJsonNull() ? null : element;
    }
}
