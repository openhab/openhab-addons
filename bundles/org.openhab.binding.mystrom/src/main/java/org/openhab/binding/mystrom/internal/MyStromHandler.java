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
package org.openhab.binding.mystrom.internal;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.WATT;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_SWITCH;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_TEMPERATURE;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link MyStromHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Frank - Initial contribution
 */
@NonNullByDefault
public class MyStromHandler extends BaseThingHandler {

    private static class MyStromReport {

        public float power;
        public boolean relay;
        public float temperature;
    }

    private static final int HTTP_OK_CODE = 200;
    private static final String COMMUNICATION_ERROR = "Error while communicating to the myStrom plug: ";
    private static final String HTTP_REQUEST_URL_PREFIX = "http://";

    private final Logger logger = LoggerFactory.getLogger(MyStromHandler.class);

    private HttpClient httpClient;
    private String hostname = "";

    private @Nullable ScheduledFuture<?> pollingJob;
    private ExpiringCache<MyStromReport> cache = new ExpiringCache<>(Duration.ofSeconds(3), this::getReport);
    private final Gson gson = new Gson();

    public MyStromHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                pollDevice();
            } else {
                if (command instanceof OnOffType && CHANNEL_SWITCH.equals(channelUID.getId())) {
                    sendHttpGet("relay?state=" + (command == OnOffType.ON ? "1" : "0"));
                    scheduler.schedule(this::pollDevice, 500, TimeUnit.MILLISECONDS);
                }
            }
        } catch (MyStromException e) {
            logger.warn("Error while handling command {}", e.getMessage());
        }
    }

    private @Nullable MyStromReport getReport() {
        try {
            String returnContent = sendHttpGet("report");
            MyStromReport report = gson.fromJson(returnContent, MyStromReport.class);
            updateStatus(ThingStatus.ONLINE);
            return report;
        } catch (MyStromException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return null;
        }
    }

    private void pollDevice() {
        MyStromReport report = cache.getValue();
        if (report != null) {
            updateState(CHANNEL_SWITCH, report.relay ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_POWER, QuantityType.valueOf(report.power, WATT));
            updateState(CHANNEL_TEMPERATURE, QuantityType.valueOf(report.temperature, CELSIUS));
        }
    }

    @Override
    public void initialize() {
        MyStromConfiguration config = getConfigAs(MyStromConfiguration.class);
        this.hostname = HTTP_REQUEST_URL_PREFIX + config.hostname;

        updateStatus(ThingStatus.UNKNOWN);
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevice, 0, config.refresh, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location
     * created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @return String contents of the response for the GET request.
     * @throws Exception
     */
    public String sendHttpGet(String action) throws MyStromException {
        String url = hostname + "/" + action;
        ContentResponse response = null;
        try {
            response = httpClient.newRequest(url).timeout(10, TimeUnit.SECONDS).method(HttpMethod.GET).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MyStromException(COMMUNICATION_ERROR + e.getMessage());
        }

        if (response.getStatus() != HTTP_OK_CODE) {
            throw new MyStromException(
                    "Error sending HTTP GET request to " + url + ". Got response code: " + response.getStatus());
        }
        return response.getContentAsString();
    }
}
