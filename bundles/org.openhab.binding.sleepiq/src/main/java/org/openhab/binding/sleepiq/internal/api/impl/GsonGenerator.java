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
package org.openhab.binding.sleepiq.internal.api.impl;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sleepiq.internal.api.dto.SleepNumberRequest;
import org.openhab.binding.sleepiq.internal.api.dto.TimeSince;
import org.openhab.binding.sleepiq.internal.api.enums.Side;
import org.openhab.binding.sleepiq.internal.api.impl.typeadapters.SideTypeAdapter;
import org.openhab.binding.sleepiq.internal.api.impl.typeadapters.SleepNumberRequestAdapter;
import org.openhab.binding.sleepiq.internal.api.impl.typeadapters.TimeSinceTypeAdapter;
import org.openhab.binding.sleepiq.internal.api.impl.typeadapters.ZonedDateTimeTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link GsonGenerator} class creates the GSON generator object.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class GsonGenerator {
    public static Gson create() {
        return create(false);
    }

    public static Gson create(boolean prettyPrint) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter());
        builder.registerTypeAdapter(TimeSince.class, new TimeSinceTypeAdapter());
        builder.registerTypeAdapter(SleepNumberRequest.class, new SleepNumberRequestAdapter());
        builder.registerTypeAdapter(Side.class, new SideTypeAdapter());

        if (prettyPrint) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }

    private GsonGenerator() {
    }
}
