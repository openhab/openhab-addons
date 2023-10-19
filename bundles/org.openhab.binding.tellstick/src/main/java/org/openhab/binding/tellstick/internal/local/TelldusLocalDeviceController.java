/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.local;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tellstick.internal.TelldusBindingException;
import org.openhab.binding.tellstick.internal.conf.TelldusLocalConfiguration;
import org.openhab.binding.tellstick.internal.handler.TelldusDeviceController;
import org.openhab.binding.tellstick.internal.local.dto.TelldusLocalResponseDTO;
import org.openhab.binding.tellstick.internal.local.dto.TellstickLocalDeviceDTO;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tellstick.JNA;
import org.tellstick.device.TellstickDevice;
import org.tellstick.device.TellstickDeviceEvent;
import org.tellstick.device.TellstickException;
import org.tellstick.device.TellstickSensorEvent;
import org.tellstick.device.iface.Device;
import org.tellstick.device.iface.DeviceChangeListener;
import org.tellstick.device.iface.SensorListener;
import org.tellstick.device.iface.SwitchableDevice;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link TelldusLocalDeviceController} handles the communication with Telldus Local API (Tellstick ZNET v1/v2)
 * This controller uses JSON based Rest API to communicate with Telldus Local API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
public class TelldusLocalDeviceController implements DeviceChangeListener, SensorListener, TelldusDeviceController {
    private final Logger logger = LoggerFactory.getLogger(TelldusLocalDeviceController.class);
    private long lastSend = 0;
    public static final long DEFAULT_INTERVAL_BETWEEN_SEND_SEC = 250;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private String localApiUrl;
    private String authorizationHeader = "Bearer ";
    static final String HTTP_LOCAL_API = "api/";
    static final String HTTP_LOCAL_API_DEVICES = HTTP_LOCAL_API + "devices/list?supportedMethods=19&includeIgnored=0";
    static final String HTTP_LOCAL_API_SENSORS = HTTP_LOCAL_API
            + "sensors/list?includeValues=1&includeScale=1&includeUnit=1&includeIgnored=0";
    static final String HTTP_LOCAL_API_SENSOR_INFO = HTTP_LOCAL_API + "sensor/info";
    static final String HTTP_LOCAL_API_DEVICE_DIM = HTTP_LOCAL_API + "device/dim?id=%d&level=%d";
    static final String HTTP_LOCAL_API_DEVICE_TURNOFF = HTTP_LOCAL_API + "device/turnOff?id=%d";
    static final String HTTP_LOCAL_DEVICE_TURNON = HTTP_LOCAL_API + "device/turnOn?id=%d";
    private static final int MAX_RETRIES = 3;
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    public TelldusLocalDeviceController(TelldusLocalConfiguration configuration, HttpClient httpClient) {
        this.httpClient = httpClient;
        localApiUrl = "http://" + configuration.ipAddress + "/";
        authorizationHeader = authorizationHeader + configuration.accessToken;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void handleSendEvent(Device device, int resendCount, boolean isdimmer, Command command)
            throws TellstickException {
        logger.debug("Send {} to {}", command, device);
        try {
            if (device instanceof TellstickLocalDeviceDTO) {
                if (command == OnOffType.ON) {
                    turnOn(device);
                } else if (command == OnOffType.OFF) {
                    turnOff(device);
                } else if (command instanceof PercentType percentCommand) {
                    dim(device, percentCommand);
                } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                    increaseDecrease(device, increaseDecreaseCommand);
                }
            } else if (device instanceof SwitchableDevice) {
                if (command == OnOffType.ON) {
                    if (isdimmer) {
                        logger.trace("Turn off first in case it is allready on");
                        turnOff(device);
                    }
                    turnOn(device);
                } else if (command == OnOffType.OFF) {
                    turnOff(device);
                }
            } else {
                logger.warn("Cannot send to {}", device);
            }
        } catch (InterruptedException e) {
            logger.debug("OH is shut-down.");
        }
    }

    private void increaseDecrease(Device dev, IncreaseDecreaseType increaseDecreaseType)
            throws TellstickException, InterruptedException {
        String strValue = ((TellstickDevice) dev).getData();
        double value = 0;
        if (strValue != null) {
            value = Double.valueOf(strValue);
        }
        int percent = (int) Math.round((value / 255) * 100);
        if (IncreaseDecreaseType.INCREASE == increaseDecreaseType) {
            percent = Math.min(percent + 10, 100);
        } else if (IncreaseDecreaseType.DECREASE == increaseDecreaseType) {
            percent = Math.max(percent - 10, 0);
        }
        dim(dev, new PercentType(percent));
    }

