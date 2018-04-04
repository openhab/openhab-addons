/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.internal.live;

import java.math.BigDecimal;
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
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tellstick.handler.TelldusDeviceController;
import org.openhab.binding.tellstick.internal.live.xml.TelldusLiveResponse;
import org.openhab.binding.tellstick.internal.live.xml.TellstickNetDevice;
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
 * @author Jarle Hjortland
 */

public class TelldusLiveDeviceController implements DeviceChangeListener, SensorListener, TelldusDeviceController {
    private final Logger logger = LoggerFactory.getLogger(TelldusLiveDeviceController.class);
    private long lastSend = 0;
    public static final long DEFAULT_INTERVAL_BETWEEN_SEND = 250;
    static final int REQUEST_TIMEOUT_MS = 5000;
    private AsyncHttpClient client;
    static final String HTTP_API_TELLDUS_COM_XML = "http://api.telldus.com/xml/";
    static final String HTTP_TELLDUS_CLIENTS = HTTP_API_TELLDUS_COM_XML + "clients/list";
    static final String HTTP_TELLDUS_DEVICES = HTTP_API_TELLDUS_COM_XML + "devices/list?supportedMethods=19";
    static final String HTTP_TELLDUS_SENSORS = HTTP_API_TELLDUS_COM_XML + "sensors/list?includeValues=1&includeScale=1";
    static final String HTTP_TELLDUS_SENSOR_INFO = HTTP_API_TELLDUS_COM_XML + "sensor/info";
    static final String HTTP_TELLDUS_DEVICE_DIM = HTTP_API_TELLDUS_COM_XML + "device/dim?id=%d&level=%d";
    static final String HTTP_TELLDUS_DEVICE_TURNOFF = HTTP_API_TELLDUS_COM_XML + "device/turnOff?id=%d";
    static final String HTTP_TELLDUS_DEVICE_TURNON = HTTP_API_TELLDUS_COM_XML + "device/turnOn?id=%d";
    private static final int MAX_RETRIES = 3;

    public TelldusLiveDeviceController() {
    }

    @Override
    public void dispose() {
        try {
            client.close();
        } catch (Exception e) {
            logger.error("Failed to close client", e);
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
            // TODO Auto-generated catch block
            logger.error("Failed to connect", e);
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

        logger.info("Send {} to {}", command, device);
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
            throw new RuntimeException("Cannot send DIM to " + dev);
        }
    }

    private void turnOff(Device dev) throws TellstickException {
        if (dev instanceof TellstickNetDevice) {
            TelldusLiveResponse response = callRestMethod(String.format(HTTP_TELLDUS_DEVICE_TURNOFF, dev.getId()),
                    TelldusLiveResponse.class);
            handleResponse((TellstickNetDevice) dev, response);
        } else {
            throw new RuntimeException("Cannot send OFF to " + dev);
        }
    }

    private void handleResponse(TellstickNetDevice device, TelldusLiveResponse response) {
        if (response == null || (response.status == null && response.error == null)) {
            throw new RuntimeException("No response " + response);
        } else if (response.error != null) {
            if (response.error.equals("The client for this device is currently offline")) {
                device.setOnline(false);
                device.setUpdated(true);
            }
            throw new RuntimeException("Error " + response.error);
        } else if (!response.status.trim().equals("success")) {
            throw new RuntimeException("Response " + response.status);
        }
    }

    private void turnOn(Device dev) throws TellstickException {
        if (dev instanceof TellstickNetDevice) {
            TelldusLiveResponse response = callRestMethod(String.format(HTTP_TELLDUS_DEVICE_TURNON, dev.getId()),
                    TelldusLiveResponse.class);
            handleResponse((TellstickNetDevice) dev, response);
        } else {
            throw new RuntimeException("Cannot send ON to " + dev);
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
                dimValue = dimValue.divide(new BigDecimal(255), 0, BigDecimal.ROUND_HALF_UP);
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
        T resultObj = null;
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    resultObj = innerCallRest(uri, response);
                    break;
                } catch (TimeoutException e) {
                    logger.warn("TimeoutException error in get", e);
                } catch (InterruptedException e) {
                    logger.warn("InterruptedException error in get", e);
                }
            }
        } catch (JAXBException e) {
            logger.warn("Encoding error in get", e);
            logResponse(uri, e);
            throw new TelldusLiveException(e);
        } catch (XMLStreamException e) {
            logger.warn("Communication error in get", e);
            logResponse(uri, e);
            throw new TelldusLiveException(e);
        } catch (ExecutionException e) {
            logger.warn("ExecutionException error in get", e);
            throw new TelldusLiveException(e);
        }
        return resultObj;
    }

    private <T> T innerCallRest(String uri, Class<T> response) throws InterruptedException, ExecutionException,
            TimeoutException, JAXBException, FactoryConfigurationError, XMLStreamException {
        Future<Response> future = client.prepareGet(uri).execute();
        Response resp = future.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        // TelldusLiveHandler.logger.info("Devices" + resp.getResponseBody());
        JAXBContext jc = JAXBContext.newInstance(response);
        XMLInputFactory xif = XMLInputFactory.newInstance();
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
        if (e != null) {
            logger.warn("Request [{}] Failure:{}", uri, e.getMessage());
        }
    }
}
