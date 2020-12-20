/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.irobot.internal.dto;

/**
 * iRobot MQTT protocol messages
 *
 * @author Pavel Fedin - Initial contribution
 *
 */
public class MQTTProtocol {
    public interface Request {
        public String getTopic();
    }

    public static class CommandRequest implements Request {
        public String command;
        public long time;
        public String initiator;

        public CommandRequest(String cmd) {
            command = cmd;
            time = System.currentTimeMillis() / 1000;
            initiator = "localApp";
        }

        @Override
        public String getTopic() {
            return "cmd";
        }
    }

    public static class DeltaRequest implements Request {
        public StateValue state;

        public DeltaRequest(StateValue state) {
            this.state = state;
        }

        @Override
        public String getTopic() {
            return "delta";
        }
    }

    public static class CleanMissionStatus {
        public String cycle;
        public String phase;
        public int error;
    }

    public static class BinStatus {
        public boolean present;
        public boolean full;
    }

    public static class SignalStrength {
        public int rssi;
        public int snr;
    }

    public static class Schedule {
        public String[] cycle;
        public int[] h;
        public int[] m;

        public static final int NUM_WEEK_DAYS = 7;

        public Schedule(int cycles_bitmask) {
            cycle = new String[NUM_WEEK_DAYS];
            for (int i = 0; i < NUM_WEEK_DAYS; i++) {
                enableCycle(i, (cycles_bitmask & (1 << i)) != 0);
            }
        }

        public Schedule(String[] cycle) {
            this.cycle = cycle;
        }

        public boolean cycleEnabled(int i) {
            return cycle[i].equals("start");
        }

        public void enableCycle(int i, boolean enable) {
            cycle[i] = enable ? "start" : "none";
        }
    }

    public static class StateValue {
        // Just some common type, nothing to do here
        protected StateValue() {
        }
    }

    public static class OpenOnly extends StateValue {
        public boolean openOnly;

        public OpenOnly(boolean openOnly) {
            this.openOnly = openOnly;
        }
    }

    public static class BinPause extends StateValue {
        public boolean binPause;

        public BinPause(boolean binPause) {
            this.binPause = binPause;
        }
    }

    public static class PowerBoost extends StateValue {
        public boolean carpetBoost;
        public boolean vacHigh;

        public PowerBoost(boolean carpetBoost, boolean vacHigh) {
            this.carpetBoost = carpetBoost;
            this.vacHigh = vacHigh;
        }
    }

    public static class CleanPasses extends StateValue {
        public boolean noAutoPasses;
        public boolean twoPass;

        public CleanPasses(boolean noAutoPasses, boolean twoPass) {
            this.noAutoPasses = noAutoPasses;
            this.twoPass = twoPass;
        }
    }

    public static class CleanSchedule extends StateValue {
        public Schedule cleanSchedule;

        public CleanSchedule(Schedule schedule) {
            cleanSchedule = schedule;
        }
    }

    // "reported" messages never contain the full state, only a part.
    // Therefore all the fields in this class are nullable
    public static class GenericState extends StateValue {
        // "cleanMissionStatus":{"cycle":"clean","phase":"hmUsrDock","expireM":0,"rechrgM":0,"error":0,"notReady":0,"mssnM":1,"sqft":7,"initiator":"rmtApp","nMssn":39}
        public CleanMissionStatus cleanMissionStatus;
        // "batPct":100
        public Integer batPct;
        // "bin":{"present":true,"full":false}
        public BinStatus bin;
        // "signal":{"rssi":-55,"snr":33}
        public SignalStrength signal;
        // "cleanSchedule":{"cycle":["none","start","start","start","start","none","none"],"h":[9,12,12,12,12,12,9],"m":[0,0,0,0,0,0,0]}
        public Schedule cleanSchedule;
        // "openOnly":false
        public Boolean openOnly;
        // "binPause":true
        public Boolean binPause;
        // "carpetBoost":true
        public Boolean carpetBoost;
        // "vacHigh":false
        public Boolean vacHigh;
        // "noAutoPasses":true
        public Boolean noAutoPasses;
        // "twoPass":true
        public Boolean twoPass;
        // "softwareVer":"v2.4.6-3"
        public String softwareVer;
        // "navSwVer":"01.12.01#1"
        public String navSwVer;
        // "wifiSwVer":"20992"
        public String wifiSwVer;
        // "mobilityVer":"5806"
        public String mobilityVer;
        // "bootloaderVer":"4042"
        public String bootloaderVer;
        // "umiVer":"6",
        public String umiVer;
    }

    // Data comes as JSON string: {"state":{"reported":<Actual content here>}}
    // or: {"state":{"desired":<Some content here>}}
    // Of the second form i've so far observed only: {"state":{"desired":{"echo":null}}}
    // I don't know what it is, so let's ignore it.
    public static class ReportedState {
        public GenericState reported;
    }

    public static class StateMessage {
        public ReportedState state;
    }
};
