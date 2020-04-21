
package org.openhab.binding.wlanthermo.internal.api.data;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

public class Data {

    @SerializedName("system")
    @Expose
    private System system;
    @SerializedName("channel")
    @Expose
    private List<Channel> channel = new ArrayList<Channel>();
    @SerializedName("pitmaster")
    @Expose
    private Pitmaster pitmaster;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Data() {
    }

    /**
     * 
     * @param system
     * @param pitmaster
     * @param channel
     */
    public Data(System system, List<Channel> channel, Pitmaster pitmaster) {
        super();
        this.system = system;
        this.channel = channel;
        this.pitmaster = pitmaster;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public Data withSystem(System system) {
        this.system = system;
        return this;
    }

    public List<Channel> getChannel() {
        return channel;
    }

    public void setChannel(List<Channel> channel) {
        this.channel = channel;
    }

    public Data withChannel(List<Channel> channel) {
        this.channel = channel;
        return this;
    }

    public Pitmaster getPitmaster() {
        return pitmaster;
    }

    public void setPitmaster(Pitmaster pitmaster) {
        this.pitmaster = pitmaster;
    }

    public Data withPitmaster(Pitmaster pitmaster) {
        this.pitmaster = pitmaster;
        return this;
    }

    public State getState(ChannelUID channelUID) {
        if (channelUID.getId().startsWith("system#") && system != null) {
            switch (channelUID.getId()) {
                case "system#soc":
                    return new DecimalType(system.getSoc());
                case "system#charge":
                    return OnOffType.from(system.getCharge());
                case "system#rssi":
                    return new DecimalType(Math.floor(system.getRssi() * -1 / 24));
            }
        } else if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channel.size() > 0 && channelId <= channel.size()) {
                switch (channelUID.getIdWithoutGroup()) {
                    case "name":
                        return new StringType(channel.get(channelId).getName());
                    case "typ":
                        return new StringType(channel.get(channelId).getTyp() + "");
                    case "temp":
                        if (channel.get(channelId).getTemp() == 999.0) {
                            return UnDefType.UNDEF;
                        } else {
                            return new DecimalType(channel.get(channelId).getTemp());
                        }
                    case "min":
                        return new DecimalType(channel.get(channelId).getMin());
                    case "max":
                        return new DecimalType(channel.get(channelId).getMax());
                    case "alarm_device":
                        return OnOffType.from(BigInteger.valueOf(channel.get(channelId).getAlarm()).testBit(1));
                    case "alarm_push":
                        return OnOffType.from(BigInteger.valueOf(channel.get(channelId).getAlarm()).testBit(0));
                    case "color":
                        Color c = Color.decode(channel.get(channelId).getColor());
                        return HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                }
            }
        }
        return UnDefType.UNDEF;
    }

    public void setState(ChannelUID channelUID, Command command) {
        //TODO: Set state with command value
    }

}
