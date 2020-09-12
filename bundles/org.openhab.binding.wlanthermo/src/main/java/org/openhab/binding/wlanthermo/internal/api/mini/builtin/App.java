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
package org.openhab.binding.wlanthermo.internal.api.mini.builtin;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_ALARM_DEVICE;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_HIGH;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_LOW;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_COLOR_NAME;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_MAX;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_MIN;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_NAME;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_PITMASTER_CHANNEL_ID;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_PITMASTER_CURRENT;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_PITMASTER_DUTY_CYCLE;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_PITMASTER_ENABLED;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_PITMASTER_LID_OPEN;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_PITMASTER_SETPOINT;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_TEMP;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.SYSTEM_CPU_LOAD;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.SYSTEM_CPU_TEMP;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_ALARM_MAX;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_ALARM_MIN;

import java.awt.Color;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wlanthermo.internal.WlanThermoMiniHandler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 * Be careful to not overwrite the getState/getTrigger function mapping the Data to OH channels!
 *
 * @author Christian Schlipp - Initial contribution
 */
public class App {

    @SerializedName("temp_unit")
    @Expose
    private String tempUnit;
    @SerializedName("pit")
    @Expose
    private Pit pit;
    @SerializedName("pit2")
    @Expose
    private Pit pit2;
    @SerializedName("cpu_load")
    @Expose
    private Double cpuLoad;
    @SerializedName("cpu_temp")
    @Expose
    private Double cpuTemp;
    @SerializedName("channel")
    @Expose
    private Channel channel;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;

    /**
     * No args constructor for use in serialization
     * 
     */
    public App() {
    }

    /**
     * 
     * @param cpuLoad
     * @param pit2
     * @param tempUnit
     * @param channel
     * @param pit
     * @param cpuTemp
     * @param timestamp
     */
    public App(String tempUnit, Pit pit, Pit pit2, Double cpuLoad, Double cpuTemp, Channel channel, String timestamp) {
        super();
        this.tempUnit = tempUnit;
        this.pit = pit;
        this.pit2 = pit2;
        this.cpuLoad = cpuLoad;
        this.cpuTemp = cpuTemp;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    public String getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
    }

    public App withTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
        return this;
    }

    public Pit getPit() {
        return pit;
    }

    public void setPit(Pit pit) {
        this.pit = pit;
    }

    public App withPit(Pit pit) {
        this.pit = pit;
        return this;
    }

    public Pit getPit2() {
        return pit2;
    }

    public void setPit2(Pit pit2) {
        this.pit2 = pit2;
    }

    public App withPit2(Pit pit2) {
        this.pit2 = pit2;
        return this;
    }

    public Double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(Double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public App withCpuLoad(Double cpuLoad) {
        this.cpuLoad = cpuLoad;
        return this;
    }

    public Double getCpuTemp() {
        return cpuTemp;
    }

    public void setCpuTemp(Double cpuTemp) {
        this.cpuTemp = cpuTemp;
    }

    public App withCpuTemp(Double cpuTemp) {
        this.cpuTemp = cpuTemp;
        return this;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public App withChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public App withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public State getState(ChannelUID channelUID, WlanThermoMiniHandler wlanThermoHandler) {
        State state = null;
        if (channelUID.getId().startsWith("system#")) {
            switch (channelUID.getIdWithoutGroup()) {
                case SYSTEM_CPU_TEMP:
                    if (getCpuTemp() == null) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = new DecimalType(getCpuTemp());
                    }
                    break;
                case SYSTEM_CPU_LOAD:
                    if (getCpuLoad() == null) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = new DecimalType(getCpuLoad());
                    }
                    break;
            }
        } else if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length()));
            if (channelId >= 0 && channelId <= 9) {
                Channel channel = getChannel();
                if (channel == null) {
                    return UnDefType.UNDEF;
                }
                Data data = channel.getData(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_NAME:
                        state = new StringType(data.getName());
                        break;
                    case CHANNEL_TEMP:
                        if (data.getState().equals("er")) {
                            state = UnDefType.UNDEF;
                        } else {
                            state = new DecimalType(data.getTemp());
                        }
                        break;
                    case CHANNEL_MIN:
                        state = new DecimalType(data.getTempMin());
                        break;
                    case CHANNEL_MAX:
                        state = new DecimalType(data.getTempMax());
                        break;
                    case CHANNEL_ALARM_DEVICE:
                        state = OnOffType.from(data.getAlert());
                        break;
                    case CHANNEL_ALARM_OPENHAB_HIGH:
                        if (!data.getState().equals("er") && data.getTemp() > data.getTempMax()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case CHANNEL_ALARM_OPENHAB_LOW:
                        if (!data.getState().equals("er") && data.getTemp() < data.getTempMin()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case CHANNEL_COLOR:
                        Color c = Color.decode(UtilMini.toHex(data.getColor()));
                        state = HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                        break;
                    case CHANNEL_COLOR_NAME:
                        state = new StringType(data.getColor());
                        break;
                }
            }
        } else if (channelUID.getId().startsWith("pit")) {
            Pit pit;
            if (channelUID.getGroupId().equals("pit1")) {
                pit = getPit();
            } else if (channelUID.getGroupId().equals("pit2")) {
                pit = getPit2();
            } else {
                return UnDefType.UNDEF;
            }
            if (pit == null || !pit.getEnabled()) {
                return UnDefType.UNDEF;
            }
            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_PITMASTER_ENABLED:
                    state = OnOffType.from(pit.getEnabled());
                    break;
                case CHANNEL_PITMASTER_CURRENT:
                    state = new DecimalType(pit.getCurrent());
                    break;
                case CHANNEL_PITMASTER_SETPOINT:
                    state = new DecimalType(pit.getSetpoint());
                    break;
                case CHANNEL_PITMASTER_DUTY_CYCLE:
                    state = new DecimalType(pit.getControlOut());
                    break;
                case CHANNEL_PITMASTER_LID_OPEN:
                    state = OnOffType.from(pit.getOpenLid().equals("True"));
                    break;
                case CHANNEL_PITMASTER_CHANNEL_ID:
                    state = new DecimalType(pit.getCh());
                    break;
            }
        }
        return state;
    }

    public String getTrigger(ChannelUID channelUID) {
        String trigger = null;
        if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channelId >= 0 && channelId <= 9) {
                Channel channel = getChannel();
                if (channel == null) {
                    return "";
                }
                Data data = channel.getData(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case "alarm_openhab":
                        if (!data.getState().equals("er")) {
                            if (data.getTemp() > data.getTempMax()) {
                                trigger = TRIGGER_ALARM_MAX;
                            } else if (data.getTemp() < data.getTempMin()) {
                                trigger = TRIGGER_ALARM_MIN;
                            }
                        }
                }
            }
        }
        return trigger;
    }
}
