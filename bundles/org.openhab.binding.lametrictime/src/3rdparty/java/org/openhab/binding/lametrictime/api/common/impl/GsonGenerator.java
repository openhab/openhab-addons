/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
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
package org.openhab.binding.lametrictime.api.common.impl;

import org.openhab.binding.lametrictime.api.common.impl.typeadapters.ActionTypeAdapterFactory;
import org.openhab.binding.lametrictime.api.common.impl.typeadapters.ApplicationTypeAdapterFactory;
import org.openhab.binding.lametrictime.api.common.impl.typeadapters.UpdateActionTypeAdapterFactory;
import org.openhab.binding.lametrictime.api.common.impl.typeadapters.imported.JSR310TypeAdapters;
import org.openhab.binding.lametrictime.api.common.impl.typeadapters.imported.RuntimeTypeAdapterFactory;
import org.openhab.binding.lametrictime.api.local.model.BooleanParameter;
import org.openhab.binding.lametrictime.api.local.model.IntegerParameter;
import org.openhab.binding.lametrictime.api.local.model.Parameter;
import org.openhab.binding.lametrictime.api.local.model.StringParameter;

import com.google.gson.FieldNamingPolicy;
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
        GsonBuilder builder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                               .registerTypeAdapterFactory(new ApplicationTypeAdapterFactory())
                                               .registerTypeAdapterFactory(new ActionTypeAdapterFactory())
                                               .registerTypeAdapterFactory(new UpdateActionTypeAdapterFactory())
                                               .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(Parameter.class,
                                                                                                        "data_type")
                                                                                                    .registerSubtype(BooleanParameter.class,
                                                                                                                     "bool")
                                                                                                    .registerSubtype(StringParameter.class,
                                                                                                                     "string")
                                                                                                    .registerSubtype(IntegerParameter.class,
                                                                                                                     "int"));

        // add Java 8 Time API support
        JSR310TypeAdapters.registerJSR310TypeAdapters(builder);

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
