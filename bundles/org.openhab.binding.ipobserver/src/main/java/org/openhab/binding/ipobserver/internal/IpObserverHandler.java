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
package org.openhab.binding.ipobserver.internal;

import static org.openhab.binding.ipobserver.internal.IpObserverBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpObserverHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpObserverHandler extends BaseThingHandler {
    private final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(IpObserverHandler.class);
    private Map<String, UpdateHandler> updateHandlers = new HashMap<String, UpdateHandler>();
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private IpObserverConfiguration config = new IpObserverConfiguration();

    class UpdateHandler {
        private IpObserverHandler handler;
        private Channel channel;
        private String currentState = "";
        private Unit<?> unit;
        private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

        UpdateHandler(IpObserverHandler handler, Channel channel, Class<? extends State> acceptable, Unit<?> unit) {
            super();
            this.handler = handler;
            this.channel = channel;
            this.unit = unit;
            acceptedDataTypes.add(acceptable);
        }

        public void processMessage(String sensorValue) {
            if (!Objects.equals(sensorValue, this.currentState)) {
                this.currentState = sensorValue;
                State state = TypeParser.parseState(this.acceptedDataTypes, sensorValue);
                if (state instanceof QuantityType) {
                    handler.updateState(this.channel.getUID(),
                            QuantityType.valueOf(Double.parseDouble(sensorValue), unit));
                } else if (state != null) {
                    this.handler.updateState(this.channel.getUID(), state);
                }
            }
        }
    }

    public IpObserverHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void parseSettings(String html) {
        Document doc = Jsoup.parse(html);
        // 0=lux, 1=w/m2, 2=fc
        config.solarUnit = doc.select("select[name=unit_Solar] option[selected]").val();
        // 0=m/s, 1=km/h, 2=ft/s, 3=bft, 4=mph, 5=knot
        config.windUnit = doc.select("select[name=unit_Wind] option[selected]").val();
        // 0=degC, 1=degF
        if ("1".equals(doc.select("select[name=u_Temperature] option[selected]").val())) {
            config.imperialTemperature = true;
        } else {
            config.imperialTemperature = false;
        }
        // 0=mm, 1=in
        if ("1".equals(doc.select("select[name=u_Rainfall] option[selected]").val())) {
            config.imperialRain = true;
        } else {
            config.imperialRain = false;
        }
    }

    private void parseAndUpdate(String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("input");
        for (Element element : elements) {
            String elementName = element.attr("name");
            String value = element.attr("value");
            logger.debug("Found element {}, value is {}", elementName, value);
            if (value != null) {
                UpdateHandler localUpdater = updateHandlers.get(elementName);
                if (localUpdater != null) {
                    localUpdater.processMessage(value);
                }
            }
        }
    }

    private void sendGetRequest(String url) {
        Request request = httpClient.newRequest("http://" + config.address + url);
        request.method(HttpMethod.GET).timeout(5, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
                if (url == STATION_SETTINGS_URL) {
                    parseSettings(contentResponse.getContentAsString());
                } else {
                    parseAndUpdate(contentResponse.getContentAsString());
                }
                return;
            } else {
                errorReason = String.format("IpObserver request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: IpObserver was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorReason = String.format("InterruptedException: %s", e.getMessage());
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorReason);
    }

    private void pollStation() {
        sendGetRequest(LIVE_DATA_URL);
    }

    private void createChannel(String chanName, Class<? extends State> type, Unit<?> unit, String htmlName) {
        @Nullable
        Channel channel = this.getThing().getChannel(chanName);
        if (channel != null) {
            updateHandlers.put(htmlName, new UpdateHandler(this, channel, type, unit));
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(IpObserverConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        sendGetRequest(STATION_SETTINGS_URL);
        createChannel(INDOOR_TEMP, QuantityType.class, SIUnits.CELSIUS, "inTemp");// ImperialUnits.FAHRENHEIT
        createChannel(OUTDOOR_TEMP, QuantityType.class, SIUnits.CELSIUS, "outTemp");
        createChannel(INDOOR_HUMIDITY, DecimalType.class, Units.PERCENT, "inHumi");
        createChannel(OUTDOOR_HUMIDITY, DecimalType.class, Units.PERCENT, "outHumi");
        createChannel(ABS_PRESSURE, DecimalType.class, SIUnits.PASCAL, "AbsPress");
        createChannel(REL_PRESSURE, DecimalType.class, SIUnits.PASCAL, "RelPress");
        createChannel(WIND_DIRECTION, DecimalType.class, SIUnits.CELSIUS, "windir");
        createChannel(WIND_SPEED, QuantityType.class, Units.KNOT, "avgwind");
        createChannel(WIND_SPEED2, QuantityType.class, Units.KNOT, "windspeed");
        createChannel(WIND_GUST, QuantityType.class, Units.KNOT, "gustspeed");
        createChannel(DAILY_GUST, QuantityType.class, Units.KNOT, "dailygust");
        createChannel(SOLAR_RADIATION, DecimalType.class, SIUnits.CELSIUS, "solarrad");
        createChannel(UV, DecimalType.class, SIUnits.CELSIUS, "uv");
        createChannel(UVI, DecimalType.class, SIUnits.CELSIUS, "uvi");
        createChannel(HOURLY_RAIN, QuantityType.class, Units.MILLIMETRE_PER_HOUR, "rainofhourly");
        createChannel(DAILY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofdaily");
        createChannel(WEEKLY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofweekly");
        createChannel(MONTHLY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofmonthly");
        createChannel(YEARLY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofyearly");
        createChannel(BATTERY_OUT, StringType.class, Units.PERCENT, "outBattSta1");
        createChannel(INDOOR_BATTERY, StringType.class, Units.PERCENT, "inBattSta");
        createChannel(LAST_UPDATED_TIME, StringType.class, SIUnits.CELSIUS, "CurrTime");
        pollingFuture = scheduler.scheduleWithFixedDelay(this::pollStation, 1, config.pollTime, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        updateHandlers.clear();
        if (pollingFuture != null) {
            pollingFuture.cancel(true);
            pollingFuture = null;
        }
    }
}
