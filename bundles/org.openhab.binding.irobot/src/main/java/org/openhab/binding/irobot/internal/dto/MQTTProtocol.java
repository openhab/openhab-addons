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
package org.openhab.binding.irobot.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * iRobot MQTT protocol messages
 *
 * @author Pavel Fedin - Initial contribution
 * @author Florian Binder - Added CleanRoomsRequest
 *
 */
public class MQTTProtocol {
    public interface Request {
        public String getTopic();
    }

    public static class CleanRoomsRequest extends CommandRequest {
        public int ordered;
        @SerializedName("pmap_id")
        public String pmapId;
        @SerializedName("user_pmapv_id")
        public String userPmapvId;
        public List<Region> regions;

        public CleanRoomsRequest(String cmd, String mapId, String[] pregions, String[] types, String userPmapvId) {
            super(cmd);
            ordered = 1;
            pmapId = mapId;
            this.userPmapvId = userPmapvId;

            regions = new ArrayList<Region>();
            for (int i = 0; (i < pregions.length) && (i < types.length); i++) {
                regions.add(new Region(pregions[i], types[i]));
            }
        }

        public static class Region {
            @SerializedName("region_id")
            public String regionId;
            public String type;

            public Region(String id, String type) {
                this.regionId = id;
                this.type = type;
            }
        }
    }

    public static class CommandRequest implements Request {
        public String command;
        public long time;
        public String initiator;

        public CommandRequest(String cmd) {
            command = cmd;
            time = System.currentTimeMillis() / 1000;
            initiator = "openhab";
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
        public String initiator;
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

    public static class MapUploadAllowed extends StateValue {
        public boolean mapUploadAllowed;

        public MapUploadAllowed(boolean mapUploadAllowed) {
            this.mapUploadAllowed = mapUploadAllowed;
        }
    }

    public static class SubModSwVer {
        public String nav;
        public String mob;
        public String pwr;
        public String sft;
        public String mobBtl;
        public String linux;
        public String con;
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
        // "mapUploadAllowed":true
        public Boolean mapUploadAllowed;
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
        // "sku":"R981040"
        public String sku;
        // "batteryType":"lith"
        public String batteryType;
        // Used by i7:
        // "subModSwVer":{
        // "nav": "lewis-nav+3.2.4-EPMF+build-HEAD-7834b608797+12", "mob":"3.2.4-XX+build-HEAD-7834b608797+12",
        // "pwr": "0.5.0+build-HEAD-7834b608797+12",
        // "sft":"1.1.0+Lewis-Builds/Lewis-Certified-Safety/lewis-safety-bbbe81f2c82+21",
        // "mobBtl": "4.2", "linux":"linux+2.1.6_lock-1+lewis-release-rt419+12",
        // "con":"2.1.6-tags/release-2.1.6@c6b6585a/build"}
        public SubModSwVer subModSwVer;
        // "lastCommand":
        // {"command":"start","initiator":"localApp","time":1610283995,"ordered":1,"pmap_id":"AAABBBCCCSDDDEEEFFF","regions":[{"region_id":"6","type":"rid"}]}
        public JsonElement lastCommand;
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

    // DISCOVERY
    public static class RobotCapabilities {
        public Integer pose;
        public Integer ota;
        public Integer multiPass;
        public Integer carpetBoost;
        public Integer pp;
        public Integer binFullDetect;
        public Integer langOta;
        public Integer maps;
        public Integer edge;
        public Integer eco;
        public Integer scvConf;
    }

    /*
     * JSON of the following contents (addresses are undisclosed):
     * @formatter:off
     * {
     *   "ver":"3",
     *   "hostname":"Roomba-<blid>",
     *   "robotname":"Roomba",
     *   "robotid":"<blid>", --> available on some models only
     *   "ip":"XXX.XXX.XXX.XXX",
     *   "mac":"XX:XX:XX:XX:XX:XX",
     *   "sw":"v2.4.6-3",
     *   "sku":"R981040",
     *   "nc":0,
     *   "proto":"mqtt",
     *   "cap":{
     *     "pose":1,
     *     "ota":2,
     *     "multiPass":2,
     *     "carpetBoost":1,
     *     "pp":1,
     *     "binFullDetect":1,
     *     "langOta":1,
     *     "maps":1,
     *     "edge":1,
     *     "eco":1,
     *     "svcConf":1
     *   }
     * }
     * @formatter:on
     */
    public static class DiscoveryResponse {
        public String ver;
        public String hostname;
        public String robotname;
        public String robotid;
        public String ip;
        public String mac;
        public String sw;
        public String sku;
        public String nc;
        public String proto;
        public RobotCapabilities cap;
    }

    // LoginRequester
    public static class BlidResponse {
        public String robotid;
        public String hostname;
    }
};
