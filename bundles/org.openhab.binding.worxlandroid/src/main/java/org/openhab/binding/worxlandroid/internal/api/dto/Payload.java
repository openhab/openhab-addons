/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.api.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidErrorCodes;
import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidStatusCodes;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Payload {
    public class US {
        public int enabled;
        public String stat;
    }

    public class Ots {
        @SerializedName("wtm")
        public int duration = -1;
        private int bc = -1;

        public boolean getEdgeCut() {
            return bc == 1;
        }
    }

    public class Al {
        public int lvl;
        public int t;
    }

    public class Modules {
        @SerializedName("US")
        public US uS;
    }

    public class Rain {
        @SerializedName("s")
        public Boolean raining;
        @SerializedName("cnt")
        public int counter = -1;
    }

    public class Schedule {
        public static enum Mode {
            @SerializedName("1")
            NORMAL,
            @SerializedName("2")
            PARTY,
            UNKNOWN
        }

        @SerializedName("m")
        public Mode scheduleMode = Mode.UNKNOWN;
        @SerializedName("p")
        public int timeExtension = -1;
        public int distm;
        public Ots ots;
        public List<List<String>> d;
        public List<List<String>> dd;
    }

    public class Battery {
        @SerializedName("t")
        public double temp = -1;
        @SerializedName("v")
        public double voltage = -1;
        @SerializedName("p")
        public int level = -1;
        @SerializedName("nr")
        public int chargeCycle = -1;
        @SerializedName("c")
        public Boolean charging;
        public int m;
    }

    public class Stat {
        @SerializedName("b")
        public int bladeWorkTime = -1;
        @SerializedName("d")
        public int distanceCovered = -1;
        @SerializedName("wt")
        public int mowerWorkTime = -1;
        @SerializedName("bl")
        public int lawnPerimeter;
    }

    public class Cfg {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        private String dt = ""; // "dt": "13/03/2020",
        private String tm = ""; // "tm": "17:09:34"

        public int id = -1;
        public String lg = ""; // en, fr...
        public int cmd = -1;
        public Schedule sc;
        @SerializedName("mz")
        public List<Integer> multiZones = List.of();
        @SerializedName("mzv")
        public List<Integer> multizoneAllocations = List.of();
        @SerializedName("rd")
        public int rainDelay = -1;
        @SerializedName("sn")
        public String serialNumber = "";
        public int mzk;
        public Al al;
        public int tq;
        public Modules modules;

        public ZonedDateTime getDateTime(ZoneId zoneId) {
            return dt.isEmpty() || tm.isEmpty() ? null
                    : ZonedDateTime.of(LocalDateTime.parse("%s %s".formatted(dt, tm), FORMATTER), zoneId);
        }
    }

    public class Dat {
        public static enum Axis {
            // Don't change order - ordinal is used
            PITCH,
            ROLL,
            YAW;
        }

        private int lk = -1;
        @SerializedName("dmp")
        private double[] dataMotionProcessor = { -1, -1, -1 }; // pitch, roll, yaw

        public String mac = "";
        public String fw = "";
        @SerializedName("bt")
        public Battery battery;
        public Stat st;
        @SerializedName("ls")
        public WorxLandroidStatusCodes statusCode = WorxLandroidStatusCodes.UNKNOWN;
        @SerializedName("le")
        public WorxLandroidErrorCodes errorCode = WorxLandroidErrorCodes.UNKNOWN;
        @SerializedName("lz")
        public int lastZone = -1;
        @SerializedName("rsi")
        public int wifiQuality;
        public int fwb;
        public String conn;
        public int act;
        public int tr;
        public Rain rain;
        public Modules modules;

        public boolean isLocked() {
            return lk == 1;
        }

        public double getAngle(Axis axis) {
            return dataMotionProcessor[axis.ordinal()];
        }
    }

    public Cfg cfg;
    public Dat dat;
}