    private void dim(Device dev, PercentType command) throws TellstickException, InterruptedException {
        double value = command.doubleValue();

        // 0 means OFF and 100 means ON
        if (value == 0 && dev instanceof TellstickLocalDeviceDTO) {
            turnOff(dev);
        } else if (value == 100 && dev instanceof TellstickLocalDeviceDTO) {
            turnOn(dev);
        } else if (dev instanceof TellstickLocalDeviceDTO device
                && (device.getMethods() & JNA.CLibrary.TELLSTICK_DIM) > 0) {
            long tdVal = Math.round((value / 100) * 255);
            TelldusLocalResponseDTO response = callRestMethod(
                    String.format(HTTP_LOCAL_API_DEVICE_DIM, dev.getId(), tdVal), TelldusLocalResponseDTO.class);
            handleResponse(device, response);
        } else {
            throw new TelldusBindingException("Cannot send DIM to " + dev);
        }
    }

    private void turnOff(Device dev) throws TellstickException, InterruptedException {
        if (dev instanceof TellstickLocalDeviceDTO device) {
            TelldusLocalResponseDTO response = callRestMethod(String.format(HTTP_LOCAL_API_DEVICE_TURNOFF, dev.getId()),
                    TelldusLocalResponseDTO.class);
            handleResponse(device, response);
        } else {
            throw new TelldusBindingException("Cannot send OFF to " + dev);
        }
    }

    private void handleResponse(TellstickLocalDeviceDTO device, TelldusLocalResponseDTO response)
            throws TellstickException {
        if (response == null || (response.getStatus() == null && response.getError() == null)) {
            throw new TelldusBindingException("No response " + response);
        } else if (response.getError() != null) {
            device.setUpdated(true);
            throw new TelldusBindingException("Error " + response.getError());
        } else if (!"success".equals(response.getStatus().trim())) {
            throw new TelldusBindingException("Response " + response.getStatus());
        }
    }

    private void turnOn(Device dev) throws TellstickException, InterruptedException {
        if (dev instanceof TellstickLocalDeviceDTO device) {
            TelldusLocalResponseDTO response = callRestMethod(String.format(HTTP_LOCAL_DEVICE_TURNON, dev.getId()),
                    TelldusLocalResponseDTO.class);
            handleResponse(device, response);
        } else {
            throw new TelldusBindingException("Cannot send ON to " + dev);
        }
    }

    @Override
    public State calcState(Device dev) {
        TellstickLocalDeviceDTO device = (TellstickLocalDeviceDTO) dev;
        State st = null;

        switch (device.getState()) {
            case JNA.CLibrary.TELLSTICK_TURNON:
                st = OnOffType.ON;
                break;
            case JNA.CLibrary.TELLSTICK_TURNOFF:
                st = OnOffType.OFF;
                break;
            case JNA.CLibrary.TELLSTICK_DIM:
                BigDecimal dimValue = new BigDecimal(device.getStatevalue());
                if (dimValue.intValue() == 0) {
                    st = OnOffType.OFF;
                } else if (dimValue.intValue() >= 255) {
                    st = OnOffType.ON;
                } else {
                    st = OnOffType.ON;
                }
                break;
            default:
                logger.warn("Could not handle {} for {}", device.getState(), device);
        }

        return st;
    }

    @Override
    public BigDecimal calcDimValue(Device device) {
        BigDecimal dimValue = BigDecimal.ZERO;
        switch (((TellstickLocalDeviceDTO) device).getState()) {
            case JNA.CLibrary.TELLSTICK_TURNON:
                dimValue = new BigDecimal(100);
                break;
            case JNA.CLibrary.TELLSTICK_TURNOFF:
                break;
            case JNA.CLibrary.TELLSTICK_DIM:
                dimValue = new BigDecimal(((TellstickLocalDeviceDTO) device).getStatevalue());
                dimValue = dimValue.multiply(new BigDecimal(100));
                dimValue = dimValue.divide(new BigDecimal(255), 0, RoundingMode.HALF_UP);
                break;
            default:
                logger.warn("Could not handle {} for {}", (((TellstickLocalDeviceDTO) device).getState()), device);
        }
        return dimValue;
    }

    public long getLastSend() {
        return lastSend;
    }

    public void setLastSend(long currentTimeMillis) {
        lastSend = currentTimeMillis;
    }

    @Override
    public void onRequest(TellstickSensorEvent newDevices) {
        setLastSend(newDevices.getTimestamp());
    }

    @Override
    public void onRequest(TellstickDeviceEvent newDevices) {
        setLastSend(newDevices.getTimestamp());
    }

    <T> T callRestMethod(String uri, Class<T> response) throws TelldusLocalException, InterruptedException {
        T resultObj = null;
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    resultObj = innerCallRest(localApiUrl + uri, response);
                    break;
                } catch (TimeoutException e) {
                    logger.warn("TimeoutException error in get");
                }
            }
        } catch (JsonSyntaxException e) {
            throw new TelldusLocalException(e);
        } catch (ExecutionException e) {
            throw new TelldusLocalException(e);
        }
        return resultObj;
    }

    private <T> T innerCallRest(String uri, Class<T> json)
            throws ExecutionException, InterruptedException, TimeoutException, JsonSyntaxException {
        logger.trace("HTTP GET: {}", uri);

        Request request = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(REQUEST_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);
        request.header("Authorization", authorizationHeader);

        ContentResponse response = request.send();
        String content = response.getContentAsString();
        logger.trace("API response: {}", content);

        return gson.fromJson(content, json);
    }
}
