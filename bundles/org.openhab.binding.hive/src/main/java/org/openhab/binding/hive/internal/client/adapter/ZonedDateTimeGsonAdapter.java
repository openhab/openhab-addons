/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link ZonedDateTime} as used by the
 * Hive API.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class ZonedDateTimeGsonAdapter extends GsonTypeAdapterBase<ZonedDateTime> {
    private static final DateTimeFormatter HIVE_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public void writeValue(final JsonWriter out, final @Nullable ZonedDateTime zonedDateTime) throws IOException {
        if (zonedDateTime != null) {
            out.value(zonedDateTime.format(HIVE_DATETIME_FORMAT));
        } else {
            out.nullValue();
        }
    }

    @Override
    public @Nullable ZonedDateTime readValue(final JsonReader in) throws IOException {
        return ZonedDateTime.parse(in.nextString(), HIVE_DATETIME_FORMAT);
    }
}
