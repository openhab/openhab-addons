
package org.openhab.binding.wlanthermo.internal.api.mini.builtin;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_ALARM_DEVICE;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_HIGH;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_ALARM_OPENHAB_LOW;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_MAX;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_MIN;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_NAME;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.CHANNEL_TEMP;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.SYSTEM_CPU_LOAD;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.SYSTEM_CPU_TEMP;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_ALARM_MAX;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_ALARM_MIN;

import java.awt.Color;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wlanthermo.internal.WlanThermoMiniHandler;

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
                    state = new DecimalType(getCpuTemp());
                    break;
                case SYSTEM_CPU_LOAD:
                    state = new DecimalType(getCpuLoad());
                    break;
            }
        } else if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channelId >= 0 && channelId <= 10) {
                Data channel = getChannel().getData(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_NAME:
                        state = new StringType(channel.getName());
                        break;
                    case CHANNEL_TEMP:
                        if (channel.getState().equals("er")) {
                            state = UnDefType.UNDEF;
                        } else {
                            state = new DecimalType(channel.getTemp());
                        }
                        break;
                    case CHANNEL_MIN:
                        state = new DecimalType(channel.getTempMin());
                        break;
                    case CHANNEL_MAX:
                        state = new DecimalType(channel.getTempMax());
                        break;
                    case CHANNEL_ALARM_DEVICE:
                        state = OnOffType.from(channel.getAlert());
                        break;
                    case CHANNEL_ALARM_OPENHAB_HIGH:
                        if (!channel.getState().equals("er")
                                && channel.getTemp() > channel.getTempMax()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case CHANNEL_ALARM_OPENHAB_LOW:
                        if (!channel.getState().equals("er") && channel.getTemp() < channel.getTempMin()) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        break;
                    case CHANNEL_COLOR:
                        Color c = Color.decode(channel.getColor());
                        state = HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                        break;
                }
            }
        }
        return state;
    }

    public String getTrigger(ChannelUID channelUID) {
        String trigger = null;
        if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channelId >= 0 && channelId <= 10) {
                Data channel = getChannel().getData(channelId);
                switch (channelUID.getIdWithoutGroup()) {
                    case "alarm_openhab":
                        if (!channel.getState().equals("er")) {
                            if (channel.getTemp() > channel.getTempMax()) {
                                trigger = TRIGGER_ALARM_MAX;
                            } else if (channel.getTemp() < channel.getTempMin()) {
                                trigger = TRIGGER_ALARM_MIN;
                            }
                        }
                }
            }
        }
        return trigger;
    }

}
