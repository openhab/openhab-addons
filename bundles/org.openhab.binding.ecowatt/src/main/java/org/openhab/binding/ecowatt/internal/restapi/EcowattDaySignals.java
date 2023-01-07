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
package org.openhab.binding.ecowatt.internal.restapi;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EcowattDaySignals} class contains fields mapping the content of each value of JSON table "signals" inside
 * the API response
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class EcowattDaySignals {
    @SerializedName("GenerationFichier")
    public @Nullable ZonedDateTime fileTimestamp;
    @SerializedName("jour")
    public @Nullable ZonedDateTime day;
    @SerializedName("dvalue")
    public int value;
    public @Nullable String message;
    public @Nullable List<EcowattHourSignal> values;

    public @Nullable ZonedDateTime getDay() {
        return day;
    }

    public int getDaySignal() {
        return value;
    }

    public @Nullable String getDayMessage() {
        return message;
    }

    public int getHourSignal(int hour) {
        List<EcowattHourSignal> localValues = values;
        if (localValues != null) {
            for (EcowattHourSignal hourSignal : localValues) {
                if (hourSignal.hour == hour) {
                    return hourSignal.value;
                }
            }
        }
        return 0;
    }
}
