/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.api.impl;

import org.openhab.binding.sleepiq.api.impl.typeadapters.JSR310TypeAdapters;
import org.openhab.binding.sleepiq.api.impl.typeadapters.TimeSinceTypeAdapter;
import org.openhab.binding.sleepiq.api.model.TimeSince;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonGenerator
{
    public static Gson create()
    {
        return create(false);
    }

    public static Gson create(boolean prettyPrint)
    {
        GsonBuilder builder = new GsonBuilder();

        // add Java 8 Time API support
        JSR310TypeAdapters.registerJSR310TypeAdapters(builder);

        builder.registerTypeAdapter(TimeSince.class, new TimeSinceTypeAdapter());

        if (prettyPrint)
        {
            builder.setPrettyPrinting();
        }

        return builder.create();
    }

    // @formatter:off
    private GsonGenerator() {}
    // @formatter:on
}
