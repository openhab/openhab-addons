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
package org.openhab.binding.mystrom.internal;

import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_BRIGHTNESS;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_COLOR_TEMPERATURE;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_RAMP;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_SWITCH;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.HSV;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.MONO;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.RGB;
import static org.openhab.core.library.unit.Units.SECOND;
import static org.openhab.core.library.unit.Units.WATT;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * The {@link MyStromBulbHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frederic Chastagnol - Initial contribution
 */
@NonNullByDefault
public class MyStromBulbHandler extends AbstractMyStromHandler {

    private static final Type DEVICE_INFO_MAP_TYPE = new TypeToken<HashMap<String, MyStromDeviceSpecificInfo>>() {
    }.getType();

    private final Logger logger = LoggerFactory.getLogger(MyStromBulbHandler.class);

    private final ExpiringCache<Map<String, MyStromDeviceSpecificInfo>> cache = new ExpiringCache<>(
            Duration.ofSeconds(3), this::getReport);

    private PercentType lastBrightness = PercentType.HUNDRED;
    private PercentType lastColorTemperature = new PercentType(50);
    private String lastMode = MONO;
    private HSBType lastColor = HSBType.WHITE;

    public MyStromBulbHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                pollDevice();
            } else {
                String sResp = null;
                switch (channelUID.getId()) {
                    case CHANNEL_SWITCH:
                        if (command instanceof OnOffType) {
                            sResp = sendToBulb(command == OnOffType.ON ? "on" : "off", null, null, null);
                        }
                        break;
                    case CHANNEL_COLOR:
                        if (command instanceof HSBType hsb) {
                            if (Objects.equals(hsb.as(OnOffType.class), OnOffType.OFF)) {
                                sResp = sendToBulb("off", null, null, null);
                            } else {
                                String hsv = command.toString().replace(",", ";");
                                sResp = sendToBulb("on", hsv, null, HSV);
                            }
                        }
                        break;
                    case CHANNEL_BRIGHTNESS:
                        if (command instanceof PercentType brightness) {
                            if (Objects.equals(brightness.as(OnOffType.class), OnOffType.OFF)) {
                                sResp = sendToBulb("off", null, null, null);
                            } else {
                                if (lastMode.equals(MONO)) {
                                    String mono = convertPercentageToMyStromCT(lastColorTemperature) + ";"
                                            + command.toString();
                                    sResp = sendToBulb("on", mono, null, MONO);
                                } else {
                                    String hsv = lastColor.getHue().intValue() + ";" + lastColor.getSaturation() + ";"
                                            + command.toString();
                                    sResp = sendToBulb("on", hsv, null, HSV);
                                }
                            }
                        }
                        break;
                    case CHANNEL_COLOR_TEMPERATURE:
                        if (command instanceof PercentType temperature) {
                            String mono = convertPercentageToMyStromCT(temperature) + ";" + lastBrightness.toString();
                            sResp = sendToBulb("on", mono, null, MONO);
                        }
                        break;
                    case CHANNEL_RAMP:
                        if (command instanceof DecimalType) {
                            sResp = sendToBulb(null, null, command.toString(), null);
                        }
                        break;
                    case CHANNEL_MODE:
                        if (command instanceof StringType) {
                            sResp = sendToBulb(null, null, null, command.toString());
                        }
                        break;
                    default:
                }

                if (sResp != null) {
                    Map<String, MyStromDeviceSpecificInfo> report = gson.fromJson(sResp, DEVICE_INFO_MAP_TYPE);
                    if (report != null) {
                        report.entrySet().stream().filter(e -> e.getKey().equals(mac)).findFirst()
                                .ifPresent(info -> updateDevice(info.getValue()));
                    }
                }
            }
        } catch (MyStromException e) {
            logger.warn("Error while handling command {}", e.getMessage());
        }
    }

    @Override
    protected void checkRequiredInfo() throws MyStromException {
        if (mac.isBlank()) {
            throw new MyStromException("Cannot retrieve MAC info from myStrom device " + getThing().getUID());
        }
    }

    private @Nullable Map<String, MyStromDeviceSpecificInfo> getReport() {
        try {
            String returnContent = sendHttpRequest(HttpMethod.GET, "/api/v1/device", null);
            Map<String, MyStromDeviceSpecificInfo> report = gson.fromJson(returnContent, DEVICE_INFO_MAP_TYPE);
            updateStatus(ThingStatus.ONLINE);
            return report;
        } catch (MyStromException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return null;
        }
    }

    @Override
    protected void pollDevice() {
        Map<String, MyStromDeviceSpecificInfo> report = cache.getValue();
        if (report != null) {
            report.entrySet().stream().filter(e -> e.getKey().equals(mac)).findFirst()
                    .ifPresent(info -> updateDevice(info.getValue()));
        }
    }

    private void updateDevice(@Nullable MyStromBulbResponse deviceInfo) {
        if (deviceInfo != null) {
            updateState(CHANNEL_SWITCH, OnOffType.from(deviceInfo.on));
            updateState(CHANNEL_RAMP, QuantityType.valueOf(deviceInfo.ramp, MetricPrefix.MILLI(SECOND)));
            if (deviceInfo instanceof MyStromDeviceSpecificInfo info) {
                updateState(CHANNEL_POWER, QuantityType.valueOf(info.power, WATT));
            }
            if (deviceInfo.on) {
                try {
                    lastMode = deviceInfo.mode;
                    long numSemicolon = deviceInfo.color.chars().filter(c -> c == ';').count();
                    if (numSemicolon == 1 && deviceInfo.mode.equals(MONO)) {
                        String[] xy = deviceInfo.color.split(";");
                        lastColorTemperature = new PercentType(convertMyStromCTToPercentage(xy[0]));
                        lastBrightness = PercentType.valueOf(xy[1]);
                        lastColor = new HSBType(lastColor.getHue() + ",0," + lastBrightness);
                        updateState(CHANNEL_COLOR_TEMPERATURE, lastColorTemperature);
                    } else if (numSemicolon == 2 && deviceInfo.mode.equals(HSV)) {
                        lastColor = HSBType.valueOf(deviceInfo.color.replace(";", ","));
                        lastBrightness = lastColor.getBrightness();
                    } else if (!"".equals(deviceInfo.color) && deviceInfo.mode.equals(RGB)) {
                        int r = Integer.parseInt(deviceInfo.color.substring(2, 4), 16);
                        int g = Integer.parseInt(deviceInfo.color.substring(4, 6), 16);
                        int b = Integer.parseInt(deviceInfo.color.substring(6, 8), 16);
                        lastColor = HSBType.fromRGB(r, g, b);
                        lastBrightness = lastColor.getBrightness();
                    }
                    updateState(CHANNEL_COLOR, lastColor);
                    updateState(CHANNEL_BRIGHTNESS, lastBrightness);
                    updateState(CHANNEL_MODE, StringType.valueOf(lastMode));
                } catch (IllegalArgumentException e) {
                    logger.warn("Error while updating {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Given a URL and a set parameters, send a HTTP POST request to the URL location
     * created by the URL and parameters.
     *
     * @param action The action we want to take (on,off or toggle)
     * @param color The color we set the bulb to (When using RGBW mode the first two hex numbers are used for the
     *            white channel! hsv is of form <UINT 0..360>;<UINT 0..100>;<UINT 0..100>)
     * @param ramp Transition time from the light’s current state to the new state. [ms]
     * @param mode The color mode we want the Bulb to set to (rgb or hsv or mono)
     * @return String contents of the response for the GET request.
     * @throws MyStromException Throws on communication error
     */
    private String sendToBulb(@Nullable String action, @Nullable String color, @Nullable String ramp,
            @Nullable String mode) throws MyStromException {
        Fields fields = new Fields();
        if (action != null) {
            fields.put("action", action);
        }
        if (color != null) {
            fields.put("color", color);
        }
        if (ramp != null) {
            fields.put("ramp", ramp);
        }
        if (mode != null) {
            fields.put("mode", mode);
        }
        StringBuilder builder = new StringBuilder(fields.getSize() * 32);
        for (Fields.Field field : fields) {
            for (String value : field.getValues()) {
                if (builder.length() > 0) {
                    builder.append("&");
                }
                builder.append(field.getName()).append("=").append(value);
            }
        }
        return sendHttpRequest(HttpMethod.POST, "/api/v1/device/" + mac, builder.toString());
    }

    /**
     * Convert the color temperature from myStrom (1-18) to openHAB (percentage)
     *
     * @param ctValue Color temperature in myStrom: "1" = warm to "18" = cold.
     * @return Color temperature (0-100%). 0% is the coldest setting.
     * @throws NumberFormatException if the argument is not an integer
     */
    private int convertMyStromCTToPercentage(String ctValue) throws NumberFormatException {
        int ct = Integer.parseInt(ctValue);
        return Math.round((18 - limitColorTemperature(ct)) / 17F * 100F);
    }

    /**
     * Convert the color temperature from openHAB (percentage) to myStrom (1-18)
     *
     * @param colorTemperature Color temperature from openHab. 0 = coldest, 100 = warmest
     * @return Color temperature from myStrom. 1 = warmest, 18 = coldest
     */
    private String convertPercentageToMyStromCT(PercentType colorTemperature) {
        int ct = 18 - Math.round(colorTemperature.floatValue() * 17F / 100F);
        return Integer.toString(limitColorTemperature(ct));
    }

    private int limitColorTemperature(int colorTemperature) {
        return Math.max(1, Math.min(colorTemperature, 18));
    }

    private static class MyStromBulbResponse {
        public boolean on;
        public String color = "";
        public String mode = "";
        public long ramp;

        @Override
        public String toString() {
            return "MyStromBulbResponse{" + "on=" + on + ", color='" + color + '\'' + ", mode='" + mode + '\''
                    + ", ramp=" + ramp + '}';
        }
    }

    private static class MyStromDeviceSpecificInfo extends MyStromBulbResponse {
        public double power;
    }
}
