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
package org.openhab.binding.tellstick.internal.live;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder;
import org.asynchttpclient.Response;
import org.asynchttpclient.oauth.ConsumerKey;
import org.asynchttpclient.oauth.OAuthSignatureCalculator;
import org.asynchttpclient.oauth.RequestToken;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tellstick.internal.TelldusBindingException;
import org.openhab.binding.tellstick.internal.handler.TelldusDeviceController;
import org.openhab.binding.tellstick.internal.live.xml.TelldusLiveResponse;
import org.openhab.binding.tellstick.internal.live.xml.TellstickNetDevice;
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

/**
 * {@link TelldusLiveDeviceController} is the communication with Telldus Live service (Tellstick.NET and ZNET)
 * This controller uses XML based Rest API to communicate with Telldus Live.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TelldusLiveDeviceController implements DeviceChangeListener, SensorListener, TelldusDeviceController {
    private final Logger logger = LoggerFactory.getLogger(TelldusLiveDeviceController.class);
    private long lastSend = 0;
    public static final long DEFAULT_INTERVAL_BETWEEN_SEND = 250;
    private static final int REQUEST_TIMEOUT_MS = 15000;
    private AsyncHttpClient client;
    static final String HTTP_API_TELLDUS_COM_XML = "http://api.telldus.com/xml/";
    static final String HTTP_TELLDUS_CLIENTS = HTTP_API_TELLDUS_COM_XML + "clients/list";
    static final String HTTP_TELLDUS_DEVICES = HTTP_API_TELLDUS_COM_XML + "devices/list?supportedMethods=19";
    static final String HTTP_TELLDUS_SENSORS = HTTP_API_TELLDUS_COM_XML
            + "sensors/list?includeValues=1&includeScale=1&includeUnit=1";
    static final String HTTP_TELLDUS_SENSOR_INFO = HTTP_API_TELLDUS_COM_XML + "sensor/info";
    static final String HTTP_TELLDUS_DEVICE_DIM = HTTP_API_TELLDUS_COM_XML + "device/dim?id=%d&level=%d";
    static final String HTTP_TELLDUS_DEVICE_TURNOFF = HTTP_API_TELLDUS_COM_XML + "device/turnOff?id=%d";
    static final String HTTP_TELLDUS_DEVICE_TURNON = HTTP_API_TELLDUS_COM_XML + "device/turnOn?id=%d";

    private int nbRequest;
    private long sumRequestDuration;
    private long minRequestDuration = 1_000_000;
    private long maxRequestDuration;
    private int nbTimeouts;
    private int nbErrors;

    public TelldusLiveDeviceController() {
    }

    @Override
    public void dispose() {
        try {
            client.close();
        } catch (Exception e) {
            logger.debug("Failed to close client", e);
        }
    }

    void connectHttpClient(String publicKey, String privateKey, String token, String tokenSecret) {
        ConsumerKey consumer = new ConsumerKey(publicKey, privateKey);
        RequestToken user = new RequestToken(token, tokenSecret);
        OAuthSignatureCalculator calc = new OAuthSignatureCalculator(consumer, user);
        this.client = new DefaultAsyncHttpClient(createAsyncHttpClientConfig());
        try {
            this.client.setSignatureCalculator(calc);
            Response response = client.prepareGet(HTTP_TELLDUS_CLIENTS).execute().get();
            logger.debug("Response {} statusText {}", response.getResponseBody(), response.getStatusText());
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Failed to connect", e);
        }
    }

    private AsyncHttpClientConfig createAsyncHttpClientConfig() {
        Builder builder = new DefaultAsyncHttpClientConfig.Builder();
        builder.setConnectTimeout(REQUEST_TIMEOUT_MS);
        return builder.build();
    }

    @Override
    public void handleSendEvent(Device device, int resendCount, boolean isdimmer, Command command)
            throws TellstickException {
        logger.debug("Send {} to {}", command, device);
        if (device instanceof TellstickNetDevice) {
            if (command == OnOffType.ON) {
                turnOn(device);
            } else if (command == OnOffType.OFF) {
                turnOff(device);
            } else if (command instanceof PercentType) {
                dim(device, (PercentType) command);
            } else if (command instanceof IncreaseDecreaseType) {
                increaseDecrease(device, ((IncreaseDecreaseType) command));
            }
        } else if (device instanceof SwitchableDevice) {
            if (command == OnOffType.ON) {
                if (isdimmer) {
                    logger.debug("Turn off first in case it is allready on");
                    turnOff(device);
                }
                turnOn(device);
            } else if (command == OnOffType.OFF) {
                turnOff(device);
            }
        } else {
            logger.warn("Cannot send to {}", device);
        }
    }

    private void increaseDecrease(Device dev, IncreaseDecreaseType increaseDecreaseType) throws TellstickException {
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

    private void dim(Device dev, PercentType command) throws TellstickException {
        double value = command.doubleValue();

        // 0 means OFF and 100 means ON
        if (value == 0 && dev instanceof TellstickNetDevice) {
            turnOff(dev);
        } else if (value == 100 && dev instanceof TellstickNetDevice) {
            turnOn(dev);
        } else if (dev instanceof TellstickNetDevice
                && (((TellstickNetDevice) dev).getMethods() & JNA.CLibrary.TELLSTICK_DIM) > 0) {
            long tdVal = Math.round((value / 100) * 255);
            TelldusLiveResponse response = callRestMethod(String.format(HTTP_TELLDUS_DEVICE_DIM, dev.getId(), tdVal),
                    TelldusLiveResponse.class);
            handleResponse((TellstickNetDevice) dev, response);
        } else {
            throw new TelldusBindingException("Cannot send DIM to " + dev);
        }
    }

    private void turnOff(Device dev) throws TellstickException {
        if (dev instanceof TellstickNetDevice) {
            TelldusLiveResponse response = callRestMethod(String.format(HTTP_TELLDUS_DEVICE_TURNOFF, dev.getId()),
                    TelldusLiveResponse.class);
            handleResponse((TellstickNetDevice) dev, response);
        } else {
            throw new TelldusBindingException("Cannot send OFF to " + dev);
        }
    }

    private void handleResponse(TellstickNetDevice device, TelldusLiveResponse response) throws TellstickException {
        if (response == null || (response.status == null && response.error == null)) {
            throw new TelldusBindingException("No response " + response);
        } else if (response.error != null) {
            if (response.error.equals("The client for this device is currently offline")) {
                device.setOnline(false);
                device.setUpdated(true);
            }
            throw new TelldusBindingException("Error " + response.error);
        } else if (!response.status.trim().equals("success")) {
            throw new TelldusBindingException("Response " + response.status);
        }
    }

    private void turnOn(Device dev) throws TellstickException {
        if (dev instanceof TellstickNetDevice) {
            TelldusLiveResponse response = callRestMethod(String.format(HTTP_TELLDUS_DEVICE_TURNON, dev.getId()),
                    TelldusLiveResponse.class);
            handleResponse((TellstickNetDevice) dev, response);
        } else {
            throw new TelldusBindingException("Cannot send ON to " + dev);
        }
    }

    @Override
    public State calcState(Device dev) {
        TellstickNetDevice device = (TellstickNetDevice) dev;
        State st = null;
        if (device.getOnline()) {
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
        }
        return st;
    }

    @Override
    public BigDecimal calcDimValue(Device device) {
        BigDecimal dimValue = new BigDecimal(0);
        switch (((TellstickNetDevice) device).getState()) {
            case JNA.CLibrary.TELLSTICK_TURNON:
                dimValue = new BigDecimal(100);
                break;
            case JNA.CLibrary.TELLSTICK_TURNOFF:
                break;
            case JNA.CLibrary.TELLSTICK_DIM:
                dimValue = new BigDecimal(((TellstickNetDevice) device).getStatevalue());
                dimValue = dimValue.multiply(new BigDecimal(100));
                dimValue = dimValue.divide(new BigDecimal(255), 0, RoundingMode.HALF_UP);
                break;
            default:
                logger.warn("Could not handle {} for {}", (((TellstickNetDevice) device).getState()), device);
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

    <T> T callRestMethod(String uri, Class<T> response) throws TelldusLiveException {
        Instant start = Instant.now();
        T resultObj = null;
        try {
            resultObj = innerCallRest(uri, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logResponse(uri, e);
            monitorAdditionalRequest(start, Instant.now(), e);
            throw new TelldusLiveException(e);
        } catch (TimeoutException | ExecutionException | JAXBException | XMLStreamException e) {
            logResponse(uri, e);
            monitorAdditionalRequest(start, Instant.now(), e);
            throw new TelldusLiveException(e);
        }
        monitorAdditionalRequest(start, Instant.now(), null);
        return resultObj;
    }

    private void monitorAdditionalRequest(Instant start, Instant end, @Nullable Throwable exception) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        long duration = Duration.between(start, end).toMillis();
        sumRequestDuration += duration;
        nbRequest++;
        if (duration < minRequestDuration) {
            minRequestDuration = duration;
        }
        if (duration > maxRequestDuration) {
            maxRequestDuration = duration;
        }
        if (exception instanceof TimeoutException) {
            nbTimeouts++;
        } else if (exception != null) {
            nbErrors++;
        }
    }

    public long getAverageRequestDuration() {
        return nbRequest == 0 ? 0 : sumRequestDuration / nbRequest;
    }

    public long getMinRequestDuration() {
        return minRequestDuration;
    }

    public long getMaxRequestDuration() {
        return maxRequestDuration;
    }

    public int getNbTimeouts() {
        return nbTimeouts;
    }

    public int getNbErrors() {
        return nbErrors;
    }

    private <T> T innerCallRest(String uri, Class<T> response) throws InterruptedException, ExecutionException,
            TimeoutException, JAXBException, FactoryConfigurationError, XMLStreamException {
        Future<Response> future = client.prepareGet(uri).execute();
        Response resp = future.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (resp.getStatusCode() != 200) {
            throw new ExecutionException("HTTP request returned status code " + resp.getStatusCode(), null);
        }
        // TelldusLiveHandler.logger.info("Devices" + resp.getResponseBody());
        JAXBContext jc = JAXBContext.newInstance(response);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader xsr = xif.createXMLStreamReader(resp.getResponseBodyAsStream());
        // xsr = new PropertyRenamerDelegate(xsr);

        @SuppressWarnings("unchecked")
        T obj = (T) jc.createUnmarshaller().unmarshal(xsr);
        if (logger.isTraceEnabled()) {
            logger.trace("Request [{}] Response:{}", uri, resp.getResponseBody());
        }
        return obj;
    }

    private void logResponse(String uri, Exception e) {
        logger.warn("Request [{}] failed: {} {}", uri, e.getClass().getSimpleName(), e.getMessage());
    }
}
