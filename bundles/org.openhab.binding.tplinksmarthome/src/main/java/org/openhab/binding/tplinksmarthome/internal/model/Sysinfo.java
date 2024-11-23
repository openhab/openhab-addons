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
package org.openhab.binding.tplinksmarthome.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for reading TP-Link Smart Home device state.
 * Only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class Sysinfo extends ErrorResponse {

    public static class CtrlProtocols {
        private String name;
        private String version;

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return "name:" + name + ", version:" + version;
        }
    }

    /**
     * With default light state state. The default light state is set when the device is off. If the device is on the
     * state is in the parent fields.
     */
    public static class WithDefaultLightState extends LightState {
        private LightState dftOnState;

        public LightState getLightState() {
            if (dftOnState == null) {
                return this;
            } else {
                dftOnState.setOnOff(getOnOff());
                return dftOnState;
            }
        }

        @Override
        public String toString() {
            return super.toString() + ", dftOnState:{" + dftOnState + "}";
        }
    }

    /**
     * Status of the plug as set in the range extender products.
     */
    public static class Plug {
        private String feature;
        private String relayStatus;

        public String getFeature() {
            return feature;
        }

        public OnOffType getRelayStatus() {
            return OnOffType.from("ON".equals(relayStatus));
        }
    }

    /**
     * Status of a single outlet on power strip.
     */
    public static class Outlet {
        private String alias;
        private String id;
        private long onTime;
        private int state;

        public String getAlias() {
            return alias;
        }

        public String getId() {
            return id;
        }

        public long getOnTime() {
            return onTime;
        }

        public OnOffType getState() {
            return OnOffType.from(state == 1);
        }
    }

    /**
     * Status of the range extended Wi-Fi.
     */
    public static class RangeextenderWireless {
        private int w2gRssi;

        public int getW2gRssi() {
            return w2gRssi;
        }
    }

    private String swVer;
    private String hwVer;
    private String model;
    @SerializedName("deviceId")
    private String deviceId;
    @SerializedName("hwId")
    private String hwId;
    @SerializedName("oemId")
    private String oemId;
    private String alias;
    private String activeMode;
    private int rssi;
    @SerializedName(value = "type", alternate = "mic_type")
    private String type;
    @SerializedName(value = "mac", alternate = { "mic_mac", "ethernet_mac" })
    private String mac;

    // switch and plug specific system info
    @SerializedName("fwId")
    private String fwId;
    private String devName;
    private String iconHash;
    private int relayState; // 0 is off, 1 is on
    private long onTime;
    private String feature; // HS100 -> TIM, HS110 -> TIM:ENE
    // Disabled updating as it's a different type for different devices.
    // private int updating;
    private int ledOff;
    private double latitude;
    private double longitude;

    // powerstrip/multiple plugs support.
    private int childNum;
    private List<Outlet> children = new ArrayList<>();

    // dimmer specific system info
    private int brightness;

    // bulb specific system info
    private boolean isFactory;
    private String discoVer;
    private CtrlProtocols ctrlProtocols;
    private WithDefaultLightState lightState = new WithDefaultLightState();

    // range extender specific system info
    private String ledStatus;
    private Plug plug = new Plug();
    private Sysinfo system;
    @SerializedName("rangeextender.wireless")
    private RangeextenderWireless reWireless;

    public String getSwVer() {
        return swVer;
    }

    public String getHwVer() {
        return hwVer;
    }

    public String getType() {
        return type;
    }

    public String getModel() {
        return model;
    }

    public String getMac() {
        return mac;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getHwId() {
        return hwId;
    }

    public String getFwId() {
        return fwId;
    }

    public String getOemId() {
        return oemId;
    }

    public String getAlias() {
        return alias;
    }

    public String getDevName() {
        return devName;
    }

    public String getIconHash() {
        return iconHash;
    }

    public OnOffType getRelayState() {
        return OnOffType.from(relayState == 1);
    }

    public int getBrightness() {
        return brightness;
    }

    public long getOnTime() {
        return onTime;
    }

    public String getActiveMode() {
        return activeMode;
    }

    public String getFeature() {
        return feature;
    }

    public int getRssi() {
        // for range extender use the 2g rssi.
        return reWireless == null ? rssi : reWireless.getW2gRssi();
    }

    public OnOffType getLedOff() {
        return OnOffType.from(ledOff != 1);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isFactory() {
        return isFactory;
    }

    public String getDiscoVer() {
        return discoVer;
    }

    public String getProtocolName() {
        return ctrlProtocols == null ? null : ctrlProtocols.getName();
    }

    public String getProtocolVersion() {
        return ctrlProtocols == null ? null : ctrlProtocols.getVersion();
    }

    public LightState getLightState() {
        return lightState.getLightState();
    }

    public OnOffType getLedStatus() {
        return OnOffType.from(!"ON".equals(ledStatus));
    }

    public Plug getPlug() {
        return plug;
    }

    public int getChildNum() {
        return childNum;
    }

    public List<Outlet> getChildren() {
        return children;
    }

    public Sysinfo getSystem() {
        return system;
    }

    /**
     * Returns the {@link Sysinfo} object independent of the device. The range extender devices have the system
     * information in another place as the other devices. This method returns the object independent of how the device
     * returns it.
     *
     * @return device independent {@link Sysinfo} object.
     */
    public Sysinfo getActualSysinfo() {
        return system == null ? this : system;
    }

    public RangeextenderWireless getReWireless() {
        return reWireless;
    }

    @Override
    public String toString() {
        return "Sysinfo [swVer=" + swVer + ", hwVer=" + hwVer + ", model=" + model + ", deviceId=" + deviceId
                + ", hwId=" + hwId + ", oemId=" + oemId + ", alias=" + alias + ", activeMode=" + activeMode + ", rssi="
                + rssi + ", type=" + type + ", mac=" + mac + ", fwId=" + fwId + ", devName=" + devName + ", iconHash="
                + iconHash + ", relayState=" + relayState + ", brightness=" + brightness + ", onTime=" + onTime
                + ", feature=" + feature + ", ledOff=" + ledOff + ", latitude=" + latitude + ", longitude=" + longitude
                + ", isFactory=" + isFactory + ", discoVer=" + discoVer + ", ctrlProtocols=" + ctrlProtocols
                + ", lightState=" + lightState + ", ledStatus=" + ledStatus + ", plug=" + plug + ", system=" + system
                + ", reWireless=" + reWireless + "]";
    }
}
