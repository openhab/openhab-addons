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
package org.openhab.binding.airgradient.internal.communication;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.model.Measure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Helper for parsing JSON.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class JsonParserHelper {

    public static List<Measure> parseJson(Gson gson, String stringResponse) {
        List<@Nullable Measure> measures = null;
        if (stringResponse.startsWith("[")) {
            // Array of measures, like returned from the AirGradients API
            Type measuresType = new TypeToken<List<@Nullable Measure>>() {
            }.getType();
            measures = gson.fromJson(stringResponse, measuresType);
        } else if (stringResponse.startsWith("{")) {
            // Single measure e.g. if you read directly from the device
            Type measureType = new TypeToken<Measure>() {
            }.getType();
            Measure measure = gson.fromJson(stringResponse, measureType);
            measures = new ArrayList<>(1);
            measures.add(measure);
        }

        if (measures != null) {
            List<@Nullable Measure> nullableMeasuresWithoutNulls = measures.stream().filter(Objects::nonNull).toList();
            List<Measure> measuresWithoutNulls = new ArrayList<>(nullableMeasuresWithoutNulls.size());
            for (@Nullable
            Measure m : nullableMeasuresWithoutNulls) {
                if (m != null) {
                    measuresWithoutNulls.add(m);
                }
            }

            return measuresWithoutNulls;
        }

        return Collections.emptyList();
    }
}
