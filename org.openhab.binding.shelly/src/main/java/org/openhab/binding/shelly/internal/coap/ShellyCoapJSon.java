/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.coap;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.Validate;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link ShellyCoapJSon} helps the CoIoT Json into Java objects
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyCoapJSon {

    public static final String CoIoT_Tag_blk = "blk";
    public static final String CoIoT_Tag_sen = "sen";
    public static final String CoIoT_Tag_act = "act";

    public static final String CoIoT_Tag_ID = "I";
    public static final String CoIoT_Tag_DESC = "D";
    public static final String CoIoT_Tag_TYPE = "T";
    public static final String CoIoT_Tag_RANGE = "R";
    public static final String CoIoT_Tag_LINKS = "L";

    public static final String CoIoT_Tag_Generic = "G";

    public static class CoIoT_Descr_blk {
        String I; // ID
        String D; // Description

        // Sometimes sen entries are part of the blk array - not conforming the Spec!
        public String T; // Type
        public String R; // Range
        public String L; // Links
    }

    public static class CoIoT_Descr_sen {
        public String I; // ID
        public String D; // Description
        public String T; // Type
        public String R; // Range
        public String L; // Links
    }

    public static class CoIoT_Descr_P {
        public String I; // ID
        public String D; // Description
        public String R; // Range
    }

    public static class CoIoT_Descr_act {
        public String I; // ID
        public String D; // Description
        public String L; // Links
        public ArrayList<CoIoT_Descr_P> P; // ?
    }

    public static class CoIoT_DevDescription {
        public ArrayList<CoIoT_Descr_blk> blk;
        public ArrayList<CoIoT_Descr_sen> sen;
        public ArrayList<CoIoT_Descr_act> act;
    }

    public static class CoIoT_Sensor {
        public String index; // id
        public double value; // value
    }

    public static class CoIoT_GenericSensorList {
        public ArrayList<CoIoT_Sensor> G;

        public CoIoT_GenericSensorList() {
            G = new ArrayList<CoIoT_Sensor>();
        }
    }

    protected static class CoIoT_SensorTypeAdapter extends TypeAdapter<CoIoT_GenericSensorList> {
        @Override
        public CoIoT_GenericSensorList read(final JsonReader in) throws IOException {
            CoIoT_GenericSensorList list = new CoIoT_GenericSensorList();

            in.beginObject();
            String G = in.nextName();
            Validate.notNull(G, "Invalid JSon format for CoIot_SensorList");
            if (G.equals(CoIoT_Tag_Generic)) {
                in.beginArray();
                while (in.hasNext()) {
                    in.beginArray();
                    final CoIoT_Sensor sensor = new CoIoT_Sensor();
                    in.nextInt(); // alway 0
                    sensor.index = new Integer(in.nextInt()).toString();
                    sensor.value = in.nextDouble();
                    in.endArray();
                    list.G.add(sensor);
                }
                in.endArray();
            }
            in.endObject();

            return list;
        }

        @Override
        public void write(final JsonWriter out, final CoIoT_GenericSensorList o) throws IOException {
            CoIoT_GenericSensorList sensors = o;
            out.beginObject();
            if (sensors != null) {
                out.name(CoIoT_Tag_Generic).beginArray();
                for (int i = 0; i < sensors.G.size(); i++) {
                    out.beginArray();
                    out.value(0);
                    out.value(sensors.G.get(i).index);
                    out.value(sensors.G.get(i).value);
                    out.endArray();
                }
                out.endArray();
            }
            out.endObject();
        }

    }

}
