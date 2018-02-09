/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mihome.internal.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - Refactor & sound
 */
public class XiaomiActorGatewayHandler extends XiaomiActorBaseHandler {

    private static final int COLOR_TEMPERATURE_MAX = 6500;
    private static final int COLOR_TEMPERATURE_MIN = 1700;

    private static final int DEFAULT_BRIGTHNESS_PCENT = 100;
    private static final int DEFAULT_VOLUME_PCENT = 50;
    private static final int DEFAULT_COLOR = 0xffffff;

    private static final String RGB = "rgb";
    private static final String ILLUMINATION = "illumination";
    private static final String MID = "mid";
    private static final String VOL = "vol";

    private Integer lastBrigthness;
    private Integer lastVolume;
    private Integer lastColor;

    private final Logger logger = LoggerFactory.getLogger(XiaomiActorGatewayHandler.class);

    public XiaomiActorGatewayHandler(Thing thing) {
        super(thing);
        lastBrigthness = DEFAULT_BRIGTHNESS_PCENT;
        lastVolume = DEFAULT_VOLUME_PCENT;
        lastColor = DEFAULT_COLOR;
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    int newBright = ((PercentType) command).intValue();
                    if (lastBrigthness != newBright) {
                        lastBrigthness = newBright;
                        logger.debug("Set brigthness to {}", lastBrigthness);
                        writeBridgeLightColor(lastColor, lastBrigthness);
                    } else {
                        logger.debug("Do not send this command, value {} already set", newBright);
                    }
                    return;
                } else if (command instanceof OnOffType) {
                    writeBridgeLightColor(lastColor, command == OnOffType.ON ? lastBrigthness : 0);
                    return;
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    lastColor = ((HSBType) command).getRGB() & 0xffffff;
                    writeBridgeLightColor(lastColor, lastBrigthness);
                    return;
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof PercentType) {
                    PercentType colorTemperature = (PercentType) command;
                    int kelvin = (COLOR_TEMPERATURE_MAX - COLOR_TEMPERATURE_MIN) / 100 * colorTemperature.intValue()
                            + COLOR_TEMPERATURE_MIN;
                    int color = ColorUtil.getRGBFromK(kelvin);
                    writeBridgeLightColor(color, lastBrigthness);
                    updateState(CHANNEL_COLOR,
                            HSBType.fromRGB((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff));
                    return;
                }
                break;
            case CHANNEL_GATEWAY_SOUND:
                if (command instanceof DecimalType) {
                    writeBridgeRingtone(((DecimalType) command).intValue(), lastVolume);
                    updateState(CHANNEL_GATEWAY_SOUND_SWITCH, OnOffType.ON);
                    return;
                }
                break;
            case CHANNEL_GATEWAY_SOUND_SWITCH:
                if (command instanceof OnOffType) {
                    if (((OnOffType) command) == OnOffType.OFF) {
                        stopRingtone();
                    }
                    return;
                }
                break;
            case CHANNEL_GATEWAY_VOLUME:
                if (command instanceof DecimalType) {
                    updateLastVolume((DecimalType) command);
                }
                return;
        }
        // Only gets here, if no condition was met
        logger.warn("Can't handle command {} on channel {}", command, channelUID);
    }

    private void updateLastVolume(DecimalType newVolume) {
        lastVolume = newVolume.intValue();
        logger.debug("Changed volume to {}", lastVolume);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        logger.debug("Update {} for channel {} received", newState, channelUID);
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS:
                if (newState instanceof PercentType) {
                    lastBrigthness = ((PercentType) newState).intValue();
                }
                break;
            case CHANNEL_COLOR:
                if (newState instanceof HSBType) {
                    lastColor = ((HSBType) newState).getRGB();
                }
                break;
            case CHANNEL_GATEWAY_VOLUME:
                if (newState instanceof DecimalType) {
                    updateLastVolume((DecimalType) newState);
                }
                break;
        }
    }

    @Override
    void parseReport(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseHeartbeat(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseReadAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseWriteAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseDefault(JsonObject data) {
        if (data.has(RGB)) {
            long rgb = data.get(RGB).getAsLong();
            updateState(CHANNEL_BRIGHTNESS, new PercentType((int) (((rgb >> 24) & 0xff))));
            updateState(CHANNEL_COLOR,
                    HSBType.fromRGB((int) (rgb >> 16) & 0xff, (int) (rgb >> 8) & 0xff, (int) rgb & 0xff));
        }
        if (data.has(ILLUMINATION)) {
            int illu = data.get(ILLUMINATION).getAsInt();
            updateState(CHANNEL_ILLUMINATION, new DecimalType(illu));
        }
    }

    private void writeBridgeLightColor(int color, int brightness) {
        long brightnessInt = brightness << 24;
        writeBridgeLightColor((color & 0xffffff) | brightnessInt & 0xff000000);
    }

    private void writeBridgeLightColor(long color) {
        getXiaomiBridgeHandler().writeToBridge(new String[] { RGB }, new Object[] { color });
    }

    /**
     * Play ringtone on Xiaomi Gateway
     * 0 - 8, 10 - 13, 20 - 29 -- ringtones that come with the system)
     * > 10001 -- user-defined ringtones
     *
     * @param ringtoneId
     */
    private void writeBridgeRingtone(int ringtoneId, int volume) {
        getXiaomiBridgeHandler().writeToBridge(new String[] { MID, VOL }, new Object[] { ringtoneId, volume });
    }

    /**
     * Stop playing ringtone on Xiaomi Gateway
     * by setting "mid" parameter to 10000
     */
    private void stopRingtone() {
        getXiaomiBridgeHandler().writeToBridge(new String[] { MID }, new Object[] { 10000 });
    }

}
