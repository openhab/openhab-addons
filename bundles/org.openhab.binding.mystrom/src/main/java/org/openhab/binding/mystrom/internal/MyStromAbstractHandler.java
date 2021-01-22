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

import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_CONNECTED;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_DNS;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_GW;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_IP;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_LAST_REFRESH;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_MAC;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_MASK;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_SSID;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_STATIC;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_TYPE;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.PROPERTY_VERSION;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

import com.google.gson.Gson;

/**
 * The {@link MyStromAbstractHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frederic Chastagnol - Initial contribution
 */
@NonNullByDefault
public abstract class MyStromAbstractHandler extends BaseThingHandler {
    protected static final int HTTP_OK_CODE = 200;
    protected static final String COMMUNICATION_ERROR = "Error while communicating to the myStrom plug: ";
    protected static final String HTTP_REQUEST_URL_PREFIX = "http://";

    private final HttpClient httpClient;
    private String hostname = "";
    private String mac = "";

    private @Nullable ScheduledFuture<?> pollingJob;
    private final Gson gson = new Gson();

    public MyStromAbstractHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public final void initialize() {
        MyStromConfiguration config = getConfigAs(MyStromConfiguration.class);
        this.hostname = HTTP_REQUEST_URL_PREFIX + config.hostname;

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.schedule(this::initializeInternal, 0, TimeUnit.SECONDS);
    }

    @Override
    public final void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    @Override
    public final Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MyStromDeviceActions.class);
    }

    public final void updateProperties() throws MyStromException {
        @Nullable
        MyStromDeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            throw new MyStromException("Cannot retrieve device info from myStrom device " + getThing().getUID());
        }
        this.mac = deviceInfo.mac;
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_MAC, deviceInfo.mac);
        properties.put(PROPERTY_VERSION, deviceInfo.version);
        properties.put(PROPERTY_TYPE, Long.toString(deviceInfo.type));
        properties.put(PROPERTY_SSID, deviceInfo.ssid);
        properties.put(PROPERTY_IP, deviceInfo.ip);
        properties.put(PROPERTY_MASK, deviceInfo.mask);
        properties.put(PROPERTY_GW, deviceInfo.gw);
        properties.put(PROPERTY_DNS, deviceInfo.dns);
        properties.put(PROPERTY_STATIC, Boolean.toString(deviceInfo.staticState));
        properties.put(PROPERTY_CONNECTED, Boolean.toString(deviceInfo.connected));
        Calendar calendar = Calendar.getInstance();
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
        properties.put(PROPERTY_LAST_REFRESH, formatter.format(calendar.getTime()));
        updateProperties(properties);
    }

    private @Nullable MyStromDeviceInfo getDeviceInfo() throws MyStromException {
        String url = hostname + "/api/v1/info";
        ContentResponse response;
        try {
            Request request = httpClient.newRequest(url).timeout(10, TimeUnit.SECONDS).method(HttpMethod.GET);
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MyStromException(COMMUNICATION_ERROR + e.getMessage());
        }

        if (response.getStatus() != HTTP_OK_CODE) {
            throw new MyStromException(
                    "Error sending HTTP GET request to " + url + ". Got response code: " + response.getStatus());
        }

        String gs = response.getContentAsString();
        if (gs == null) {
            return null;
        }
        return gson.fromJson(gs, MyStromDeviceInfo.class);
    }

    private void initializeInternal() {
        try {
            updateProperties();
            updateStatus(ThingStatus.ONLINE);
            MyStromConfiguration config = getConfigAs(MyStromConfiguration.class);
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevice, 0, config.refresh, TimeUnit.SECONDS);
        } catch (MyStromException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    protected final HttpClient getHttpClient() {
        return httpClient;
    }

    protected final String getHostname() {
        return hostname;
    }

    protected final String getMac() {
        return mac;
    }

    protected final Gson getGson() {
        return gson;
    }

    protected abstract void pollDevice();
}
