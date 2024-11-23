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
package org.openhab.binding.tado.swagger.codegen.api;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.openhab.binding.tado.swagger.codegen.api.model.AirConditioningCapabilities;
import org.openhab.binding.tado.swagger.codegen.api.model.CoolingZoneSetting;
import org.openhab.binding.tado.swagger.codegen.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.swagger.codegen.api.model.GenericZoneSetting;
import org.openhab.binding.tado.swagger.codegen.api.model.HeatingCapabilities;
import org.openhab.binding.tado.swagger.codegen.api.model.HeatingZoneSetting;
import org.openhab.binding.tado.swagger.codegen.api.model.HotWaterCapabilities;
import org.openhab.binding.tado.swagger.codegen.api.model.HotWaterZoneSetting;
import org.openhab.binding.tado.swagger.codegen.api.model.ManualTerminationCondition;
import org.openhab.binding.tado.swagger.codegen.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.swagger.codegen.api.model.OverlayTerminationConditionTemplate;
import org.openhab.binding.tado.swagger.codegen.api.model.TadoModeTerminationCondition;
import org.openhab.binding.tado.swagger.codegen.api.model.TimerTerminationCondition;
import org.openhab.binding.tado.swagger.codegen.api.model.TimerTerminationConditionTemplate;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class GsonBuilderFactory {

    public static GsonBuilder defaultGsonBuilder() {
        return new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter())
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(GenericZoneCapabilities.class, "type")
                        .registerSubtype(HotWaterCapabilities.class, "HOT_WATER")
                        .registerSubtype(AirConditioningCapabilities.class, "AIR_CONDITIONING")
                        .registerSubtype(HeatingCapabilities.class, "HEATING"))
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(GenericZoneSetting.class, "type")
                        .registerSubtype(HotWaterZoneSetting.class, "HOT_WATER")
                        .registerSubtype(CoolingZoneSetting.class, "AIR_CONDITIONING")
                        .registerSubtype(HeatingZoneSetting.class, "HEATING"))
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(OverlayTerminationCondition.class, "type")
                        .registerSubtype(TadoModeTerminationCondition.class, "TADO_MODE")
                        .registerSubtype(TimerTerminationCondition.class, "TIMER")
                        .registerSubtype(ManualTerminationCondition.class, "MANUAL"))
                .registerTypeAdapterFactory(
                        RuntimeTypeAdapterFactory.of(OverlayTerminationConditionTemplate.class, "type")
                                .registerSubtype(TimerTerminationConditionTemplate.class, "TIMER"));
    }

    public static class OffsetDateTimeTypeAdapter extends TypeAdapter<OffsetDateTime> {

        private DateTimeFormatter formatter;

        public OffsetDateTimeTypeAdapter() {
            this(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        public OffsetDateTimeTypeAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public void setFormat(DateTimeFormatter dateFormat) {
            this.formatter = dateFormat;
        }

        @Override
        public void write(JsonWriter out, OffsetDateTime date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(date));
            }
        }

        @Override
        public OffsetDateTime read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    String date = in.nextString();
                    if (date.endsWith("+0000")) {
                        date = date.substring(0, date.length() - 5) + "Z";
                    }
                    return OffsetDateTime.parse(date, formatter);
            }
        }
    }
}
