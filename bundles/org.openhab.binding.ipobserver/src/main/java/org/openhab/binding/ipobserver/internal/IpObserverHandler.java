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
import org.openhab.core.library.unit.ImperialUnits;
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
 * @author Thomas Hentschel - Initial contribution.
 * @author Matthew Skinner - Full re-write for BND, V3.0 and UOM
 */
@NonNullByDefault
public class IpObserverHandler extends BaseThingHandler {
    private final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(IpObserverHandler.class);
    private Map<String, ChannelHandler> channelHandlers = new HashMap<String, ChannelHandler>();
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private IpObserverConfiguration config = new IpObserverConfiguration();

    private class ChannelHandler {
        private IpObserverHandler handler;
        private Channel channel;
        private String previousValue = "";
        private Unit<?> unit;
        private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

        ChannelHandler(IpObserverHandler handler, Channel channel, Class<? extends State> acceptable, Unit<?> unit) {
            super();
            this.handler = handler;
            this.channel = channel;
            this.unit = unit;
            acceptedDataTypes.add(acceptable);
        }

        public void processValue(String sensorValue) {
            if (!sensorValue.equals(previousValue)) {
                previousValue = sensorValue;
                State state = TypeParser.parseState(this.acceptedDataTypes, sensorValue);
                if (state == null) {
                    return;
                } else if (state instanceof QuantityType) {
                    handler.updateState(this.channel.getUID(),
                            QuantityType.valueOf(Double.parseDouble(sensorValue), unit));
                } else {
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
        config.solarUnit = doc.select("select[name=unit_Solar] option[selected]").val();
        config.windUnit = doc.select("select[name=unit_Wind] option[selected]").val();
        config.pressureUnit = doc.select("select[name=unit_Pressure] option[selected]").val();
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
        String value = doc.select("select[name=inBattSta] option[selected]").val();
        ChannelHandler localUpdater = channelHandlers.get("inBattSta");
        if (localUpdater != null) {
            localUpdater.processValue(value);
        }
        value = doc.select("select[name=outBattSta] option[selected]").val();
        localUpdater = channelHandlers.get("outBattSta");
        if (localUpdater != null) {
            localUpdater.processValue(value);
        }

        Elements elements = doc.select("input");
        for (Element element : elements) {
            String elementName = element.attr("name");
            value = element.attr("value");
            if (!value.isEmpty()) {
                logger.trace("Found element {}, value is {}", elementName, value);
                localUpdater = channelHandlers.get(elementName);
                if (localUpdater != null) {
                    localUpdater.processValue(value);
                }
            }
        }
    }

    private void sendGetRequest(String url) {
        Request request = httpClient.newRequest("http://" + config.address + url);
        request.method(HttpMethod.GET).timeout(5, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");
        String errorReason = "";
        try {
            long start = System.currentTimeMillis();
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                long responseTime = (System.currentTimeMillis() - start);
                if (!this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Finding out which units of measurement the weather station is using.");
                    sendGetRequest(STATION_SETTINGS_URL);
                }
                updateState(RESPONSE_TIME, new QuantityType<>(responseTime, MetricPrefix.MILLI(Units.SECOND)));
                if (url == STATION_SETTINGS_URL) {
                    parseSettings(contentResponse.getContentAsString());
                    setupChannels();
                } else {
                    parseAndUpdate(contentResponse.getContentAsString());
                }
                if (config.autoReboot > 0 && responseTime > config.autoReboot) {
                    logger.debug("An Auto reboot of the IP Observer unit has been triggered as the response was {}ms.",
                            responseTime);
                    sendGetRequest(REBOOT_URL);
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

    private void createChannelHandler(String chanName, Class<? extends State> type, Unit<?> unit, String htmlName) {
        @Nullable
        Channel channel = this.getThing().getChannel(chanName);
        if (channel != null) {
            channelHandlers.put(htmlName, new ChannelHandler(this, channel, type, unit));
        }
    }

    private void setupChannels() {
        if (config.imperialTemperature) {
            logger.debug("Using imperial units of measurement for temperature.");
            createChannelHandler(TEMP_INDOOR, QuantityType.class, ImperialUnits.FAHRENHEIT, "inTemp");
            createChannelHandler(TEMP_OUTDOOR, QuantityType.class, ImperialUnits.FAHRENHEIT, "outTemp");
        } else {
            logger.debug("Using metric units of measurement for temperature.");
            createChannelHandler(TEMP_INDOOR, QuantityType.class, SIUnits.CELSIUS, "inTemp");
            createChannelHandler(TEMP_OUTDOOR, QuantityType.class, SIUnits.CELSIUS, "outTemp");
        }

        if (config.imperialRain) {
            createChannelHandler(HOURLY_RAIN_RATE, QuantityType.class, ImperialUnits.INCH, "rainofhourly");
            createChannelHandler(DAILY_RAIN, QuantityType.class, ImperialUnits.INCH, "rainofdaily");
            createChannelHandler(WEEKLY_RAIN, QuantityType.class, ImperialUnits.INCH, "rainofweekly");
            createChannelHandler(MONTHLY_RAIN, QuantityType.class, ImperialUnits.INCH, "rainofmonthly");
            createChannelHandler(YEARLY_RAIN, QuantityType.class, ImperialUnits.INCH, "rainofyearly");
        } else {
            logger.debug("Using metric units of measurement for rain.");
            createChannelHandler(HOURLY_RAIN_RATE, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE),
                    "rainofhourly");
            createChannelHandler(DAILY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofdaily");
            createChannelHandler(WEEKLY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofweekly");
            createChannelHandler(MONTHLY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofmonthly");
            createChannelHandler(YEARLY_RAIN, QuantityType.class, MetricPrefix.MILLI(SIUnits.METRE), "rainofyearly");
        }

        if (config.windUnit.equals("5")) {
            createChannelHandler(WIND_AVERAGE_SPEED, QuantityType.class, Units.KNOT, "avgwind");
            createChannelHandler(WIND_SPEED, QuantityType.class, Units.KNOT, "windspeed");
            createChannelHandler(WIND_GUST, QuantityType.class, Units.KNOT, "gustspeed");
            createChannelHandler(WIND_MAX_GUST, QuantityType.class, Units.KNOT, "dailygust");
        } else if (config.windUnit.equals("4")) {
            createChannelHandler(WIND_AVERAGE_SPEED, QuantityType.class, ImperialUnits.MILES_PER_HOUR, "avgwind");
            createChannelHandler(WIND_SPEED, QuantityType.class, ImperialUnits.MILES_PER_HOUR, "windspeed");
            createChannelHandler(WIND_GUST, QuantityType.class, ImperialUnits.MILES_PER_HOUR, "gustspeed");
            createChannelHandler(WIND_MAX_GUST, QuantityType.class, ImperialUnits.MILES_PER_HOUR, "dailygust");
        } else if (config.windUnit.equals("1")) {
            createChannelHandler(WIND_AVERAGE_SPEED, QuantityType.class, SIUnits.KILOMETRE_PER_HOUR, "avgwind");
            createChannelHandler(WIND_SPEED, QuantityType.class, SIUnits.KILOMETRE_PER_HOUR, "windspeed");
            createChannelHandler(WIND_GUST, QuantityType.class, SIUnits.KILOMETRE_PER_HOUR, "gustspeed");
            createChannelHandler(WIND_MAX_GUST, QuantityType.class, SIUnits.KILOMETRE_PER_HOUR, "dailygust");
        } else if (config.windUnit.equals("0")) {
            createChannelHandler(WIND_AVERAGE_SPEED, QuantityType.class, Units.METRE_PER_SECOND, "avgwind");
            createChannelHandler(WIND_SPEED, QuantityType.class, Units.METRE_PER_SECOND, "windspeed");
            createChannelHandler(WIND_GUST, QuantityType.class, Units.METRE_PER_SECOND, "gustspeed");
            createChannelHandler(WIND_MAX_GUST, QuantityType.class, Units.METRE_PER_SECOND, "dailygust");
        } else {
            logger.warn(
                    "The IP Observer is sending a wind format the binding does not support. Select one of the other units.");
        }

        if (config.solarUnit.equals("1")) {
            createChannelHandler(SOLAR_RADIATION, QuantityType.class, Units.IRRADIANCE, "solarrad");
        } else if (config.solarUnit.equals("0")) {
            createChannelHandler(SOLAR_RADIATION, QuantityType.class, Units.LUX, "solarrad");
        } else {
            logger.warn(
                    "The IP Observer is sending fc (Foot Candles) for the solar radiation. Select one of the other units.");
        }

        if (config.pressureUnit.equals("0")) {
            createChannelHandler(ABS_PRESSURE, QuantityType.class, MetricPrefix.HECTO(SIUnits.PASCAL), "AbsPress");
            createChannelHandler(REL_PRESSURE, QuantityType.class, MetricPrefix.HECTO(SIUnits.PASCAL), "RelPress");
        } else if (config.pressureUnit.equals("1")) {
            createChannelHandler(ABS_PRESSURE, QuantityType.class, ImperialUnits.INCH_OF_MERCURY, "AbsPress");
            createChannelHandler(REL_PRESSURE, QuantityType.class, ImperialUnits.INCH_OF_MERCURY, "RelPress");
        } else if (config.pressureUnit.equals("2")) {
            createChannelHandler(ABS_PRESSURE, QuantityType.class, Units.MILLIMETRE_OF_MERCURY, "AbsPress");
            createChannelHandler(REL_PRESSURE, QuantityType.class, Units.MILLIMETRE_OF_MERCURY, "RelPress");
        }

        createChannelHandler(WIND_DIRECTION, QuantityType.class, Units.DEGREE_ANGLE, "windir");
        createChannelHandler(INDOOR_HUMIDITY, DecimalType.class, Units.PERCENT, "inHumi");
        createChannelHandler(OUTDOOR_HUMIDITY, DecimalType.class, Units.PERCENT, "outHumi");
        // The units for the following are ignored as they are not a QuantityType.class
        createChannelHandler(UV, DecimalType.class, SIUnits.CELSIUS, "uv");
        createChannelHandler(UV_INDEX, DecimalType.class, SIUnits.CELSIUS, "uvi");
        // was outBattSta1 so some units may use this instead?
        createChannelHandler(OUTDOOR_BATTERY, StringType.class, Units.PERCENT, "outBattSta");
        createChannelHandler(OUTDOOR_BATTERY, StringType.class, Units.PERCENT, "outBattSta1");
        createChannelHandler(INDOOR_BATTERY, StringType.class, Units.PERCENT, "inBattSta");
        createChannelHandler(LAST_UPDATED_TIME, StringType.class, SIUnits.CELSIUS, "CurrTime");
    }

    @Override
    public void initialize() {
        config = getConfigAs(IpObserverConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        pollingFuture = scheduler.scheduleWithFixedDelay(this::pollStation, 1, config.pollTime, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        channelHandlers.clear();
        @Nullable
        ScheduledFuture<?> localFuture = pollingFuture;
        if (localFuture != null) {
            localFuture.cancel(true);
            localFuture = null;
        }
    }
}
