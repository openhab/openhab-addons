/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import java.util.Iterator;

import org.eclipse.smarthome.core.items.Item;
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
 * @author Dieter Schmidt
 */
public class XiaomiActorGatewayHandler extends XiaomiActorBaseHandler {

    private static final int COLOR_TEMPERATURE_MAX = 6500;
    private static final int COLOR_TEMPERATURE_MIN = 1700;

    private Integer lastBrightness;

    private final Logger logger = LoggerFactory.getLogger(XiaomiActorGatewayHandler.class);

    public XiaomiActorGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    int newBright = ((PercentType) command).intValue();
                    if (lastBrightness != newBright) {
                        lastBrightness = newBright;
                        logger.debug("Set brigthness to {}", lastBrightness);
                        writeBridgeLightColor(getGatewayLightColor(), lastBrightness);
                    } else {
                        logger.debug("Do not send this command, value {} already set", newBright);
                    }
                    return;
                } else if (command instanceof OnOffType) {
                    restoreBrightnessFromItem();

                    writeBridgeLightColor(getGatewayLightColor(), command == OnOffType.ON ? lastBrightness : 0);
                    return;
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    writeBridgeLightColor(((HSBType) command).getRGB() & 0xffffff, getGatewayLightBrightness());
                    return;
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof PercentType) {
                    PercentType colorTemperature = (PercentType) command;
                    int kelvin = (COLOR_TEMPERATURE_MAX - COLOR_TEMPERATURE_MIN) / 100 * colorTemperature.intValue()
                            + COLOR_TEMPERATURE_MIN;
                    int color = ColorUtil.getRGBFromK(kelvin);
                    writeBridgeLightColor(color, getGatewayLightBrightness());
                    updateState(CHANNEL_COLOR,
                            HSBType.fromRGB((color / 256 / 256) & 0xff, (color / 256) & 0xff, color & 0xff));
                    return;
                }
                break;
            case CHANNEL_GATEWAY_SOUND:
                if (command instanceof DecimalType) {
                    Item volumeItem = getItemInChannel(CHANNEL_GATEWAY_VOLUME);
                    State state;
                    if (volumeItem == null) {
                        state = null;
                        logger.debug("There was no Item found for soundVolume, default 50% is used");
                    } else {
                        state = volumeItem.getState();
                    }
                    int volume = state instanceof DecimalType ? ((DecimalType) state).intValue() : 50;
                    writeBridgeRingtone(((DecimalType) command).intValue(), volume);
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
                // nothing to do, just suppress error
                return;
        }
        // Only gets here, if no condition was met
        logger.warn("Can't handle command {} on channel {}", command, channelUID);
    }

    private void restoreBrightnessFromItem() {
        if (lastBrightness == null) {
            try {
                Iterator<Item> iter = linkRegistry
                        .getLinkedItems(new ChannelUID(this.thing.getUID(), CHANNEL_BRIGHTNESS)).iterator();
                while (iter.hasNext()) {
                    Item item = iter.next();
                    if (item.getState() instanceof PercentType) {
                        lastBrightness = Integer.parseInt(item.getState().toString());
                        logger.debug("last brightness value found: {}", lastBrightness);
                        break;
                    }
                }
                lastBrightness = lastBrightness == null || lastBrightness == 0 ? 100 : lastBrightness;
                logger.debug("No dimmer value for brightness found, adjusted to {}", lastBrightness);
            } catch (NumberFormatException e) {
                lastBrightness = 100;
                logger.debug("No last brightness value found - assuming 100");
            }
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
        if (data.has("rgb")) {
            long rgb = data.get("rgb").getAsLong();
            updateState(CHANNEL_BRIGHTNESS, new PercentType((int) (((rgb >> 24) & 0xff))));
            updateState(CHANNEL_COLOR,
                    HSBType.fromRGB((int) (rgb >> 16) & 0xff, (int) (rgb >> 8) & 0xff, (int) rgb & 0xff));
        }
        if (data.has("illumination")) {
            int illu = data.get("illumination").getAsInt();
            updateState(CHANNEL_ILLUMINATION, new DecimalType(illu));
        }
    }

    private int getGatewayLightColor() {
        Item item = getItemInChannel(CHANNEL_COLOR);
        if (item == null) {
            return 0xffffff;
        }

        State state = item.getState();
        if (state instanceof HSBType) {
            return ((HSBType) state).getRGB() & 0xffffff;
        }

        return 0xffffff;
    }

    private int getGatewayLightBrightness() {
        Item item = getItemInChannel(CHANNEL_BRIGHTNESS);
        if (item == null) {
            return 100;
        }

        State state = item.getState();
        if (state == null) {
            return 100;
        } else if (state instanceof PercentType) {
            PercentType brightness = (PercentType) state;
            return brightness.intValue();
        } else if (state instanceof OnOffType) {
            return state == OnOffType.ON ? 100 : 0;
        }

        return 100;
    }

    private void writeBridgeLightColor(int color, int brightness) {
        long brightnessInt = brightness << 24;
        writeBridgeLightColor((color & 0xffffff) | brightnessInt & 0xff000000);
    }

    private void writeBridgeLightColor(long color) {
        getXiaomiBridgeHandler().writeToBridge(new String[] { "rgb" }, new Object[] { color });
    }

    /**
     * Play ringtone on Xiaomi Gateway
     * 0 - 8, 10 - 13, 20 - 29 -- ringtones that come with the system)
     * > 10001 -- user-defined ringtones
     *
     * @param ringtoneId
     */
    private void writeBridgeRingtone(int ringtoneId, int volume) {
        getXiaomiBridgeHandler().writeToBridge(new String[] { "mid", "vol" }, new Object[] { ringtoneId, volume });
    }

    /**
     * Stop playing ringtone on Xiaomi Gateway
     * by setting "mid" parameter to 10000
     */
    private void stopRingtone() {
        getXiaomiBridgeHandler().writeToBridge(new String[] { "mid" }, new Object[] { 10000 });
    }

}
