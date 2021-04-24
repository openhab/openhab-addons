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

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Bulk updated to UOM.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;

<<<<<<< HEAD
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
=======
=======
>>>>>>> Bulk updated to UOM.
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
<<<<<<< HEAD
import org.openhab.core.types.RefreshType;
>>>>>>> ipObserver creation
=======
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
>>>>>>> Bulk updated to UOM.
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
<<<<<<< HEAD
<<<<<<< HEAD
    private final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(IpObserverHandler.class);
    private Map<String, ChannelHandler> channelHandlers = new HashMap<String, ChannelHandler>();
<<<<<<< HEAD
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private IpObserverConfiguration config = new IpObserverConfiguration();

    class ChannelHandler {
        private IpObserverHandler handler;
        private Channel channel;
        private String currentState = "";
        private Unit<?> unit;
        private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

        ChannelHandler(IpObserverHandler handler, Channel channel, Class<? extends State> acceptable, Unit<?> unit) {
            super();
            this.handler = handler;
            this.channel = channel;
            this.unit = unit;
            acceptedDataTypes.add(acceptable);
        }

        public void processMessage(String sensorValue) {
            if (!sensorValue.equals(this.currentState)) {
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
=======

=======
    private final HttpClient httpClient;
>>>>>>> Bulk updated to UOM.
    private final Logger logger = LoggerFactory.getLogger(IpObserverHandler.class);
    private Map<String, UpdateHandler> updateHandlers = new HashMap<String, UpdateHandler>();
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private IpObserverConfiguration config = new IpObserverConfiguration();

    class UpdateHandler {
=======
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private IpObserverConfiguration config = new IpObserverConfiguration();

    class ChannelHandler {
>>>>>>> Fix merge conflicts.
        private IpObserverHandler handler;
        private Channel channel;
        private String currentState = "";
        private Unit<?> unit;
        private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

<<<<<<< HEAD
        UpdateHandler(IpObserverHandler handler, Channel channel, Class<? extends State> acceptable, Unit<?> unit) {
=======
        ChannelHandler(IpObserverHandler handler, Channel channel, Class<? extends State> acceptable, Unit<?> unit) {
>>>>>>> Fix merge conflicts.
            super();
            this.handler = handler;
            this.channel = channel;
            this.unit = unit;
            acceptedDataTypes.add(acceptable);
        }

        public void processMessage(String sensorValue) {
<<<<<<< HEAD
            if (!Objects.equals(sensorValue, this.currentState)) {
=======
            if (!sensorValue.equals(this.currentState)) {
>>>>>>> Fix merge conflicts.
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
<<<<<<< HEAD
>>>>>>> ipObserver creation
=======
        this.httpClient = httpClient;
>>>>>>> Bulk updated to UOM.
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Bulk updated to UOM.
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
            localUpdater.processMessage(value);
        }
        value = doc.select("select[name=outBattSta] option[selected]").val();
        localUpdater = channelHandlers.get("outBattSta");
        if (localUpdater != null) {
            localUpdater.processMessage(value);
        }

        Elements elements = doc.select("input");
        for (Element element : elements) {
            String elementName = element.attr("name");
            value = element.attr("value");
            logger.trace("Found element {}, value is {}", elementName, value);
            if (!value.isEmpty()) {
                localUpdater = channelHandlers.get(elementName);
                if (localUpdater != null) {
                    localUpdater.processMessage(value);
                }
<<<<<<< HEAD
            }
        }
    }

    private void sendGetRequest(String url) {
        Request request = httpClient.newRequest("http://" + config.address + url);
        request.method(HttpMethod.GET).timeout(5, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");
        String errorReason = "";
        try {
<<<<<<< HEAD
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
=======
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
=======
>>>>>>> Bulk updated to UOM.
            }
        }
    }

    private void sendGetRequest(String url) {
        Request request = httpClient.newRequest("http://" + config.address + url);
        request.method(HttpMethod.GET).timeout(5, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");
        String errorReason = "";
        try {
=======
>>>>>>> Fix merge conflicts.
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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
>>>>>>> ipObserver creation
=======
    private void createChannel(String chanName, Class<? extends State> type, Unit<?> unit, String htmlName) {
        @Nullable
        Channel channel = this.getThing().getChannel(chanName);
        if (channel != null) {
            updateHandlers.put(htmlName, new UpdateHandler(this, channel, type, unit));
>>>>>>> Bulk updated to UOM.
=======
=======
>>>>>>> Fix merge conflicts.
    private void createChannelHandler(String chanName, Class<? extends State> type, Unit<?> unit, String htmlName) {
        @Nullable
        Channel channel = this.getThing().getChannel(chanName);
        if (channel != null) {
            channelHandlers.put(htmlName, new ChannelHandler(this, channel, type, unit));
>>>>>>> Fix time channels.
        }
    }

<<<<<<< HEAD
    @Override
    public void initialize() {
        config = getConfigAs(IpObserverConfiguration.class);
<<<<<<< HEAD
<<<<<<< HEAD
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
=======

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
=======
>>>>>>> Bulk updated to UOM.
        updateStatus(ThingStatus.UNKNOWN);
        sendGetRequest(STATION_SETTINGS_URL);
=======
    private void setupChannels() {
>>>>>>> updates
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
        createChannelHandler(INDOOR_BATTERY, StringType.class, Units.PERCENT, "inBattSta");
        createChannelHandler(LAST_UPDATED_TIME, StringType.class, SIUnits.CELSIUS, "CurrTime");
    }

    @Override
    public void initialize() {
        config = getConfigAs(IpObserverConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        pollingFuture = scheduler.scheduleWithFixedDelay(this::pollStation, 1, config.pollTime, TimeUnit.SECONDS);
    }

<<<<<<< HEAD
<<<<<<< HEAD
        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
>>>>>>> ipObserver creation
=======
=======
>>>>>>> Fix merge conflicts.
    @Override
    public void dispose() {
        channelHandlers.clear();
        @Nullable
        ScheduledFuture<?> localFuture = pollingFuture;
        if (localFuture != null) {
            localFuture.cancel(true);
            localFuture = null;
        }
>>>>>>> Bulk updated to UOM.
    }
}
