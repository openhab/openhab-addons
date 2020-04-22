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
import org.eclipse.smarthome.core.library.types.QuantityType;
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
        State state = UnDefType.UNDEF;
        if (channelUID.getId().startsWith("system#") && system != null) {
            switch (channelUID.getId()) {
                case "system#soc":
                    state = new DecimalType(system.getSoc());
                    break;
                case "system#charge":
                    state =  OnOffType.from(system.getCharge());
                    break;
                case "system#rssi":
                    state =  new DecimalType(Math.floor(system.getRssi() * -1 / 24));
                    break;
            }
        } else if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channel.size() > 0 && channelId <= channel.size()) {
                switch (channelUID.getIdWithoutGroup()) {
                    case "name":
                        state =  new StringType(channel.get(channelId).getName());
                        break;
                    case "typ":
                        state =  new StringType(channel.get(channelId).getTyp() + "");
                        break;
                    case "temp":
                        if (channel.get(channelId).getTemp() == 999.0) {
                            state =  UnDefType.UNDEF;
                        } else {
                            state =  new DecimalType(channel.get(channelId).getTemp());
                        }
                        break;
                    case "min":
                        state =  new DecimalType(channel.get(channelId).getMin());
                        break;
                    case "max":
                        state =  new DecimalType(channel.get(channelId).getMax());
                        break;
                    case "alarm_device":
                        state =  OnOffType.from(BigInteger.valueOf(channel.get(channelId).getAlarm()).testBit(1));
                        break;
                    case "alarm_push":
                        state =  OnOffType.from(BigInteger.valueOf(channel.get(channelId).getAlarm()).testBit(0));
                        break;
                    case "color":
                        Color c = Color.decode(channel.get(channelId).getColor());
                        state =  HSBType.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
                        break;
                }
            }
        }
        return state;
    }

    public boolean setState(ChannelUID channelUID, Command command) {
        boolean success = false;
        if (channelUID.getId().startsWith("channel")) {
            int channelId = Integer.parseInt(channelUID.getGroupId().substring("channel".length())) - 1;
            if (channel.size() > 0 && channelId <= channel.size()) {
                switch (channelUID.getIdWithoutGroup()) {
                    case "name":
                        if (command instanceof StringType) {
                            channel.get(channelId).setName(((StringType) command).toFullString());
                            success = true;
                        }
                        break;
                    case "min":
                        if (command instanceof QuantityType) {
                            channel.get(channelId).setMin(((QuantityType) command).doubleValue());
                            success = true;
                        }
                        break;
                    case "max":
                        if (command instanceof QuantityType) {
                            channel.get(channelId).setMax(((QuantityType) command).doubleValue());
                            success = true;
                        }
                        break;
                    case "alarm_device":
                        if (command instanceof OnOffType) {
                            BigInteger value;
                            if ((OnOffType) command == OnOffType.ON) {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).setBit(1);
                            } else {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).clearBit(1);
                            }
                            channel.get(channelId).setAlarm(value.intValue());
                            success = true;
                        }
                        break;
                    case "alarm_push":
                        if (command instanceof OnOffType) {
                            BigInteger value;
                            if ((OnOffType) command == OnOffType.ON) {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).setBit(0);
                            } else {
                                value = BigInteger.valueOf(channel.get(channelId).getAlarm()).clearBit(0);
                            }
                            channel.get(channelId).setAlarm(value.intValue());
                            success = true;
                        }
                        break;
                    case "color":
                        if (command instanceof HSBType) {
                            channel.get(channelId).setColor("#" + Integer.toHexString(((HSBType) command).getRGB()));
                            success = true;
                        }
                        break;
                }
            }
        }
        return success;
    }

}
