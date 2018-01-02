/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.model;

import org.eclipse.smarthome.core.library.types.OnOffType;

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
     * Alternative light state for different devices.
     */
    public static class ExtendedLightState extends LightState {
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

    // switch and plug specific system info
    private String type;
    private String mac;
    @SerializedName("fwId")
    private String fwId;
    private String devName;
    private String iconHash;
    private int relayState; // 0 is off, 1 is on
    private long onTime;
    private String feature; // HS100 -> TIM, HS110 -> TIM:ENE
    private int updating;
    private int ledOff;
    private double latitude;
    private double longitude;

    // bulb specific system info
    private String mic_type;
    private String mic_mac;
    private boolean isFactory;
    private String discoVer;
    private CtrlProtocols ctrlProtocols;
    private ExtendedLightState lightState = new ExtendedLightState();

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
        return relayState == 1 ? OnOffType.ON : OnOffType.OFF;
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

    public int getUpdating() {
        return updating;
    }

    public int getRssi() {
        return rssi;
    }

    public OnOffType getLedOff() {
        return ledOff == 1 ? OnOffType.OFF : OnOffType.ON;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getMicType() {
        return mic_type;
    }

    public String getMicMac() {
        return mic_mac;
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

    @Override
    public String toString() {
        return "Sysinfo {swVer:" + swVer + ", hwVer:" + hwVer + ", model:" + model + ", deviceId:" + deviceId
                + ", hwId:" + hwId + ", oemId:" + oemId + ", alias:" + alias + ", activeMode:" + activeMode + ", rssi:"
                + rssi + ", type:" + type + ", mac:" + mac + ", fwId:" + fwId + ", devName:" + devName + ", iconHash:"
                + iconHash + ", relayState:" + relayState + ", onTime:" + onTime + ", feature:" + feature
                + ", updating:" + updating + ", ledOff:" + ledOff + ", latitude:" + latitude + ", longitude:"
                + longitude + ", mic_type:" + mic_type + ", mic_mac:" + mic_mac + ", isFactory:" + isFactory
                + ", discoVer:" + discoVer + ", ctrlProtocols:" + ctrlProtocols + ", lightState:" + lightState + "}"
                + super.toString();
    }
}
