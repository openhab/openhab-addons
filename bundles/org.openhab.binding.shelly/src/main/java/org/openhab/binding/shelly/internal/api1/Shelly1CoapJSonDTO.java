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
package org.openhab.binding.shelly.internal.api1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link Shelly1CoapJSonDTO} helps the CoIoT Json into Java objects
 *
 * @author Markus Michels - Initial contribution
 */
public class Shelly1CoapJSonDTO {
    // Coap
    public static final int COIOT_VERSION_1 = 1;
    public static final int COIOT_VERSION_2 = 2;

    public static final int COIOT_PORT = 5683;
    public static final String COAP_MULTICAST_ADDRESS = "224.0.1.187";

    public static final String COLOIT_URI_BASE = "/cit/";
    public static final String COLOIT_URI_DEVDESC = COLOIT_URI_BASE + "d";
    public static final String COLOIT_URI_DEVSTATUS = COLOIT_URI_BASE + "s";

    public static final int COIOT_OPTION_GLOBAL_DEVID = 3332;
    public static final int COIOT_OPTION_STATUS_VALIDITY = 3412;
    public static final int COIOT_OPTION_STATUS_SERIAL = 3420;

    public static final String COIOT_TAG_BLK = "blk";
    public static final String COIOT_TAG_SEN = "sen";
    public static final String COIOT_TAG_ACT = "act";
    public static final String COIOT_TAG_GENERIC = "G";

    public static class CoIotDescrBlk {
        @SerializedName("I")
        String id; // ID
        @SerializedName("D")
        String desc; // Description

        // Sometimes sen entries are part of the blk array - not conforming the Spec!
        @SerializedName("T")
        public String type; // Type
        @SerializedName("R")
        public String range; // Range
        @SerializedName("L")
        public String links; // Links
    }

    public static class CoIotDescrSen {
        @SerializedName("I")
        String id; // ID
        @SerializedName("D")
        String desc = ""; // Description
        @SerializedName("T")
        public String type; // Type
        @SerializedName("R")
        public String range; // Range
        @SerializedName("L")
        public String links; // Links
        @SerializedName("U")
        public String unit; // Unit
    }

    public static class CoIotDescrP {
        @SerializedName("I")
        String id; // ID
        @SerializedName("D")
        String desc; // Description
        @SerializedName("R")
        public String range; // Range
    }

    public static class CoIotDescrAct {
        @SerializedName("I")
        String id; // ID
        @SerializedName("D")
        String desc; // Description
        @SerializedName("L")
        public String links; // Links
        @SerializedName("P")
        public List<CoIotDescrP> pTag; // ?
    }

    public static class CoIotDevDescription {
        public List<CoIotDescrBlk> blk;
        public List<CoIotDescrSen> sen;
        // public List<CoIotDescrAct> act;

        public CoIotDevDescription() {
            blk = new ArrayList<>();
            sen = new ArrayList<>();
        }
    }

    public static class CoIotSensor {
        @SerializedName("index")
        public String id; // id
        public double value; // value
        public String valueStr; // value
        public List<Object> valueArray;
    }

    public static class CoIotGenericSensorList {
        @SerializedName("G")
        public List<CoIotSensor> generic;

        public CoIotGenericSensorList() {
            generic = new ArrayList<>();
        }
    }

