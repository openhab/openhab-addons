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
package org.openhab.binding.mystrom.internal;

import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.*;

import java.text.DateFormat;
import java.util.Calendar;
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
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AbstractMyStromHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frederic Chastagnol - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMyStromHandler extends BaseThingHandler {
    protected static final String COMMUNICATION_ERROR = "Error while communicating to the myStrom plug: ";
    protected MyStromConfiguration config;

    protected final HttpClient httpClient;
    protected String mac = "";

    private final Logger logger = LoggerFactory.getLogger(AbstractMyStromHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob;
    protected final Gson gson = new Gson();

    public AbstractMyStromHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        config = getConfigAs(MyStromConfiguration.class);
        this.httpClient = httpClient;
    }

    @Override
    public final void initialize() {
        config = getConfigAs(MyStromConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.schedule(this::initializeInternal, 0, TimeUnit.SECONDS);
    }

    @Override
    public final void dispose() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
        super.dispose();
    }

    private void updateProperties() {
        try {
            String json = sendHttpRequest(HttpMethod.GET, "/api/v1/info", null);
            MyStromDeviceInfo deviceInfo = gson.fromJson(json, MyStromDeviceInfo.class);
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
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM,
                    Locale.getDefault());
            properties.put(PROPERTY_LAST_REFRESH, formatter.format(calendar.getTime()));
            updateProperties(properties);
        } catch (JsonSyntaxException | MyStromException ex) {
            logger.debug("Updating properties failed: ", ex);
        }
    }

    /**
     * Calls the API with the given http method, request path and actual data.
     *
     * @param method the http method to make the call with
     * @param path The path of the API endpoint
     * @param requestData the actual raw data to send in the request body, may be {@code null}
     * @return String contents of the response for the GET request.
     * @throws MyStromException Throws on communication error
     */
    protected final String sendHttpRequest(HttpMethod method, String path, @Nullable String requestData)
            throws MyStromException {
        String url = config.getHostname() + path;
        try {
            Request request = httpClient.newRequest(url).timeout(10, TimeUnit.SECONDS).method(method);
            if (!config.getApiToken().isEmpty()) {
                request.getHeaders().add("Token", config.getApiToken());
            }
            if (requestData != null) {
                request = request.content(new StringContentProvider(requestData)).header(HttpHeader.CONTENT_TYPE,
                        "application/x-www-form-urlencoded");
            }
            ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new MyStromException("Error sending HTTP " + method + " request to " + url
                        + ". Got response code: " + response.getStatus());
            }
            return response.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MyStromException(COMMUNICATION_ERROR + e.getMessage());
        }
    }

    private void initializeInternal() {
        try {
            updateProperties();
            checkRequiredInfo();
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevice, 0, config.getRefresh(), TimeUnit.SECONDS);
        } catch (MyStromException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    protected void checkRequiredInfo() throws MyStromException {
    }

    protected abstract void pollDevice();
}
