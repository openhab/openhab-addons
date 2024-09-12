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
package org.openhab.binding.lametrictime.internal.api.common.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.ActionTypeAdapterFactory;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.ApplicationTypeAdapterFactory;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.UpdateActionTypeAdapterFactory;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported.JSR310TypeAdapters;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported.RuntimeTypeAdapterFactory;
import org.openhab.binding.lametrictime.internal.api.local.dto.BooleanParameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.IntegerParameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.Parameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.StringParameter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class for json generation support.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class GsonGenerator {
    public static Gson create() {
        return create(false);
    }

    public static Gson create(boolean prettyPrint) {
        GsonBuilder builder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapterFactory(new ApplicationTypeAdapterFactory())
                .registerTypeAdapterFactory(new ActionTypeAdapterFactory())
                .registerTypeAdapterFactory(new UpdateActionTypeAdapterFactory())
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(Parameter.class, "data_type")
                        .registerSubtype(BooleanParameter.class, "bool")
                        .registerSubtype(StringParameter.class, "string")
                        .registerSubtype(IntegerParameter.class, "int"));

        // add Java 8 Time API support
        JSR310TypeAdapters.registerJSR310TypeAdapters(builder);

        if (prettyPrint) {
            builder.setPrettyPrinting();
        }

        return builder.create();
    }

    // @formatter:off
    private GsonGenerator() {}
    // @formatter:on
}