    protected static class CoIotDevDescrTypeAdapter extends TypeAdapter<CoIotDevDescription> {
        @Override
        public CoIotDevDescription read(final JsonReader in) throws IOException {
            CoIotDevDescription descr = new CoIotDevDescription();

            /*
             * parse JSON like
             * "blk": [
             * { "I": 0, "D": "Relay0"},
             * { "I": 1, "D": "Sensors"} ],
             * "sen": [
             * { "I": 111, "T": "P", "D": "Power","R": "0/3500","L": 0},
             * { "I": 112,"T": "S","D": "Switch","R": "0/1","L": 0}
             * ]
             */
            in.beginObject();
            String name = in.nextName();
            if (name.equalsIgnoreCase(COIOT_TAG_BLK)) {
                in.beginArray();
                while (in.hasNext()) {
                    CoIotDescrBlk blk = new CoIotDescrBlk();
                    in.beginObject();
                    while (in.hasNext()) {
                        switch (in.nextName().toUpperCase()) {
                            case "I":
                                blk.id = in.nextString();
                                break;
                            case "D":
                                blk.desc = in.nextString();
                                break;
                            default:
                                // skip data
                                in.nextNull();
                        }
                    }
                    in.endObject();
                    descr.blk.add(blk);
                }
                in.endArray();
                name = in.nextName();
            }

            if (name.equalsIgnoreCase(COIOT_TAG_SEN)) {
                /*
                 * parse sensor list, e.g.
                 * "sen":[
                 * { "I":111,"T":"Red","R":"0/255","L":0},
                 * { "I":121,"T":"Green","R":"0/255","L":0},
                 * ]
                 */
                in.beginArray();
                while (in.hasNext()) {
                    CoIotDescrSen sen = new CoIotDescrSen();
                    in.beginObject();
                    while (in.hasNext()) {
                        String tag = in.nextName();
                        switch (tag.toUpperCase()) {
                            case "I":
                                sen.id = in.nextString();
                                break;
                            case "D":
                                sen.desc = in.nextString();
                                break;
                            case "T":
                                sen.type = in.nextString();
                                break;
                            case "R":
                                JsonToken token = in.peek();
                                if (token == JsonToken.BEGIN_ARRAY) {
                                    // must be v2: an array
                                    in.beginArray();
                                    sen.range = "";
                                    while (in.hasNext()) {
                                        String value = in.nextString();
                                        sen.range += sen.range.isEmpty() ? value : ";" + value;
                                    }
                                    in.endArray();
                                } else {
                                    sen.range = in.nextString();
                                }
                                break;
                            case "L":
                                sen.links = String.valueOf(in.nextInt());
                                break;
                            case "U": // New in CoAPv2: unit"
                                sen.unit = in.nextString();
                                break;
                            default:
                                // skip data
                                in.nextNull();
                        }
                    }
                    in.endObject();
                    descr.sen.add(sen);
                }

                in.endArray();
                if (in.hasNext()) {
                    name = in.nextName();
                }
            }

            if (name.equalsIgnoreCase(COIOT_TAG_ACT)) {
                // skip record
                in.skipValue();
            }

            in.endObject();
            return descr;
        }

        @Override
        public void write(final JsonWriter out, final CoIotDevDescription descr) throws IOException {
            out.beginObject();
            if (descr != null) {
                out.name(COIOT_TAG_BLK).beginArray();
                for (int i = 0; i < descr.blk.size(); i++) {
                    CoIotDescrBlk blk = descr.blk.get(i);
                    out.beginArray();
                    out.value(blk.id);
                    out.value(blk.desc);
                    out.endArray();
                }
                out.endArray();

                out.name(COIOT_TAG_SEN).beginArray();
                for (int i = 0; i < descr.sen.size(); i++) {
                    // Create element, e.g. {“I”:66, “D”:“lux”, “T”:“L”, “R”:“0/100000”, “L”:1},
                    CoIotDescrSen sen = descr.sen.get(i);
                    out.beginArray();
                    out.value(sen.id);
                    out.value(sen.desc);
                    out.value(sen.type);
                    out.value(sen.range);
                    out.value(sen.links);
                    if (sen.unit != null) {
                        out.value(sen.unit);
                    }
                    out.endArray();
                }
                out.endArray();
            }
            out.endObject();
        }
    }

    protected static class CoIotSensorTypeAdapter extends TypeAdapter<CoIotGenericSensorList> {
        @Override
        public CoIotGenericSensorList read(final JsonReader in) throws IOException {
            CoIotGenericSensorList list = new CoIotGenericSensorList();

            in.beginObject();
            String generic = in.nextName();
            if (generic.equals(COIOT_TAG_GENERIC)) {
                in.beginArray();
                while (in.hasNext()) {
                    CoIotSensor sensor = new CoIotSensor();
                    in.beginArray();
                    in.nextInt(); // alway 0
                    sensor.id = Integer.toString(in.nextInt());
                    JsonToken token = in.peek();
                    if (token == JsonToken.STRING) {
                        // handle as string
                        sensor.valueStr = in.nextString();
                        sensor.value = -1;
                    } else if (token == JsonToken.NUMBER) {
                        // handle as double
                        sensor.value = in.nextDouble();
                        sensor.valueStr = "";
                    } else if (token == JsonToken.BEGIN_ARRAY) {
                        sensor.valueArray = new ArrayList<>();
                        in.beginArray();
                        while (in.hasNext()) {
                            if (in.peek() == JsonToken.STRING) {
                                sensor.valueArray.add(in.nextString());
                            } else {
                                // skip
                                in.nextNull();
                            }
                        }
                        in.endArray();
                    }
                    in.endArray();
                    list.generic.add(sensor);
                }
                in.endArray();
            }
            in.endObject();

            return list;
        }

        @Override
        public void write(final JsonWriter out, final CoIotGenericSensorList o) throws IOException {
            CoIotGenericSensorList sensors = o;
            out.beginObject();
            if (sensors != null) {
                out.name(COIOT_TAG_GENERIC).beginArray();
                for (int i = 0; i < sensors.generic.size(); i++) {
                    out.beginArray();
                    out.value(0);
                    out.value(sensors.generic.get(i).id);
                    out.value(sensors.generic.get(i).value);
                    out.endArray();
                }
                out.endArray();
            }
            out.endObject();
        }
    }
}
