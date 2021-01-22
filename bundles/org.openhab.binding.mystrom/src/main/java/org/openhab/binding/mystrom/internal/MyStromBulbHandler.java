/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.core.cache.ExpiringCache;
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
public class MyStromBulbHandler extends MyStromAbstractHandler {

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

    private static final String COMMUNICATION_ERROR = "Error while communicating to the myStrom bulb: ";

    private final Logger logger = LoggerFactory.getLogger(MyStromBulbHandler.class);

    private final ExpiringCache<Map<String, MyStromDeviceSpecificInfo>> cache = new ExpiringCache<>(
            Duration.ofSeconds(3), this::getReport);

    private PercentType lastBrightness = PercentType.HUNDRED;
    private QuantityType<Dimensionless> lastColorTemperature = QuantityType.ONE;

    public MyStromBulbHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                pollDevice();
            } else {
                @Nullable
                String sResp = null;
                switch (channelUID.getId()) {
                    case CHANNEL_SWITCH:
                        if (command instanceof OnOffType) {
                            sResp = sendHttpPost(command == OnOffType.ON ? "on" : "off", null, null, null);
                        }
                        break;

                    case CHANNEL_COLOR:
                        if (command instanceof HSBType) {
                            String hsv = command.toString().replaceAll(",", ";");
                            sResp = sendHttpPost(null, hsv, null, HSV);
                        }
                        break;

                    case CHANNEL_BRIGHTNESS:
                        if (command instanceof PercentType) {
                            String mono = lastColorTemperature.toString() + ";" + command.toString();
                            sResp = sendHttpPost(null, mono, null, MONO);
                        }
                        break;

                    case CHANNEL_COLOR_TEMPERATURE:
                        if (command instanceof Number) {
                            String mono = command.toString() + ";" + lastBrightness.toString();
                            sResp = sendHttpPost(null, mono, null, MONO);
                        }
                        break;

                    case CHANNEL_RAMP:
                        if (command instanceof QuantityType) {
                            sResp = sendHttpPost(null, null, command.toString(), null);
                        }
                        break;

                    case CHANNEL_MODE:
                        if (command instanceof StringType) {
                            sResp = sendHttpPost(null, null, null, command.toString());
                        }
                        break;

                    default:
                }

                if (sResp != null) {
                    Type type = new TypeToken<HashMap<String, MyStromDeviceSpecificInfo>>() {
                    }.getType();
                    @Nullable
                    Map<String, MyStromDeviceSpecificInfo> report = getGson().fromJson(sResp, type);
                    if (report != null) {
                        report.entrySet().stream().filter(e -> e.getKey().equals(getMac())).findFirst()
                                .ifPresent(info -> updateDevice(info.getValue()));
                    }
                }
            }
        } catch (MyStromException e) {
            logger.warn("Error while handling command {}", e.getMessage());
        }
    }

    private @Nullable Map<String, MyStromDeviceSpecificInfo> getReport() {
        try {
            String returnContent = getDeviceSpecificInformation();
            Type type = new TypeToken<HashMap<String, MyStromDeviceSpecificInfo>>() {
            }.getType();
            Map<String, MyStromDeviceSpecificInfo> report = getGson().fromJson(returnContent, type);
            updateStatus(ThingStatus.ONLINE);
            return report;
        } catch (MyStromException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return null;
        }
    }

    @Override
    protected void pollDevice() {
        @Nullable
        Map<String, MyStromDeviceSpecificInfo> report = cache.getValue();
        if (report != null) {
            report.entrySet().stream().filter(e -> e.getKey().equals(getMac())).findFirst()
                    .ifPresent(info -> updateDevice(info.getValue()));
        }
    }

    private void updateDevice(@Nullable MyStromBulbResponse deviceInfo) {
        if (deviceInfo != null) {
            updateState(CHANNEL_SWITCH, deviceInfo.on ? OnOffType.ON : OnOffType.OFF);
            @Nullable
            HSBType color = null;
            long numSemicolon = deviceInfo.color.chars().filter(c -> c == ';').count();
            if (numSemicolon == 1 && deviceInfo.mode.equals(MONO)) {
                String[] xy = deviceInfo.color.split(";");
                lastColorTemperature = new QuantityType<>(xy[0]);
                lastBrightness = PercentType.valueOf(xy[1]);
                updateState(CHANNEL_COLOR_TEMPERATURE, lastColorTemperature);
                updateState(CHANNEL_BRIGHTNESS, lastBrightness);
                updateState(CHANNEL_MODE, StringType.valueOf(deviceInfo.mode));
            } else if (numSemicolon == 2 && deviceInfo.mode.equals(HSV)) {
                color = HSBType.valueOf(deviceInfo.color.replaceAll(";", ","));
            } else if (!deviceInfo.color.equals("") && deviceInfo.mode.equals(RGB)) {
                int r = Integer.parseInt(deviceInfo.color.substring(2, 4), 16);
                int g = Integer.parseInt(deviceInfo.color.substring(4, 6), 16);
                int b = Integer.parseInt(deviceInfo.color.substring(6, 8), 16);
                color = HSBType.fromRGB(r, g, b);
            }
            if (color != null) {
                updateState(CHANNEL_COLOR, color);
                updateState(CHANNEL_MODE, StringType.valueOf(deviceInfo.mode));
            }
            updateState(CHANNEL_RAMP, QuantityType.valueOf(deviceInfo.ramp, MetricPrefix.MILLI(SECOND)));
            if (deviceInfo instanceof MyStromDeviceSpecificInfo) {
                updateState(CHANNEL_POWER, QuantityType.valueOf(((MyStromDeviceSpecificInfo) deviceInfo).power, WATT));
            }
        }
    }

    /**
     * Given a URL, send a HTTP GET request to the URL location
     * created by the URL.
     *
     * @return String contents of the response for the GET request.
     * @throws MyStromException Throws on communication error
     */
    private String getDeviceSpecificInformation() throws MyStromException {
        String url = getHostname() + "/api/v1/device";
        ContentResponse response;
        try {
            Request request = getHttpClient().newRequest(url).timeout(10, TimeUnit.SECONDS).method(HttpMethod.GET);
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MyStromException(COMMUNICATION_ERROR + e.getMessage());
        }

        if (response.getStatus() != HTTP_OK_CODE) {
            throw new MyStromException(
                    "Error sending HTTP GET request to " + url + ". Got response code: " + response.getStatus());
        }
        return response.getContentAsString();
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
    private String sendHttpPost(@Nullable String action, @Nullable String color, @Nullable String ramp,
            @Nullable String mode) throws MyStromException {
        String url = getHostname() + "/api/v1/device/" + getMac();
        ContentResponse response;
        try {
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

            // FormContentProvider contentProvider = new FormContentProvider(fields);
            Iterator<Fields.Field> i = fields.iterator();
            StringBuilder builder = new StringBuilder(fields.getSize() * 32);
            while (i.hasNext()) {
                Fields.Field field = i.next();

                String value;
                for (Iterator<String> var5 = field.getValues().iterator(); var5.hasNext(); builder
                        .append(field.getName()).append("=").append(value)) {
                    value = var5.next();
                    if (builder.length() > 0) {
                        builder.append("&");
                    }
                }
            }
            StringContentProvider contentProvider = new StringContentProvider(builder.toString());
            Request request = getHttpClient().newRequest(url).timeout(10, TimeUnit.SECONDS).method(HttpMethod.POST)
                    .content(contentProvider).header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");

            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MyStromException(COMMUNICATION_ERROR + e.getMessage());
        }

        if (response.getStatus() != HTTP_OK_CODE) {
            throw new MyStromException(
                    "Error sending HTTP POST request to " + url + ". Got response code: " + response.getStatus());
        }
        return response.getContentAsString();
    }
}
