/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.impl;

import org.openhab.binding.sleepiq.api.enums.Side;
import org.openhab.binding.sleepiq.api.impl.typeadapters.JSR310TypeAdapters;
import org.openhab.binding.sleepiq.api.impl.typeadapters.SideTypeAdapter;
import org.openhab.binding.sleepiq.api.impl.typeadapters.SleepNumberRequestAdapter;
import org.openhab.binding.sleepiq.api.impl.typeadapters.TimeSinceTypeAdapter;
import org.openhab.binding.sleepiq.api.model.SleepNumberRequest;
import org.openhab.binding.sleepiq.api.model.TimeSince;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonGenerator {
    public static Gson create() {
        return create(false);
    }

    public static Gson create(boolean prettyPrint) {
        GsonBuilder builder = new GsonBuilder();

        // add Java 8 Time API support
        JSR310TypeAdapters.registerJSR310TypeAdapters(builder);
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
