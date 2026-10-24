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
package org.openhab.binding.gme.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GmeBindingConstants} class defines common constants used by the binding.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmeBindingConstants {

    public static final String BINDING_ID = "gme";

    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "api");
    public static final ThingTypeUID THING_TYPE_PUN = new ThingTypeUID(BINDING_ID, "pun");

    public static final String CHANNEL_PASSWORD_LAST_CHANGED = "password-last-changed";
    public static final String CHANNEL_PASSWORD_EXPIRY = "password-expiry";
    public static final String CHANNEL_PASSWORD_DAYS_REMAINING = "password-days-remaining";
    public static final String CHANNEL_PASSWORD_STATUS = "password-status";

    public static final String CHANNEL_CURRENT_PRICE = "market#current-price";
    public static final String CHANNEL_NEXT_PRICE = "market#next-price";
    public static final String CHANNEL_TODAY_PRICES = "today#prices";
    public static final String CHANNEL_TOMORROW_PRICES = "tomorrow#prices";

    public static final String CHANNEL_TODAY_ZONAL_PRICES = "today#zonal-prices";
    public static final String CHANNEL_TOMORROW_ZONAL_PRICES = "tomorrow#zonal-prices";

    public static final String CHANNEL_TODAY_AVERAGE = "today#average";
    public static final String CHANNEL_TODAY_MIN = "today#min";
    public static final String CHANNEL_TODAY_MAX = "today#max";
    public static final String CHANNEL_TODAY_MIN_TIME = "today#min-time";
    public static final String CHANNEL_TODAY_MAX_TIME = "today#max-time";

    public static final String CHANNEL_TOMORROW_AVERAGE = "tomorrow#average";
    public static final String CHANNEL_TOMORROW_MIN = "tomorrow#min";
    public static final String CHANNEL_TOMORROW_MAX = "tomorrow#max";
    public static final String CHANNEL_TOMORROW_MIN_TIME = "tomorrow#min-time";
    public static final String CHANNEL_TOMORROW_MAX_TIME = "tomorrow#max-time";

    public static final String CHANNEL_TOMORROW_AVAILABLE = "market#tomorrow-available";
    public static final String CHANNEL_LAST_UPDATE = "market#last-update";

    private GmeBindingConstants() {
    }
}
